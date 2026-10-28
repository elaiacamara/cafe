package CafeDBService;

import java.nio.channels.SocketChannel;

public class Message {
	public final SocketChannel client;
	public final String message;
	
	public Message(SocketChannel client, String message) {
		this.client = client;
		this.message = message;
	}
}
