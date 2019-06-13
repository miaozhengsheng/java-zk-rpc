package cn.zsmiao.rpc.server;

import java.io.IOException;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.RetryOneTime;

import cn.zsmiao.rpc.api.service.IHelloService;
import cn.zsmiao.rpc.server.publisher.Publisher;

public class ServerDemo {

	public static  CuratorFramework newClient = null;
	public static void main(String[] args) throws IOException {
		
		newClient = CuratorFrameworkFactory.newClient("192.168.117.130:2181", new RetryOneTime(1000));
		
		IHelloService helloService = new HelloServiceImpl();
		
		
		Publisher publisher = new Publisher();
		
		publisher.publish(helloService, 9898);
		
		
		System.out.println("服务端已经启动");
		System.in.read();
		newClient.close();
	}
}
