package cn.zs.miao.rpc.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import cn.zs.miao.rpc.api.IHelloService;
import cn.zs.miao.rpc.api.ISuperHelloService;
import cn.zs.miao.rpc.client.bean.HostBean;
import cn.zs.miao.rpc.client.proxy.NioRemoteCallProxyGenerator;
import cn.zs.miao.rpc.client.proxy.ZkNodeWatcher;

public class NioClient {

	public static ConcurrentHashMap<String, ArrayList<String>> serverInfo = new ConcurrentHashMap<String, ArrayList<String>>();

	public static CuratorFramework newClient = CuratorFrameworkFactory
			.newClient("192.168.117.130:2181", new RetryNTimes(3, 1000));

	public static Class[] serviceClasses = new Class[] { IHelloService.class,
			ISuperHelloService.class };
	
	public static Set<HostBean> REGISTER_HOST_PORT = new HashSet<HostBean>();
	
	public static Selector selector = null;

	public static void initServerInfo() {
		newClient.start();
		System.out.println("和zk建立连接成功");

		for (Class clazz : serviceClasses) {
			String path = "/rpc/" + clazz.getName();
			try {
				List<String> forPath = newClient.getChildren().forPath(path);
				serverInfo.put(clazz.getName(), new ArrayList<String>(forPath));
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("开始添加监听事件。。。。");
			new ZkNodeWatcher().registerWatcher(path);
		}

	}

	
	private static void initHostInfo() throws IOException {
		if(serverInfo.isEmpty()){
			return;
		}
		selector = Selector.open();
		Iterator<String> iterator = serverInfo.keySet().iterator();
	
		while(iterator.hasNext()){
			
			String serviceName = iterator.next();
			List<String> forPath = NioClient.serverInfo.get(serviceName);

			if (forPath == null || forPath.isEmpty()) {
				System.err.print("no server is alived");
				return;
			}

			String hostPath = forPath.get(new Random().nextInt(forPath.size()));

			String[] split = hostPath.split(":");

			String host = split[0];

			int port = Integer.parseInt(split[1]);

			System.out.println("当前请求的host为：" + host + ",当前请求的port 为：" + port);
			
			REGISTER_HOST_PORT.add(new HostBean(host, port));
		}
		
		Iterator<HostBean> iterator2 = REGISTER_HOST_PORT.iterator();
		
		while(iterator2.hasNext()){
			HostBean next = iterator2.next();
			try {
				SocketChannel socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(Boolean.FALSE);

				SocketAddress socketAddress = new InetSocketAddress(next.getHost(),next.getPort());

				socketChannel.connect(socketAddress);

				socketChannel.register(selector, SelectionKey.OP_CONNECT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) throws IOException {
		

		initServerInfo();
		
		initHostInfo();

		IHelloService helloService = NioRemoteCallProxyGenerator
				.genProxy(IHelloService.class);

		for (int i = 0; i <10; i++) {
			System.out.println(helloService.sayHello("缪正生"));

			System.out.println(helloService.add(1, 2));

			System.err.println("------------------------------");

			ISuperHelloService superHelloService = NioRemoteCallProxyGenerator
					.genProxy(ISuperHelloService.class);
			System.out.println(superHelloService.sayHello("缪正生"));
			System.out.println(superHelloService.add(1, 2));
		}

	}



}
