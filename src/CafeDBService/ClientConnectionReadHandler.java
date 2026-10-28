package CafeDBService;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ClientConnectionReadHandler implements CompletionHandler<Integer,Void>  {
	public ByteBuffer buffer = ByteBuffer.allocate(4096);
	private final AsynchronousSocketChannel client;
	private final ClientConnectionsManager manager;
	public ClientConnectionReadHandler(ClientConnectionsManager manager,AsynchronousSocketChannel client ) {
		this.manager = manager;
		this.client = client;
	}
	@Override
	public void failed(Throwable exc, Void attachment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completed(Integer bytesRead, Void attachment) {
		// TODO Auto-generated method stub
		if (bytesRead == -1)
			manager.closeConnection(client);
	}


}
