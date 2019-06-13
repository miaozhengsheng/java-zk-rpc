package cn.zsmiao.rpc.client.proxy;

import java.lang.reflect.Proxy;

public class RemoteCallProxy {
	
	
	@SuppressWarnings("unchecked")
	public static <T> T call(Class<T> interfaces){
		
		return (T) Proxy.newProxyInstance(interfaces.getClassLoader(), new Class[]{interfaces}, new RemoteCallHandler());
		
	}

}
