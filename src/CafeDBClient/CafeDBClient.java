package CafeDBClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Vector;

public class CafeDBClient {
	private SocketChannel socket;
	private Vector<Command> commands = new Vector<>();
	public CafeDBClient(int port) throws IOException {
		socket = SocketChannel.open(new InetSocketAddress("localhost", port));
		commands.add(new Command("ping", (String[] args) -> {
			this.ping(args);		
		}));
	}
	
	public void run() {
		boolean shouldStop = false;
		Scanner scanner = new Scanner(System.in);
		StringBuilder message = new StringBuilder();
		while(!shouldStop) {
			System.out.print("cafedb: ");
			String inputString = scanner.nextLine();
			
			if (inputString.isEmpty() && message.isEmpty()) {
				continue;
			}
			if (inputString.endsWith("\\")) {
				message.append(inputString, 0, inputString.length() - 1).append("\n");
			}else {
				message.append(inputString);
				try {
					send(message.toString());
					message.setLength(0);
				}catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
			/*
			var words = inputString.split(":");
			for (var command : commands) {
				if (command.matchingString.equals(words[0])) {
					command.runTask(words);
				}
			}*/
		}
	}
	
	private void ping(String[] args){
		if (args.length != 2) {
			System.out.println("Need an argument after :");
			return;
		}
		try {
			send(args[1]);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	private void send(String message) throws IOException {
		var buffer = ByteBuffer.wrap(message.getBytes());
		while(buffer.hasRemaining()) {
			socket.write(buffer);
		}
	}
}
