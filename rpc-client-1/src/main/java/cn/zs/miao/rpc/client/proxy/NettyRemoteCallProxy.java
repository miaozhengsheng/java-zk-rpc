package cn.zs.miao.rpc.client.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
























import java.util.Random;

import cn.zs.miao.rpc.client.NettyClient;
import cn.zs.miao.rpc.util.parameter.RequestParameter;

public class NettyRemoteCallProxy implements InvocationHandler{

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		Class<?> clazz = method.getDeclaringClass();
		
		String clazzName = clazz.getName();
		
		List<String> serverList = getServerList(clazzName);
		if(serverList==null||serverList.size()==0){
			throw new Exception("no server is alived");
		}
		// Zk上注册上的目前可用的节点
		Random random = new Random();
		String hostInfo = serverList.get(random.nextInt(serverList.size()));
		
		String[] split = hostInfo.split(":");
		
		String host = split[0];
		int port = Integer.parseInt(split[1]);
		// 远程调用需要用到的参数
		RequestParameter parameter = new  RequestParameter();
		parameter.setMethodName(method.getName());
		parameter.setParamterArray(args);
		parameter.setServiceName(clazzName);
		// 远程调用得到结果
		System.out.println("当前请求的服务器为："+hostInfo);
		return callRemote(host, port, parameter);
	}

	public static Object callRemote(String host,int port,RequestParameter parameter){
		
		EventLoopGroup workGroup = new NioEventLoopGroup();
		
		RemoteCallProxy remoteCallProxy = new RemoteCallProxy();
		
		Bootstrap server = new Bootstrap();
		server.group(workGroup);
		
		server.channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, true)
		.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				 pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));    
                 pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));    
                 pipeline.addLast("encoder", new ObjectEncoder());      
                 pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));    
                 pipeline.addLast("handler",remoteCallProxy);  

			}
		});
		
		 
         try {
        	 ChannelFuture future = server.connect(host, port).sync();    
             future.channel().writeAndFlush(parameter).sync();  
			 future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}  

		
		workGroup.shutdownGracefully();
		
		return remoteCallProxy.getResult();
	}
	
	public static List<String> getServerList(String clazzName){
		return NettyClient.serverInfo.get(clazzName);
	}
	
	
	static class RemoteCallProxy extends ChannelInboundHandlerAdapter {

		private Object result;
		
		public Object getResult(){
			return result;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			result = msg;
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			System.out.println("远程调用初出现异常:"+cause.getMessage());
		}
		
	}
}
