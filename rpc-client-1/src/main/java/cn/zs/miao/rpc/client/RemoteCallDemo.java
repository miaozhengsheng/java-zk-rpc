package cn.zs.miao.rpc.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import cn.zs.miao.rpc.api.IHelloService;
import cn.zs.miao.rpc.api.ISuperHelloService;
import cn.zs.miao.rpc.client.proxy.RemoteCallProxyGenerator;
import cn.zs.miao.rpc.client.proxy.ZkNodeWatcher;


public class RemoteCallDemo {

	
	public static ConcurrentHashMap<String, ArrayList<String>> serverInfo = new ConcurrentHashMap<String,ArrayList<String>>();
	
	public static CuratorFramework newClient = CuratorFrameworkFactory.newClient("192.168.117.130:2181",new RetryNTimes(3, 1000));
	
	public static Class[] serviceClasses = new Class[]{IHelloService.class,ISuperHelloService.class};
	
	public static void main(String[] args) {
		
		initServerInfo();
		
		IHelloService helloService = RemoteCallProxyGenerator.genProxy(IHelloService.class);
		
		for(int i=0;i<20;i++){
			System.out.println(helloService.sayHello("缪正生"));
			
			System.out.println(helloService.add(1, 2));
			
			System.err.println("------------------------------");
			
			ISuperHelloService superHelloService = RemoteCallProxyGenerator.genProxy(ISuperHelloService.class);
			System.out.println(superHelloService.sayHello("缪正生"));
			System.out.println(superHelloService.add(1, 2));
		}
		
	}
	
	public static void initServerInfo(){
		newClient.start();
		System.out.println("和zk建立连接成功");
		
		for(Class clazz : serviceClasses){
			String path = "/rpc/"+clazz.getName();
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
}
