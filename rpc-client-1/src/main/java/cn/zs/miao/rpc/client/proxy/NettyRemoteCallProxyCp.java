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
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.RetryNTimes;

import cn.zs.miao.rpc.api.IHelloService;
import cn.zs.miao.rpc.util.parameter.RequestParameter;

public class NettyRemoteCallProxyCp {
	
	
	// 当前可用的服务信息
	public static  Map<String, List<String>>  ALIABLE_SERVER_MAP = new HashMap<String, List<String>>();
	
	// 访问ZK集群的客户端
	public static CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("192.168.117.130:2181,192.168.117.131:2181,192.168.117.132:2181", new RetryNTimes(3, 1000));
	
	public static void main(String[] args) {
		curatorFramework.start();
		
		IHelloService helloService = getProxy(IHelloService.class);
		
		int a = 1;
		
		int b = 3;
		for(int i=0;i<10;i++){
			int result = helloService.add(a, b);
			System.out.println(a+"+"+b+"="+result);
			System.out.println(helloService.sayHello("miaozs"));
		}
		
		
	}
	
	
	public static <T> T getProxy(Class<T> clazz){
		
		return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new NettyRemoteCallProxy2());
	}

	
	static class NettyRemoteCallProxy2 implements InvocationHandler{

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			
			if(method.getDeclaringClass()==Object.class){
				return method.invoke(proxy, args);
			}
			
			RequestParameter requestParameter = new RequestParameter();
			requestParameter.setMethodName(method.getName());
			requestParameter.setParamterArray(args);
			requestParameter.setServiceName(method.getDeclaringClass().getName());
			
			List<String> serverList = ALIABLE_SERVER_MAP.get(method.getDeclaringClass().getName());
			// 初始化服务信息
			if(serverList==null||serverList.isEmpty()){
				initServerInfos(method.getDeclaringClass().getName());
			}
			
			serverList = ALIABLE_SERVER_MAP.get(method.getDeclaringClass().getName());
			
			if(serverList==null||serverList.isEmpty()) {
				throw new Exception("no server is alived");
			}
			
			Random random = new Random();
			
			int index = random.nextInt(serverList.size());
			
			String serverInfo = serverList.get(index);
			
			String[] split = serverInfo.split(":");
			
			String host = split[0];
			
			int port = Integer.parseInt(split[1]);
			
			return remoteCall(host, port, requestParameter);
		}
		
		// 远程调用真正执行的地方
		private Object remoteCall(String host,int port,RequestParameter parameter){
			
			RemoteCallHandler handler = new RemoteCallHandler();
			
			Bootstrap bootstrap = new Bootstrap();
			
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			
			bootstrap.group(workerGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					 pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));    
	                 pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));    
	                 pipeline.addLast("encoder", new ObjectEncoder());      
	                 pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));    
	                 pipeline.addLast("handler",handler);  
				}
			});
			
			try {
				ChannelFuture sync = bootstrap.connect(new InetSocketAddress(host, port)).sync();
				
				
				System.out.println("连接到服务端");
				
				sync.channel().writeAndFlush(parameter).sync();
				
				sync.channel().closeFuture().sync();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			workerGroup.shutdownGracefully();
			
			
			return handler.getResult();
		}
		
	}
	
	
	static class RemoteCallHandler extends ChannelInboundHandlerAdapter{

		private Object result;
		
		
		
		public Object getResult() {
			return result;
		}

		public void setResult(Object result) {
			this.result = result;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			this.result = msg;
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			cause.printStackTrace();
		}
		
	}
	
	
	public static void initServerInfos(String serverName){
		
		String rootPath = "/rpc/";
		
		String path = rootPath+serverName;
		
		try {
			List<String> nodeList = curatorFramework.getChildren().forPath(path);
			
			ALIABLE_SERVER_MAP.put(serverName,nodeList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
