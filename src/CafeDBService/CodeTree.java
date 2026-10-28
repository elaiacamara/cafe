package CafeDBService;

import java.util.Optional;
import java.util.Vector;

public class CodeTree {
	CodeNode childs;
	
	public static class CodeNode{
		private String name = "unset";
		private Vector<CodeNode> childs;
		private Optional<Token> value;
		//private Optional<Tokenizer.BlockRule> rule;
		private Optional<Tokenizer.ChainRule> chainRule;
		public Instruction.Type instruction;
		private CodeNode(){
			childs = new Vector<>();
			value = Optional.empty();
			//rule = Optional.empty();
			chainRule = Optional.empty();
			instruction = Instruction.Type.Pass;
		}
		public CodeNode getChild(int i){
			return childs.get(i);
		}
		public Vector<CodeNode> getChilds(){
			return childs;
		}
		public static CodeNode from(Tokenizer.BlockRule blockRule) {
			var val = new CodeNode();
			val.setRule(blockRule.getRule());
			val.instruction = blockRule.instruction;
			val.name = blockRule.NAME;
			return val;
		}
		
		public static CodeNode from(Token token, Instruction.Type instruction) {
			var val = new CodeNode();
			val.setValue(token);
			val.instruction = instruction;
			//val.name = token.debug();
			return val;
		}
		CodeNode addChild(CodeNode node) {
			childs.add(node);
			return this;
		}
		CodeNode addChilds(Vector<CodeNode> nodes) {
			childs.addAll(nodes);
			return this;
		}
		boolean isValue() {
			assert chainRule.isPresent() != value.isPresent() : "one must be set";
 			return value.isPresent();
		}
		boolean isRule() {
			assert chainRule.isPresent() != value.isPresent() : "one must be set";
 			return chainRule.isPresent();
		}
		
		Token getValue() {
			assert isValue() : "must be values";
			return value.get();
		}
		Tokenizer.ChainRule getRule() {
			assert isRule() : "must be values";
			return chainRule.get();
		}
		void setValue(Token token) {
			assert token != null : "token cant be null";
			assert chainRule.isEmpty() == true : "already set to rule";
			this.value = Optional.of(token);
		}
		
		void setRule(Tokenizer.ChainRule blockRule) {
			assert blockRule != null : "blockRule cant be null";
			assert value.isEmpty() == true : "already set to value";
			this.chainRule = Optional.of(blockRule);
		}
		private void impl_print(int depth) {
			for (int i = 0; i < depth; i++) {
				System.out.print(" ");
			}
			if (chainRule.isPresent()) {
				System.out.print(name);
			}
			if (value.isPresent()) {
				System.out.print(value.get().debug());
			}
			System.out.print("			 Instruction: " + instruction);
			System.out.println("");
			for (var child : childs) {
				child.impl_print(depth + 1);
			}
		}
		public void print() {
			impl_print(0);
		}
	}
}
