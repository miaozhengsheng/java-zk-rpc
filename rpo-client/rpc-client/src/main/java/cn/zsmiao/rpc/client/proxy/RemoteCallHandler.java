package cn.zsmiao.rpc.client.proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.RetryOneTime;

import cn.zsmiao.rpc.api.service.paramter.Request;

public class RemoteCallHandler implements InvocationHandler{

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		Request request = new Request();
		request.setClassName(method.getDeclaringClass().getName());
		request.setParameters(args);
		request.setMethodName(method.getName());
		
		return process(request);
	}
	
	private Object process(Request request){
		
		Socket socket = null;
		
		try {
			
			CuratorFramework newClient = CuratorFrameworkFactory.newClient("192.168.117.132:2181", new RetryOneTime(1000));
			newClient.start();
			
			List<String> forPath = newClient.getChildren().forPath("/rpc/"+request.getClassName());
			
			if(forPath==null||forPath.isEmpty()){
				
				System.out.println("no sever is alived");
				
				return null;
			}
			
			String path = forPath.get(0);
			String[] split = path.split(":");
			
			socket =  new Socket(split[0],Integer.parseInt(split[1]));
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			
			objectOutputStream.writeObject(request);
			
			objectOutputStream.flush();
			
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			
			Object readObject = objectInputStream.readObject();
			
			objectInputStream.close();
			
			objectOutputStream.close();
			
			return readObject;
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(socket!=null){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

}
