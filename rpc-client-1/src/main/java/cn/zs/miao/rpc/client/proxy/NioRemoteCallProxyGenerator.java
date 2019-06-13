package cn.zs.miao.rpc.client.proxy;

import java.lang.reflect.Proxy;

public class NioRemoteCallProxyGenerator {
	
	@SuppressWarnings("unchecked")
	public static <T> T genProxy(Class<T> classInterfaces){
		return (T) Proxy.newProxyInstance(classInterfaces.getClassLoader(), new Class[]{classInterfaces}, new NioRemoteCallHandler());
	}

}
