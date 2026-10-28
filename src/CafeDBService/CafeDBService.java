package CafeDBService;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;   

public class CafeDBService {
	private ServiceStatus status = ServiceStatus.Starting;
	private final ClientConnectionsManager connectionsManager;
	private final Vector<Command> commands = new Vector<>();
	private Queue<Message> messages = new ArrayDeque<>();
	private Map<String, Database> databases = new HashMap<>();
	private Context context = new Context();
	
	public CafeDBService() throws IOException {
		this.connectionsManager = new ClientConnectionsManager(8080);
		commands.add(new Command("ping", (String[] args) -> {
			this.ping(args);
		}));
		commands.add(new Command("show", (String[] args) ->{
			this.show(args);
		}));
		commands.add(new Command("create", (String[] args) ->{
			this.create(args);
		}));
	}
	public void run() throws IOException {
		boolean is_paused = false;
		boolean should_exit = false;
		while(!should_exit) {
			while(!is_paused) {
				Vector<Message> messages = connectionsManager.tick();
				this.messages.addAll(messages);
				handleClientMessages();
			}
		}
	}
	private void handleClientMessages() {
		for (var message : messages) {
			handleClientMessage(message.message);
		}
		messages.clear();
	}
	static public Result<CodeTree.CodeNode>  parser(String string) {
		Vector<Token> tokens = Lexer.lexer(string);
		System.out.println("tokens: [" + string + ']');
		for (var token : tokens) {
			System.out.println(token.debug());
		}
		Tokenizer.ParserRule parser = new Tokenizer.ParserRule();
		Result<CodeTree.CodeNode> result = parser.parse(tokens);
		System.out.println("Result is: " + result.isOk());
		if (result.isOk()) {
			var value = result.getValue();
		}
		return result;
	}
	private void handleClientMessage(String message) {
		var result = parser(message);
		if (result.isError()) {
			System.out.println("Failed to parse");
			return;
		}
		Program program = new Program(result.getValue(),context);
		/*
		for (var command : commands) {
			if (command.WORD.equals(tokens[0])) {
				System.out.println("Running taks: " + command.WORD);
				command.runTask(tokens);
			}
		}*/
		
	}
	private void ping(String[] args) {
		for (int i = 1; i < args.length; i++) {
			System.out.print(args[i] + " ");
		}
	}
	private void show(String[] args) {
		if (args.length != 3) {
			System.out.println("Solo 3 palabras");
			return;
		}
		if (args[2] != ";") {
			System.out.println("Tiene que terminar en ;");
			return;
		}
		if (!databases.containsKey(args[1])) {
			System.out.println("Failed to find database");
		}else {
			System.out.println("Printing database data");
		}
	}
	private void create(String[] args) {
		if (args.length != 4) {
			System.out.println("Solo 3 palabras");
			return;
		}
		if (args[3] != ";") {
			System.out.println("Tiene que terminar en ;");
			return;
		}
		if (!args[1].equals("database"))
			System.out.println("Tiene que terminar en ;");
		if (!databases.containsKey(args[1])) {
			System.out.println("Failed to find database");
		}else {
			System.out.println("Printing database data");
		}
	}
}
