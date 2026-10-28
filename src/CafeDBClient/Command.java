package CafeDBClient;

public class Command {
	public final String matchingString;
	@FunctionalInterface
	interface HandleCommand {
	    void handle(String[] args);
	}
	private final HandleCommand task;
	
	public Command(String matchingString, HandleCommand task){
		this.matchingString = matchingString;
		this.task = task;
	}
	
	public void runTask(String[] args) {
		task.handle(args);
	}
	
	
}
