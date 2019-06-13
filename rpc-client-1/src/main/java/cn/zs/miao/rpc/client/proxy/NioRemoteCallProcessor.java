package cn.zs.miao.rpc.client.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.zs.miao.rpc.client.NioClient;
import cn.zs.miao.rpc.util.parameter.RequestParameter;

public class NioRemoteCallProcessor {

	private String serviceName;

	private String methodName;

	private Object[] parameterArray;
	
	public NioRemoteCallProcessor(String serviceName, String methodName,
			Object[] parameterArray) {
		super();
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.parameterArray = parameterArray;

	}

	public Object callRemote() {

		try {

			RequestParameter requestParameter = new RequestParameter();

			requestParameter.setMethodName(methodName);
			requestParameter.setServiceName(serviceName);
			requestParameter.setParamterArray(parameterArray);

			List<String> forPath = NioClient.serverInfo.get(serviceName);

			if (forPath == null || forPath.isEmpty()) {
				System.err.print("no server is alived");
				return null;
			}

			while (true) {

				int keyCnt = NioClient.selector.select(3 * 1000);

				if (keyCnt <= 0) {
					continue;
				}
				
				Set<SelectionKey> keys = NioClient.selector.selectedKeys();

				Iterator<SelectionKey> iterator = keys.iterator();

				while (iterator.hasNext()) {

					SelectionKey next = iterator.next();

					iterator.remove();

					if (!next.isValid()) {
						continue;
					}

					if (next.isConnectable()) {
						doConnect(next);
					}

					if (next.isReadable()) {
						try {
							Object result = doRead(next);
							return result;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (next.isWritable()) {
						doWrite(next, requestParameter);
					}
				}

			}
			

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private Object doRead(SelectionKey next) throws Exception {

		SocketChannel channel = (SocketChannel) next.channel();

		ByteBuffer buffer = ByteBuffer.allocate(1024);

		channel.read(buffer);
		
		buffer.flip();

		byte[] byteArray = Arrays.copyOf(buffer.array(), buffer.capacity());
		
		channel.register(NioClient.selector, SelectionKey.OP_WRITE);

		ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArray));

		return objectInputStream.readObject();


	}

	private void doWrite(SelectionKey next, RequestParameter requestParameter)
			throws IOException {
		SocketChannel socketChannel = (SocketChannel) next.channel();

		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(byteOutputStream);

		oos.writeObject(requestParameter);

		byte[] resultArray = byteOutputStream.toByteArray();

		byteBuffer.put(resultArray);
		byteBuffer.flip();

		socketChannel.write(byteBuffer);

		socketChannel.register(NioClient.selector, SelectionKey.OP_READ);
	}

	private void doConnect(SelectionKey next) {

		SocketChannel socketChannel = (SocketChannel) next.channel();

		if (!socketChannel.isConnectionPending()) {
			return;
		}

		try {
			socketChannel.finishConnect();
			socketChannel.register(NioClient.selector, SelectionKey.OP_WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
