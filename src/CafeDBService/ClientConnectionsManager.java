package CafeDBService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import CafeDBService.Client.ClientDisconnectedException;	


public class ClientConnectionsManager {
	private Map<SocketChannel, Client> clients = new HashMap<>();
	private ServerSocketChannel server;
	private Selector selector;
	private boolean isAcceptingConnections = false;
	
	ClientConnectionsManager(int port) throws IOException{
		this.server = ServerSocketChannel.open();
		server.configureBlocking(false);
		
		server.bind(new InetSocketAddress(port));
		this.selector = Selector.open();
		server.register(selector, SelectionKey.OP_ACCEPT);
	}
	public void startAcceptingConnections() {
		this.isAcceptingConnections = true;
	}
	
	public void closeConnection(AsynchronousSocketChannel client) {
		
	}
	
	public Vector<Message> tick() throws IOException  {
		Vector<SocketChannel> clientsToBeRemoved = new Vector<>();
		selector.select();
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Vector<Message> messages = new Vector<>();
	    for(var key : selectedKeys) {
	    	 if (key.isAcceptable()) {
	             SocketChannel client = server.accept();
	             if (client != null) {
	                 client.configureBlocking(false);
	                 client.register(selector, SelectionKey.OP_READ);
	                 clients.put(client, new Client(client));
	             }
	         }
	    	 if (key.isReadable()) {
	    		 SocketChannel clientSocket = (SocketChannel) key.channel();
	    		 Client client = clients.get(clientSocket);
	    		 try{
	 				String message = client.read();
	 				messages.add(new Message(clientSocket, message));
	 			 }catch(Client.ClientDisconnectedException e) {
	 				 clientsToBeRemoved.add(clientSocket);				
	 			 }catch(Exception e) {
	 				
	 			 }
	    	 }
	    }
	    for (var clientToBeRemoved: clientsToBeRemoved) {
			//todo
		}
	    return messages;
	}
	public Map<SocketChannel,Client> getClients(){
		return clients;//should be public??
	}
}
