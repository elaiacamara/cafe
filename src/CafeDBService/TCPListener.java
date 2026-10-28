package CafeDBService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPListener {
	private final ServerSocketChannel server;
	private final ClientConnectionsManager clientConnectionManager;
	
	TCPListener(int port, ClientConnectionsManager clientConnectionsManager) throws IOException{
		this.server = ServerSocketChannel.open();
		server.bind(new InetSocketAddress(port));
		this.clientConnectionManager = clientConnectionsManager;
	}
	
	public void acceptNewConnection() throws IOException {
		SocketChannel client = server.accept();
		if (client != null) {
			
		}
	}
}
