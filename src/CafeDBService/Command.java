package CafeDBService;

import java.util.Vector;


public class Command {
	@FunctionalInterface
	interface Task {
	    void handle(String[] args);
	}
	private final Task task;
	public final String WORD;
	private Vector<Command> subcommands;
	
	public Command(String word, Task task) {
		this.WORD = word;
		this.task = task;
	}
	public Command addSubcommand(Command subcommand) {
		this.subcommands.add(subcommand);
		return this;
	}
	public void runTask(String[] args) {
		this.task.handle(args);
	}
}
