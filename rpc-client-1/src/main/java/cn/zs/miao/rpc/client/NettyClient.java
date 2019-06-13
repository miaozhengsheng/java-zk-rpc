package cn.zs.miao.rpc.client;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import cn.zs.miao.rpc.api.IHelloService;
import cn.zs.miao.rpc.api.ISuperHelloService;
import cn.zs.miao.rpc.client.proxy.NettyRemoteCallProxy;
import cn.zs.miao.rpc.client.proxy.ZkNodeWatcher;




public class NettyClient {
	
	
	public static 	CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("192.168.117.131:2181,192.168.117.132:2181", new RetryNTimes(3, 1000));
	
	public static Map<String, List<String>> serverInfo = new ConcurrentHashMap<String, List<String>>(8);
	
	public static Class[] serviceClasses = new Class[] { IHelloService.class,
		ISuperHelloService.class };
	
	public static void initServerInfo(){
		
		curatorFramework.start();
		
		System.out.println("成功连接到ZK集群");
		
		for(Class clazz:serviceClasses){
			
			String path = "/rpc/"+clazz.getName();
			try {
				List<String> forPath = curatorFramework.getChildren().forPath(path);
				if(forPath!=null){
					serverInfo.put(clazz.getName(), new ArrayList<String>(forPath));
				}
				
				System.out.println("开始监控节点："+path);
				
				new ZkNodeWatcher().registerWatcher(path);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void call(){
		
		IHelloService helloService = getRemoteProxy(IHelloService.class);
		int a= 1;
		int b = 2;
		for(int i=0;i<10;i++){
			System.out.println(helloService.add(a, b));
			System.out.println(helloService.sayHello("miao zheng sheng"));
		}
		
	}
	
	public static void main(String[] args) {
		
		initServerInfo();
		
		call();
	
	}
	
	public static <T> T getRemoteProxy(Class<T> clazz){
		return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new NettyRemoteCallProxy());
	}

}
