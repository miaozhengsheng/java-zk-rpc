package cn.zsmiao.rpc.server.publisher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Publisher {
	
	private static final ExecutorService executors = Executors.newCachedThreadPool();
	
	public void publish(Object service,int port){
		
		ServerSocket serverSocket = null;
		
		try {
			
			Class<?>[] interfaces = service.getClass().getInterfaces();
			// 注册服务
			Registry.register(interfaces[0].getName(), "127.0.0.1:"+port);
			
			serverSocket = new ServerSocket(port);
			
			Socket accept = serverSocket.accept();
			
			RequestHandler requestHandler = new RequestHandler(accept, service);
			executors.submit(requestHandler);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(serverSocket!=null){
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
