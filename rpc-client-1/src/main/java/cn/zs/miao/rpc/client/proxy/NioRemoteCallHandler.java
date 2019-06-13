package cn.zs.miao.rpc.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class NioRemoteCallHandler implements InvocationHandler{

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		return new NioRemoteCallProcessor(method.getDeclaringClass().getName(), method.getName(), args).callRemote();
	}
	
}
