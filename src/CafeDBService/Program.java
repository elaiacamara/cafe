package CafeDBService;

public class Program {
	Instruction instructions;
	Program(CodeTree.CodeNode node){
		Context.Item.StaticContext globalContext = Context.createGlobalContext();
		instructions = Instruction.instructionOf(node, globalContext).get();
		instructions.run(context);
	}
}
