package cn.zsmiao.rpc.client.demo;

import cn.zsmiao.rpc.api.service.IHelloService;
import cn.zsmiao.rpc.client.proxy.RemoteCallProxy;

public class ClientDemo {

	public static void main(String[] args) {
		IHelloService helloService = RemoteCallProxy.call(IHelloService.class);
		
		System.out.println(helloService.sayHello("缪正生"));
	}
}
