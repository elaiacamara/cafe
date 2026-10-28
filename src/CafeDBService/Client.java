package CafeDBService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

public class Client {
	public class ClientDisconnectedException extends Exception {}
	private final SocketChannel socket;
	
	public Client(SocketChannel socket) {
		this.socket = socket;
	}
	
	public String read() throws IOException, ClientDisconnectedException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		StringBuilder input = new StringBuilder();
		while (true) {
			int bytesRead = this.socket.read(buffer);
			if (bytesRead == -1) {
				throw new ClientDisconnectedException();
			}else if (bytesRead == 0) {
				if (input.length() != 0) {
					return input.toString();
				}
			}
			input.append(new String(buffer.array(), 0, bytesRead));
			buffer.clear();
		}
	}

}
