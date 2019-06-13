package cn.zsmiao.rpc.server.publisher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

import cn.zsmiao.rpc.api.service.paramter.Request;

public class RequestHandler implements Runnable{
	
	private Socket socket = null;
	
	private Object service;
	
	

	public RequestHandler(Socket socket, Object service) {
		this.socket = socket;
		this.service = service;
	}



	public void run() {
			
		ObjectInputStream objectInputStream = null;
		
		try {
			objectInputStream = new ObjectInputStream(socket.getInputStream());
			Request request = (Request)objectInputStream.readObject();
			
			String methodName = request.getMethodName();
			
			Object[] parameters = request.getParameters();
			
			Class<?>[] paramterTypes = null;
			
			if(parameters!=null){
				paramterTypes = new Class<?>[parameters.length];
				for(int i=0;i<parameters.length;i++){
					paramterTypes[i] = parameters[i].getClass();
				}
			}
			
			Method method = service.getClass().getMethod(methodName, paramterTypes);
			
			Object result = method.invoke(service, parameters);
			
			OutputStream outputStream = socket.getOutputStream();
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			
			objectOutputStream.writeObject(result);
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(null!=objectInputStream){
				try {
					objectInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
