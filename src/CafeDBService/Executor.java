package CafeDBService;

import java.util.Vector;

public class Executor {
	private Vector<Program> programs;
	
	public Executor(){
		
	}
	
	Result<Void> addCode(CodeTree.CodeNode node){
		var program = new Program(node);
		return Result.error("failed");
	}
	Result<Void> executeCode(CodeTree.CodeNode node){
		return Result.error("failed");
	}
}

