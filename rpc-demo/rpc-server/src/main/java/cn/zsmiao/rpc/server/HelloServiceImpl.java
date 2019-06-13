package cn.zsmiao.rpc.server;

import cn.zsmiao.rpc.api.service.IHelloService;

public class HelloServiceImpl implements IHelloService{

	public String sayHello(String name) {
		return "Hello "+name;
	}

}
