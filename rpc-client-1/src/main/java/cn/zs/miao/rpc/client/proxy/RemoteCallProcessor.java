package cn.zs.miao.rpc.client.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import cn.zs.miao.rpc.client.RemoteCallDemo;
import cn.zs.miao.rpc.util.parameter.RequestParameter;


public class RemoteCallProcessor {

	private String serviceName;
	
	private String methodName;
	
	private Object[] parameterArray;
	
	

	
	
	public RemoteCallProcessor(String serviceName, String methodName,
			Object[] parameterArray) {
		super();
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.parameterArray = parameterArray;
	}
	
	
	
	public Object callRemote(){
		
		
		List<String> forPath = RemoteCallDemo.serverInfo.get(serviceName);
		
		if(forPath==null||forPath.isEmpty()){
			System.err.print("no server is alived");
			return null;
		}
		
		
		String hostPath = forPath.get(new Random().nextInt(forPath.size()));
		
		String[] split = hostPath.split(":");
		
		String host = split[0];
		
		int port = Integer.parseInt(split[1]);
		
		try {
			
			RequestParameter requestParameter = new RequestParameter();
			
			requestParameter.setMethodName(methodName);
			requestParameter.setServiceName(serviceName);
			requestParameter.setParamterArray(parameterArray);
			
			System.out.println("当前请求的host为："+host+",当前请求的port 为："+port);
			
			Socket socket = new Socket(host, port);
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			
			objectOutputStream.writeObject(requestParameter);
			
			InputStream inputStream = socket.getInputStream();
			
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			
			Object result = objectInputStream.readObject();
			
			objectOutputStream.close();
			objectInputStream.close();
			socket.close();
			
			return result;
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	

	
}
