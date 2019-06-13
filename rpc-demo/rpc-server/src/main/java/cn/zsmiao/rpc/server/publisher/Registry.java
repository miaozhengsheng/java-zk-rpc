package cn.zsmiao.rpc.server.publisher;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import cn.zsmiao.rpc.server.ServerDemo;


public class Registry {

	
	
	public static void register(String serviceName,String url){
		
		ServerDemo.newClient.start();
		try {
			Stat forPath = ServerDemo.newClient.checkExists().forPath("/rpc/"+serviceName);
			if(forPath==null){
				ServerDemo.newClient.create().creatingParentsIfNeeded().forPath("/rpc/"+serviceName,"0".getBytes());
			}
			ServerDemo.newClient.create().withMode(CreateMode.EPHEMERAL).forPath("/rpc/"+serviceName+"/"+url,"0".getBytes() );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
