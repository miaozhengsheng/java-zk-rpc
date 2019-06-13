package cn.zs.miao.rpc.client.proxy;


import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import cn.zs.miao.rpc.client.RemoteCallDemo;


public class ZkNodeWatcher{

	
	  public void registerWatcher(final String path){
		  
	        PathChildrenCache childrenCache=new PathChildrenCache
	                (RemoteCallDemo.newClient,path,true);

	        PathChildrenCacheListener pathChildrenCacheListener=new PathChildrenCacheListener() {

				public void childEvent(CuratorFramework client,
						PathChildrenCacheEvent event) throws Exception {
					System.out.println("收到事件请求："+event.getType());
					if(event.getType()==PathChildrenCacheEvent.Type.CHILD_ADDED||event.getType()==PathChildrenCacheEvent.Type.CHILD_REMOVED){
						List<String> pathLists = RemoteCallDemo.newClient.getChildren().forPath(path);
						RemoteCallDemo.serverInfo.put(path, new ArrayList<String>(pathLists));
					}
					
				}
	        };
	        
	        
	        childrenCache.getListenable().addListener(pathChildrenCacheListener);
	        try {
	            childrenCache.start();
	        } catch (Exception e) {
	           throw new RuntimeException("注册PatchChild Watcher 异常"+e);
	        }


	    }

}
