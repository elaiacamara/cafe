package CafeDBService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;

public class Instruction {
	/*
	Compile steps
	Single module
	Traverse ast tree and create a program context and a entry point
	for now everything can be explain with a context

	When traversing we create the compiletime context
	
	
	*/
	public interface CmpInstruction{
		public Object cmpRun(Context.CompileTimeContext context);
		public Result<Result.EmptyOk> cmpResolve(Context.CompileTimeContext context);
	}
	public interface RuntimeInstruction{
		public Object run(Context.RuntimeContext context);
	}

	public static enum Type{
		FunctionDeclaration,
		FunctionCall,
		FunctionArguments,
		Word,
		Sentence,
		Program,
		FunctionCallIdentifier,
		Identifier,
		CollectToString,
		Type,
		VariableDeclaration,
		VariableReasignment,
		IntValue,
		FloatValue,
		BooleanValue,
		Operation,
		String,
		Value,
		Expresion,
		None, //wont create an instruction
		Pass //pases the child instruction as own
	}
	
	public static Optional<Instruction> instructionOf(CodeTree.CodeNode node, Context.CompileTimeContext compileTimeContext) {
		if (node.isRule())
			System.out.println("Instruction of: " + node.getRule());
		
		return switch (node.instruction) {
		case Operation -> {
			Token token = node.getChilds().get(0).getValue();
			yield Optional.of(new Word(token.value()));
		}
		case FunctionCall  ->  {
			Instruction identifier =  instructionOf(node.getChild(0), compileTimeContext).get();
			Instruction arguments = instructionOf(node.getChild(1), compileTimeContext).get();
			yield Optional.of(new FunctionCall(
				(FunctionCallIdentifier)identifier,
				(FunctionArguments)arguments
				));
		}
		case Word  -> Optional.of(new Word(((Token.Word)node.getValue()).value));
		case FunctionArguments  -> {
			Vector<Instruction> arguments = new Vector<>();
			for (var child : node.getChilds()) {
				arguments.add(instructionOf(child, compileTimeContext).get());
			}
			yield Optional.of(new FunctionArguments(arguments));
		}
		case FunctionCallIdentifier  -> {
			Identifier temp = (Identifier)(instructionOf(node.getChild(0), compileTimeContext).get());
			yield Optional.of(new FunctionCallIdentifier(temp));
		}
		case None -> {
			yield Optional.empty();
		}
		
		case Identifier -> {
			yield Optional.of(new Identifier(instructionOf(node.getChild(0), compileTimeContext).get()));
		}
		case CollectToString -> {
			AllExcept collection = (AllExcept)instructionOf(node.getChild(0), compileTimeContext).get();
			yield Optional.of(new CollectToString(collection));
		}
		case Type -> {
			yield Optional.of(new TypeInstruction((Word)instructionOf(node.getChild(0), compileTimeContext).get()));
		}
		case VariableDeclaration -> {
			Identifier identifier = (Identifier)instructionOf(node.getChild(0), compileTimeContext).get();
			TypeInstruction type = (TypeInstruction)instructionOf(node.getChild(1), compileTimeContext).get();
			Instruction value = instructionOf(node.getChild(2), compileTimeContext).get();
			yield Optional.of(new VariableDeclaration(identifier, type, value));
		}
		case VariableReasignment -> {
			Identifier identifier = (Identifier)instructionOf(node.getChild(0), compileTimeContext).get();
			Instruction value = instructionOf(node.getChild(1), compileTimeContext).get();
			yield Optional.of(new VariableReasignment(identifier, value));
		}
		case Value -> {
			Instruction value = instructionOf(node.getChild(0), compileTimeContext).get();
			yield Optional.of(new Value(value));
		}
		case IntValue -> {
			Token.Word word = (Token.Word)node.getValue();
			yield Optional.of(new ValueCast(word.value(), Integer.class));
		}
		case BooleanValue -> {
			Token.Word word = (Token.Word)node.getValue();
			yield Optional.of(new ValueCast(word.value(), Boolean.class));
		}
		case FloatValue -> {
			StringBuilder word = new StringBuilder();
			Token.Word real = (Token.Word)node.getChilds().get(0).getValue();
			Token.Word fract = (Token.Word)node.getChilds().get(1).getValue();
			yield Optional.of(new ValueCast(word.append(real).append(".").append(fract).toString(), Float.class));
			
		}
		case String ->{
			Instruction value = instructionOf(node.getChild(0), compileTimeContext).get();
			yield Optional.of(new StringValue(value));
		}
		case Expresion -> {
			if (node.getChilds().size() == 1)
				yield Optional.of(instructionOf(node.getChild(0), compileTimeContext).get());
			var left = instructionOf(node.getChild(0),compileTimeContext).get();
			var operator = instructionOf(node.getChild(1),compileTimeContext).get();
			var right = instructionOf(node.getChild(2),compileTimeContext).get();
			yield Optional.of(new Operation(left, right, operator));
		}
		case FunctionDeclaration -> {
			Identifier functionIdentifier = (Identifier)instructionOf(node.getChild(0), compileTimeContext).get();
			NamedTuple arguments = (NamedTuple)instructionOf(node.getChild(1), compileTimeContext).get();
			Optional<Instruction> returnType = Optional.empty();
			Instruction body;
			if (node.getChilds().size() == 4){
				returnType = Optional.of((TypeInstruction)instructionOf(node.getChild(2), compileTimeContext).get());
				body = instructionOf(node.getChild(3), compileTimeContext).get();
			}else{
				body = instructionOf(node.getChild(2), compileTimeContext).get();
			}
			yield Optional.of(new FunctionDeclaration(functionIdentifier, arguments, returnType, body, compileTimeContext));
		}
		case Pass -> {
			if (node.isRule()) {
				switch (node.getRule()) {
				case Tokenizer.ChainRule.Interleaved interleaved -> {
					Vector<Instruction> instructions = new Vector<>();
					for (var child : node.getChilds()) {
						System.out.println("Program repeat line");
						var result = instructionOf(child, compileTimeContext);
						if (result.isPresent()) {
							instructions.add(result.get());
						}
					}
					if (instructions.size() == 1)
						yield Optional.of(instructions.get(0));
					yield Optional.of(new Interleaved(instructions, interleaved.discardDivisor));
				}
				case Tokenizer.ChainRule.Choice choice -> {
					yield instructionOf(node.getChild(0), compileTimeContext);
				}
				case Tokenizer.ChainRule.WeightedChoice weightedChoice -> {
					yield instructionOf(node.getChild(0), compileTimeContext);
				}
				case Tokenizer.ChainRule.Repeat repeate -> {
					System.out.println("Program repeat");
					Vector<Instruction> instructions = new Vector<>();
					for (var child : node.getChilds()) {
						System.out.println("Program repeat line");
						var result = instructionOf(child, compileTimeContext);
						if (result.isPresent()) {
							instructions.add(result.get());
						}
					}
					yield Optional.of(new Repeat(instructions));
				}
				case Tokenizer.ChainRule.Sequence sequence -> {
					Vector<Instruction> instructions = new Vector<>();
					for (var child : node.getChilds()) {
						var result = instructionOf(child, compileTimeContext);
						if (result.isPresent()) {
							instructions.add(result.get());
							System.out.println("Sequence: " + result.get().toString());
						}
						
					}
					if (instructions.size() == 1)
						yield Optional.of(instructions.get(0));
					yield Optional.of(new Sequence(instructions));
				}
				case Tokenizer.ChainRule.Single single -> {
					yield instructionOf(node.getChild(0), compileTimeContext);
				}
				case Tokenizer.ChainRule.AllExcept allExcept -> {
					Vector<Instruction> instructions = new Vector<>();
					for (var child : node.getChilds()) {
						var temp = instructionOf(child, compileTimeContext);
						if (temp.isPresent())
							instructions.add(temp.get());
					}
					yield Optional.of(new AllExcept(instructions));
				}
				default -> {throw new RuntimeException("Unhandled case");}
				}
			}else {
				yield Optional.of(new Word(node.getValue().value()));
			}
		}
		default -> throw new RuntimeException("Unhandled case");
		};
	}
	
	public Object run(Context.RuntimeContext context);
	
	final class FunctionDeclaration implements Instruction{
		Context.Identifier identifier;
		NamedTuple functionArguments;
		Optional<TypeInstruction> returnType;
		Instruction body;

        public FunctionDeclaration(Identifier identifierSrc, NamedTuple functionArguments, Optional<TypeInstruction> returnType, Instruction body, Context.CompileTimeContext context) {
			String name = identifierSrc.getIdentifier();
			this.identifier = context.currentContext.copyWith(name);
			this.functionArguments = functionArguments;
			this.returnType = returnType;
			this.body = body;

			var function = new Context.Item.Function(this.identifier, Context.Item.AccessType.Public, (List<Object> args, Context.RuntimeContext runtimeContext) -> {
				var functionContent = new Context.Content(this.identifier);
				var parameters = this.functionArguments.runNamedTuple(runtimeContext);
				if (args.size() != parameters.size()){
					throw new RuntimeException("Argument count missmatch");
				}
				for (int i = 0; i < args.size(); i++){
					functionContent.declareVariable(parameters.get(i).key(), parameters.get(i).value(), args.get(i));
				}
				var stack = new Stack<Context.Content>();
				stack.add(functionContent);
				runtimeContext.functionsContext.add(stack);
				return null;
			});
			context.programContext.content.addItem(identifier, function);
		}

		public void runCmp(Context.CompileTimeContext context){
		}

		public Object run(Context.RuntimeContext context){
			return null;
		}
		
	}

	final class NamedTuple implements Instruction{
		List<TypedVariable> variables;

        public NamedTuple(List<TypedVariable> variables) {
			this.variables = variables;
        }

		public Object run(Context.RuntimeContext context){
			return runNamedTuple(context);
		}
		public List<Pair<String,Class<?>>> runNamedTuple(Context.RuntimeContext context){
			List<Pair<String, Class<?>>> variables = new ArrayList<>();
			for (var variable : this.variables){
				variables.add(variable.runTypedVariable(context));
			}
			return variables;
		}
		
	}

	final class TypedVariable implements Instruction{
		Identifier identifier;
		TypeInstruction type;

        public TypedVariable(Identifier identifier, TypeInstruction type) {
			this.identifier = identifier;
			this.type = type;
        }

		public Object run(Context.RuntimeContext context){
			return runTypedVariable(context);
		}

		public Pair<String, Class<?>> runTypedVariable(Context.RuntimeContext context){
			return new Pair<>(identifier.runIdentifier(context), type.runType(context));
		}

		
	}

	final class Operation implements Instruction{
		Instruction operator;
		Instruction left;
		Instruction right;
		public Operation(Instruction left, Instruction right, Instruction operator) {
			this.operator = operator;
			this.left = left;
			this.right = right;
		}
		
		public static int add(Integer left, Integer right){
			return left + right;
		}

		public static Integer intOperation(int left, int right, String operation){
			return switch (operation){
				case "+" -> left + right;
				case "-" -> left - right;
				case "/" -> left / right;
				case "*" -> left * right;
				case "%" -> left % right;
				default -> throw new RuntimeException("Unknown operator: " + operation);
			};
		}

		public static Float floatOperation(float left, float right, String operation){
			return switch (operation){
				case "+" -> left + right;
				case "-" -> left - right;
				case "/" -> left / right;
				case "*" -> left * right;
				case "%" -> left % right;
				default -> throw new RuntimeException("Unknown operator: " + operation);
			};
		}

		public static  Boolean booleanOperation(boolean left, boolean right, String operation){
			return switch (operation){
				case "==" -> left == right;
				case "!=" -> left != right;
				default -> throw new RuntimeException("Unknown operator: " + operation);
			};
		}

		public static String stringOperation(String left, String right, String operation){
			return switch (operation){
				case "+" -> left + right;
				default -> throw new RuntimeException("Unknown operator: " + operation);
			};
		}

		public Object run(Context.RuntimeContext context) {
			String operator = (String) this.operator.run(context);
			Object left = this.left.run(context);
			Object right = this.right.run(context);
			if (left.getClass() != right.getClass())
				throw new RuntimeException("Type missmatch");
			return switch (left){
				case Integer number ->  intOperation((Integer)left, (Integer)right, operator);
				case Float number ->  floatOperation((Float)left, (Float)right, operator);
				case Boolean number ->  booleanOperation((Boolean)left, (Boolean)right, operator);
				case String number ->  stringOperation((String)left, (String)right, operator);
				default -> throw new RuntimeException("Unknown type");
			};
		}
	}
	
	final class StringValue implements Instruction{
		Instruction stringValue;
		
		public StringValue(Instruction stringValue) {
			this.stringValue = stringValue;
		}
		public Object run(Context.RuntimeContext context) {
			return (String)stringValue.run(context);
		}
	}
	
	final class ValueCast implements Instruction{
		Class<?> type;
		String value;
		
		public ValueCast(String value, Class<?> type) {
			this.type = type;
			this.value = value;
		}
		
		public Object run(Context.RuntimeContext context) {
			if (type == Integer.class)  return Integer.valueOf(value);
		    if (type == Float.class)    return Float.valueOf(value);
		    if (type == Boolean.class)  return Boolean.valueOf(value);
		    throw new IllegalArgumentException("Unsupported type: " + type);
			//return type.valueOf(value);
		}
	}
	
	final class TypeInstruction implements Instruction{
		//public Instruction type;
		public Context.Identifier type;
		public TypeInstruction(Word type, Context.CompileTimeContext context) {
			this.type = new Context.Identifier(type.VALUE);
		}
		
		public Object run(Context.RuntimeContext context) {
			Optional<Class<?>> type = context.getType((String)this.type.run(context));
			if (type.isPresent())
				return type.get();
			throw new RuntimeException("Type not found");
		}
		
		public Class<?> runType(Context context){
			return (Class<?>)run(context);
		}
	}

	final class VariableReasignment implements Instruction{
		public Identifier identifier;
		public Instruction value;
		
		public VariableReasignment(Identifier identifier, Instruction value) {
			this.identifier = identifier;
			this.value = value;
		}
		
		public Object run(Context.RuntimeContext context) {
			String identifier = this.identifier.runIdentifier(context);
			Optional<Object> currentValue = context.getVariable(identifier);
			if (currentValue.isPresent()) {
				var result = context.reasignVariable(identifier, value.run(context)); 
				if (result.isError())
					throw new RuntimeException("Failed to reasign variable" + result.getError());
			}else {
				throw new RuntimeException("Variable doens't exist");
			}
			return null;
		}
	}
	
	final class Value implements Instruction{
		public Instruction value;
		public Value(Instruction value) {
			this.value = value;
		}
		
		public Object run(Context.RuntimeContext context) {
			Object value = this.value.run(context);
			switch (value) {
			case Integer intValue -> {
				return intValue;
			}
			case Float floatValue -> {
				return floatValue;
			}
			case Boolean booleanValue -> {
				return booleanValue;
			}
			default -> {
				String stringValue = (String)value;
				{
					Optional<Object> variableValue = context.getVariable(stringValue);
					if (variableValue.isPresent()) {
						return variableValue.get();
					}
					return stringValue;
				}	
			}
			}
			
		}
	}
	
	class VariableDeclaration implements CmpInstruction, RuntimeInstruction{
		public Identifier identifier;
		public TypeInstruction type;
		public Instruction value;
		
		public VariableDeclaration(Identifier identifier, TypeInstruction type, Instruction value){
			this.identifier = identifier;
			this.type = type;
			this.value = value;
		}
		
		public Object run(Context.RuntimeContext context) {
			var result = context.declareVariable(this.identifier.runIdentifier(context), type.runType(context), value.run(context));
			if (result.isError())
				throw new RuntimeException(result.getError().errorMessage);
			return null;
		}
	}
	
	final class CollectToString implements Instruction{
		public Instruction collection;
		public CollectToString(Instruction collection) {
			this.collection = collection;
		}
		public Object run(Context.RuntimeContext context) {
			Vector<Object> results = (Vector<Object>)collection.run(context);
			String value = "";
			for (var item : results) {
				value += (String)item;
			}
			return value;
		}
	}
	final class AllExcept implements Instruction{
		public Vector<Instruction> instructions;
		public AllExcept(Vector<Instruction> instructions) {
			this.instructions = instructions;
		}
		public Object run(Context.RuntimeContext context) {
			Vector<Object> results = new Vector<>();
			for (var instruction : instructions) {
				results.add(instruction.run(context));
			}
			return results;
		}
	}
	
	final class Sequence implements Instruction{
		public Vector<Instruction> instructions;
		
		public Sequence(Vector<Instruction> instructions) {
			this.instructions = instructions;
		}
		public Object run(Context.RuntimeContext context) {
			Vector<Object> results = new Vector<>();
			for (var instruction : instructions) {
				results.add(instruction.run(context));
			}
			return results;
		}
	}
	final class Repeat implements Instruction{
		public Vector<Instruction> instructions;
		
		public Repeat(Vector<Instruction> instructions) {
			this.instructions = instructions;
		}
		public Object run(Context.RuntimeContext context) {
			Vector<Object> results = new Vector<>();
			for (var instruction : instructions) {
				results.add(instruction.run(context));
			}
			return results;
		}
	}
	
	final class Interleaved implements Instruction{
		public Vector<Instruction> instructions;
		public final boolean DISCARDED_DIVISOR;
		
		public Interleaved(Vector<Instruction> instructions, boolean discardedDivisor) {
			this.instructions = instructions;
			this.DISCARDED_DIVISOR = discardedDivisor;
		}
		public Object run(Context.RuntimeContext context) {
			Vector<Object> results = new Vector<>();
			for (var instruction : instructions) {
				results.add(instruction.run(context));
			}
			return results;
		}
	}
	
	final class FunctionCall implements Instruction {
		public FunctionCallIdentifier functionNameSrc;
		public FunctionArguments args;
		public FunctionCall(FunctionCallIdentifier functionName, FunctionArguments argSrc){
			this.functionNameSrc = functionName;
			this.args = argSrc;
		}
		public Object run(Context.RuntimeContext context) {
			return context.callFunction(functionNameSrc.runFunctionCallIdentifier(context), args.runFunctionArguments(context));
		}
	}
	
	/*
	final class ProgramLine implements Instruction{
		
	}
	final class FunctionDeclaration implements Instruction {
		
	}*/
	final class FunctionArguments implements Instruction {
		public Vector<Instruction> arguments;
		
		public FunctionArguments(Vector<Instruction> arguments) {
			this.arguments = arguments;
		}
		
		public Object run(Context.RuntimeContext context) {
			Vector<Object> arguments = new Vector<>();
			for (var arg : this.arguments) {
				arguments.add(arg.run(context));
			}
			return arguments;
		}
		@SuppressWarnings("unchecked")
		public Vector<Object> runFunctionArguments(Context.RuntimeContext context){
			return (Vector<Object>)run(context);
		}
		
	}
	final class FunctionArgument implements Instruction{
		public Instruction argSrc;
		public Object run(Context.RuntimeContext context) {
			return argSrc.run(context);
		}
	}
	
	/*
	final class ProgramExecution implements Instruction {
		
	}*/
	
	final class Word implements Instruction {
		public final String VALUE;
		public Word(String value) {
			this.VALUE = value;
		}
		public Object run(Context.RuntimeContext context) {
			return VALUE;
		}
	}
	
	final class Identifier implements Instruction{
		public Word valueSrc;
		public Identifier(Word valueSrc) {
			this.valueSrc = valueSrc;
		}
		public Object run(Context.RuntimeContext context) {
			
			String identifier =  (String)valueSrc.run(context);
			if (identifier.length() > 0 &&  (Character.isAlphabetic(identifier.charAt(0)) || identifier.charAt(0) == '_')) {
				return identifier;
				
			}
			throw new RuntimeException("identifier missmatch");
		}

		public String getIdentifier(){
			String identifier =  valueSrc.VALUE;
			if (identifier.length() > 0 &&  (Character.isAlphabetic(identifier.charAt(0)) || identifier.charAt(0) == '_')) {
				return identifier;
			}
			throw new RuntimeException("malformed identifier");
		}
		
		public String runIdentifier(Context.RuntimeContext context) {
			return (String) this.run(context);
		}
	}
	
	final class FunctionCallIdentifier implements Instruction{
		public Identifier valueSrc;
		
		public FunctionCallIdentifier(Identifier valueSrc) {
			this.valueSrc = valueSrc;
		}
		
		public Object run(Context.RuntimeContext context) {
			String functionIdentifier = valueSrc.runIdentifier(context);
			/*if (!context.functions.containsKey(functionIdentifier)) {
				throw new RuntimeException("Function" + functionIdentifier + " not found.");//context.getline
			}*/
			return functionIdentifier;
		}
		public String runFunctionCallIdentifier(Context.RuntimeContext context) {
			return (String)run(context);
		}
	}
	
	
}
