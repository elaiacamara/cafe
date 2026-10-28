package CafeDBService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Predicate;

import CafeDBService.Result.EmptyOk;
import CafeDBService.Tokenizer.ChainRule.WeightedChoice;
public class Tokenizer {
	public static ChainRule.Repeat repeat(BlockRule blockRule) {
		return new ChainRule.Repeat(blockRule);
	}
	public static ChainRule.Choice choice(Vector<BlockRule> choices) {
		return new ChainRule.Choice(choices);
	}
	public static ChainRule.Choice choice(List<BlockRule> choices) {
		return new ChainRule.Choice(new Vector<>(choices));
	}
	
	public static Pair<BlockRule, Integer> weightedRule(BlockRule rule, int weight){
		return new Pair<>(rule, weight);
	}
	public static ChainRule.WeightedChoice weightedChoice(List<Pair<BlockRule, Integer>> choices) {
		return new ChainRule.WeightedChoice(new Vector<>(choices));
	}
	
	public static ChainRule.Interleaved interleaved(BlockRule content, BlockRule divisor, boolean discardDivisor){
		return new ChainRule.Interleaved(content, divisor, discardDivisor);
	}
	
	//need custom optional with an extra boolean for requirement. Cant be, could be, must be. Only have can cant.

	@SafeVarargs
	public static ChainRule.Sequence sequence(Pair<BlockRule, Boolean>... rules) {
		return new ChainRule.Sequence(rules);
	}
	
	@SafeVarargs
	public static ChainRule.AllExcept allExcept(BlockRule... rules) {
		return new ChainRule.AllExcept(rules);
	}
	
	
	public static ChainRule.Single single(BlockRule rule){
		return new ChainRule.Single(rule);
	}
	
	public static Pair<BlockRule, Boolean> required(BlockRule rule){
		return new Pair<BlockRule, Boolean>(rule, true);
	}
	public static Pair<BlockRule, Boolean> optional(BlockRule rule){
		return new Pair<BlockRule, Boolean>(rule, false);
	}
	
	
	public  sealed interface ChainRule {
			
		public final static class Repeat implements ChainRule{
			BlockRule rule;
			Repeat(BlockRule blockRule){
				this.rule = blockRule;
			}
		}
		public final static class Choice implements ChainRule{
			Vector<BlockRule> choices;
			Choice(Vector<BlockRule> choices){
				this.choices = choices;
			}
		}
		public final static class WeightedChoice implements ChainRule{
			Vector<Pair<BlockRule, Integer>> choices;
			WeightedChoice(Vector<Pair<BlockRule, Integer>> choices){
				this.choices = choices;
			}
		}
		public final static class Sequence implements ChainRule{
			List<Pair<BlockRule, Boolean>> rules;
			@SafeVarargs
			Sequence(Pair<BlockRule, Boolean>... rules){
				this.rules = List.of(rules);
			}
			public static Optional<Pair<BlockRule, Boolean>> required(BlockRule rule){
				return Optional.of(new Pair<>(rule, true));
			}
			public static Optional<Pair<BlockRule, Boolean>> optional(BlockRule rule){
				return Optional.of(new Pair<>(rule, false));
			}
		}
		public final static class Single implements ChainRule{
			BlockRule rule;
			
			Single(BlockRule rule){
				this.rule = rule;
			}
		}
		
		public final static class AllExcept implements ChainRule{
			List<BlockRule> rules;
			
			AllExcept(BlockRule... rules){
				this.rules = List.of(rules);
			}
		}
		
		public final static class Interleaved implements ChainRule{
			BlockRule content;
			BlockRule divisor;
			boolean discardDivisor;
			
			public Interleaved(BlockRule content, BlockRule divisor, boolean discardDivisor) {
				this.content = content;
				this.divisor = divisor;
				this.discardDivisor = discardDivisor;
			}
		}
			
	}
	
	public static class BlockRule{
		public boolean isRule;
		public boolean isToken;
		public final String NAME;
		
		private Optional<Token.Type> token;
		private Optional<Predicate<Token>> checker;
		private Optional<ChainRule> chainRule;
		
		public final Instruction.Type instruction;
		
		public BlockRule(String name, Instruction.Type instruction) {
			isRule = false;
			isToken = false;
			this.token = Optional.empty();
			this.chainRule = Optional.empty();
			this.checker = Optional.empty();
			NAME = name;
			this.instruction = instruction;
		}
		
		public BlockRule(String name) {
			isRule = false;
			isToken = false;
			this.token = Optional.empty();
			this.chainRule = Optional.empty();
			this.checker = Optional.empty();
			NAME = name;
			this.instruction = Instruction.Type.Pass;
		}
		
		public ChainRule getRule() {
			assert isRule : "must be rule";
			return this.chainRule.get();
		}
		
		public Token.Type getToken() {
			assert isToken : "must be rule";
			return this.token.get();
		}
		public BlockRule setChecker(Predicate<Token> checker) {
			this.checker = Optional.of(checker);
			return this;
		}
		public Optional<Boolean> runChecker(Token token){
			if (checker.isEmpty())
				return Optional.empty();
			return Optional.of(checker.get().test(token));
		}
		
		public BlockRule is(Token.Type token) {
			assert isRule == false: "block was already rule";
			isRule = false;
			isToken = true;
			this.token = Optional.of(token);
			this.chainRule = Optional.empty();
			return this;
		}
		
		public BlockRule is(ChainRule rule) {
			assert isToken == false: "block was already token";
			isRule = true;
			isToken = false;
			this.token = Optional.empty();
			this.chainRule = Optional.of(rule);
			return this;
		}
		
		public static BlockRule of(String name, Instruction.Type instruction) {
			return new BlockRule(name, instruction);
		}
		
	}
	
	
	static class ParserRule{
		public BlockRule blockRule;
		public Vector<Token> tokens = new Vector<>();
		public BlockRule programRule = new BlockRule("Program");
		public BlockRule codeBlock = new BlockRule("Code block");
		public BlockRule sentence = new BlockRule("Sentence");
		public BlockRule sentenceBody = new BlockRule("Sentence body");
		public BlockRule expresion = new BlockRule("Expresion", Instruction.Type.Expresion);
		public BlockRule expresionDefinition = new BlockRule("Expresion definition");
		public BlockRule operator = new BlockRule("Operator");
		public BlockRule simpleQuotes = new BlockRule("Simple Quotes");
		public BlockRule doubleQuotes = new BlockRule("Double Quotes", Instruction.Type.String);
		public BlockRule doubleQuotesBody = new BlockRule("Double Quotes Body", Instruction.Type.CollectToString);
		public BlockRule backTicks = new BlockRule("Back Ticks");
		public BlockRule parenthesisBlock = new BlockRule("Parenthesis");
		public BlockRule formatBlock = new BlockRule("Format Block");
		public BlockRule spacedWordBlock = new BlockRule("Spaced Word Block");
		//public BlockRule wordBlock = new BlockRule("Word Block", null);
		public BlockRule whiteSpaceBlock = new BlockRule("White Space Block");
		public BlockRule functionCallBlock = new BlockRule("Function call Block", Instruction.Type.FunctionCall);
		public BlockRule parenthesisList = new BlockRule("Parenthesis list");
		public BlockRule parenthesisNextItem = new BlockRule("Parenthesis next item" );
		public BlockRule unitWord = new BlockRule("Unit word", Instruction.Type.Word);
		public BlockRule identifier = new BlockRule("Identifier", Instruction.Type.Identifier);
		public BlockRule commaBlock = new BlockRule("Comma block rule", null);
		public BlockRule functionIdentifier = new BlockRule("Function identifier", Instruction.Type.FunctionCallIdentifier);
		public BlockRule variableDeclaration = new BlockRule("Variable declaration",Instruction.Type.VariableDeclaration);
		public BlockRule variableReasignment = new BlockRule("Variable Reasignment", Instruction.Type.VariableReasignment);
		public BlockRule value = new BlockRule("Value", Instruction.Type.Value);
		public BlockRule floatNumber = new BlockRule("Float number", Instruction.Type.FloatValue);
		public BlockRule intNumber = new BlockRule("Int number", Instruction.Type.IntValue);
		public BlockRule booleanValue = new BlockRule("Boolean value", Instruction.Type.BooleanValue);
		public BlockRule numericWord = new BlockRule("Numeric word");
		public BlockRule typeBlock = new BlockRule("Type", Instruction.Type.Type);
		public BlockRule parenthesysExpresion = new BlockRule("Parenthesys expresion");
		public BlockRule functionDeclaration = new BlockRule("Function declaration");
		public BlockRule namedTuple = new BlockRule("Named tuple");
		public BlockRule returnType = new BlockRule("Return type");
		public BlockRule typedVariable = new BlockRule("Typed variable");
		public BlockRule functionBody = new BlockRule("Function Body");
		private Map<String, BlockRule> intermediateBlocks = new HashMap<>();

		
		private Map<Character, BlockRule> basicBlocks = new HashMap<>();
		private Map<String, BlockRule> patternBlocks = new HashMap<>();	
		public BlockRule ruleFor(char c){//make basic block dinamically
			if (basicBlocks.containsKey(c)) {
				return basicBlocks.get(c);
			}else {
				var rule = new BlockRule("Basic rule: " + c, Instruction.Type.Pass);
				rule.is(Token.getType(Token.of(c)));
				basicBlocks.put(c, rule);
				return rule;
			}
		}
		public BlockRule ruleFor(char c, Instruction.Type type){//make basic block dinamically
			if (basicBlocks.containsKey(c)) {
				return basicBlocks.get(c);
			}else {
				var rule = new BlockRule("Basic rule: " + c, type);
				rule.is(Token.getType(Token.of(c)));
				basicBlocks.put(c, rule);
				return rule;
			}
		}
		public BlockRule ruleFor(String pattern, Instruction.Type type){
			if (patternBlocks.containsKey(pattern)){
				return patternBlocks.get(pattern);
			}else{
				var rule = new BlockRule("Match " + pattern, type)
						.is(single(unitWord))
						.setChecker((Token word) -> word.value().equals(pattern));
				patternBlocks.put(pattern, rule);
				return rule;
			}
		}
		public BlockRule dynRule(String name) {
			if (intermediateBlocks.containsKey(name)) {
				return intermediateBlocks.get(name);
			}else {
				var rule = new BlockRule("Basic rule: " + name, Instruction.Type.Pass);
				intermediateBlocks.put(name, rule);
				return rule;
			}
		}
		public BlockRule dynRule(String name, Instruction.Type type) {
			if (intermediateBlocks.containsKey(name)) {
				return intermediateBlocks.get(name);
			}else {
				var rule = new BlockRule("Basic rule: " + name, type);
				intermediateBlocks.put(name, rule);
				return rule;
			}
		}
		public ParserRule() {
			//need to make tokens at the moment by just passing string ";" "." "," etc
			programRule.is(repeat(dynRule("programChoices")));
			dynRule("programChoices").is(choice(List.of(sentence, functionDeclaration)));
			functionDeclaration.is(sequence(required(ruleFor("fn", Instruction.Type.None)), required(whiteSpaceBlock), required(functionIdentifier), required(namedTuple), optional(returnType), required(functionBody)));
		    namedTuple.is(sequence(required(ruleFor('(', Instruction.Type.None)), required(dynRule("NamedTupleBody")), required(ruleFor(')',Instruction.Type.None))));
			dynRule("NamedTupleBody").is(interleaved(typedVariable, ruleFor(','), true));
			typedVariable.is(sequence(required(identifier), required(ruleFor(':', Instruction.Type.None)), required(typeBlock)));
			returnType.is(sequence(required(ruleFor("->", Instruction.Type.None)), required(typeBlock)));
			functionBody.is(sequence(required(ruleFor('{')), required(dynRule("sentences").is(repeat(sentence))), required(ruleFor('}'))));
			//dynRule("sentences").is(repeat(sentence));
			sentence.is(sequence(required(sentenceBody), required(ruleFor(';', Instruction.Type.None)),optional(whiteSpaceBlock)));
			sentenceBody.is(choice(List.of(functionCallBlock, variableDeclaration,variableReasignment)));
			variableDeclaration.is(sequence(required(identifier), required(ruleFor(':', Instruction.Type.None)), optional(typeBlock), required(ruleFor('=', Instruction.Type.None)), required(expresion)));
			variableReasignment.is(sequence(required(identifier),required(ruleFor('=', Instruction.Type.None)), required(expresion)));
			//expresion.is(choice(List.of(functionCallBlock,value)));//add operators here later + * /- 
			expresion.is(interleaved(expresionDefinition, operator, false));
			operator.is(weightedChoice(List.of(
					weightedRule(ruleFor('+'),1),
					weightedRule(ruleFor('-'),1),
					weightedRule(ruleFor('*'),2),
					weightedRule(ruleFor('/'),2),
					weightedRule(ruleFor('%'),2)
					)));
			expresionDefinition.is(choice(List.of(functionCallBlock, parenthesysExpresion, value, expresion)));
			parenthesysExpresion.is(sequence(required(ruleFor('(', Instruction.Type.None)), required(expresion), required(ruleFor(')', Instruction.Type.None))));
			value.is(choice(List.of(floatNumber, intNumber, booleanValue, doubleQuotes, unitWord)));
			typeBlock.is(single(unitWord));
			floatNumber.is(sequence(required(numericWord),required(ruleFor('.', Instruction.Type.None)), required(numericWord)));
			intNumber.is(Token.Type.Word).setChecker((Token token) -> token.value().chars().allMatch(Character::isDigit));
			booleanValue.is(Token.Type.Word).setChecker((Token token) -> token.value().equals("true") || token.value().equals("false"));
			numericWord.is(Token.Type.Word).setChecker( (Token token) -> token.value().chars().allMatch(Character::isDigit));
			spacedWordBlock.is(sequence(optional(whiteSpaceBlock),required(unitWord), optional(whiteSpaceBlock)));
			functionCallBlock.is(sequence(required(functionIdentifier), required(parenthesisBlock)));
			functionIdentifier.is(single(identifier));
			//wordBlock.is(choice(List.of(parenthesisBlock, unitWord)));// Token.Word.getType());
			identifier.is(single(unitWord));
			parenthesisBlock.is(sequence(
					required(dynRule("OpenParenthesis", Instruction.Type.None).is(single(ruleFor('(')))),
					required(dynRule("FunctionArgument", Instruction.Type.FunctionArguments).is(interleaved(expresion, ruleFor(','),true))),
					required(dynRule("CloseParenthesis", Instruction.Type.None).is(single(ruleFor(')'))))
					));
			doubleQuotes.is(sequence(
					required(dynRule("OpenQuotes", Instruction.Type.None).is(single(ruleFor('"')))),
					required(doubleQuotesBody),
					required(dynRule("CloseQuotes", Instruction.Type.None).is(single(ruleFor('"'))))
					));
			doubleQuotesBody.is(single(dynRule("QuotesFilter").is(allExcept(dynRule("CloseQuotes")))));
			parenthesisList.is(sequence(required(unitWord), optional(dynRule("ParentesisNextItems"))));
			dynRule("ParentesisNextItems").is(repeat(parenthesisNextItem));
			parenthesisNextItem.is(sequence(required(commaBlock), required(unitWord)));
			unitWord.is(Token.Word.getType());
			commaBlock.is(sequence(optional(whiteSpaceBlock), required(ruleFor(',')),optional(whiteSpaceBlock)));
			whiteSpaceBlock.is(Token.WhiteSpaces.getType());
		}
		public static class ParseResult{
			final boolean TOKEN_CONSUMED;
			final boolean TOKEN_DOESNT_MATCH_RULE;
			final Optional<Integer> CURRENT_INDEX;
			Optional <CodeTree.CodeNode> acumulatedCode;
			Optional <Integer> weight;
			
			private ParseResult(boolean tokenConsumed,
								boolean tokenDoesntMatchRule,
								Optional<Integer> currentIndex,
								Optional<CodeTree.CodeNode> node,
								Optional<Integer> weight)
			{
				TOKEN_CONSUMED = tokenConsumed;
				TOKEN_DOESNT_MATCH_RULE = tokenDoesntMatchRule;
				CURRENT_INDEX = currentIndex;
				
				if (node.isPresent()) {
					acumulatedCode = node;
				}
				this.weight = weight;
			}
			/*public static  ParseResult tokenConsumed(int currentIndex) {
				return new ParseResult(true, false, Optional.of(currentIndex), Optional.empty(), Optional.empty());
			}*/
			public static  ParseResult tokenConsumed(int currentIndex, CodeTree.CodeNode node) {
				return new ParseResult(true, false, Optional.of(currentIndex), Optional.of(node), Optional.empty());
			}
			public static  ParseResult tokenConsumed(int currentIndex, CodeTree.CodeNode node, int weight) {
				return new ParseResult(true, false, Optional.of(currentIndex), Optional.of(node), Optional.of(weight));
			}
			public static  ParseResult tokenDoesntMatchRule() {
				return new ParseResult(false, true, Optional.empty(), Optional.empty(), Optional.empty());
			}
			
			public ParseResult wrapConsumedWith(CodeTree.CodeNode node) {
				return ParseResult.tokenConsumed(this.CURRENT_INDEX.get(), node);
			}
			public ParseResult wrapConsumedWith(BlockRule rule) {
				return ParseResult.tokenConsumed(this.CURRENT_INDEX.get(), CodeTree.CodeNode.from(rule).addChild(this.acumulatedCode.get()));
			}
			
			public ParseResult wrapConsumedWith(BlockRule rule, int weight) {
				return ParseResult.tokenConsumed(this.CURRENT_INDEX.get(), CodeTree.CodeNode.from(rule).addChild(this.acumulatedCode.get()), weight);
			}
		}

        public ParseResult parseInterlievedWeighted(Vector<Token> tokens, int i, BlockRule rule) {
			//10 + 10 + 10 + 10
			var interleaved = (ChainRule.Interleaved) rule.getRule();
			int currentWeight = 0;
            var rootNode = CodeTree.CodeNode.from(rule);
            {
                var contentRes = parseToken(tokens, i, interleaved.content);
                if (contentRes.TOKEN_CONSUMED) {
                    rootNode.addChild(contentRes.acumulatedCode.get());
                    i = contentRes.CURRENT_INDEX.get();
                } else {
                    return ParseResult.tokenDoesntMatchRule();
                }
                var divisorRes = parseToken(tokens, i, interleaved.divisor);
                if (divisorRes.TOKEN_DOESNT_MATCH_RULE) {
                    return ParseResult.tokenConsumed(i, rootNode);
                }
                int tempI = divisorRes.CURRENT_INDEX.get();
                var contentRes2 = parseToken(tokens, tempI, interleaved.content);
                if (divisorRes.TOKEN_DOESNT_MATCH_RULE) {
                    return ParseResult.tokenConsumed(i, rootNode);
                }
                i = contentRes2.CURRENT_INDEX.get();
				
				currentWeight = divisorRes.weight.get();
				rootNode.addChild(divisorRes.acumulatedCode.get());
                rootNode.addChild(contentRes2.acumulatedCode.get());
            }
            while (true) {
                var divisorRes = parseToken(tokens, i, interleaved.divisor);
                if (divisorRes.TOKEN_DOESNT_MATCH_RULE) {
                    return ParseResult.tokenConsumed(i, rootNode);
                }
                int tempI = divisorRes.CURRENT_INDEX.get();
                var contentRes = parseToken(tokens, tempI, interleaved.content);
                if (divisorRes.TOKEN_DOESNT_MATCH_RULE) {
                    return ParseResult.tokenConsumed(i, rootNode);
                }
                i = contentRes.CURRENT_INDEX.get();
               // if (WEIGHTED) {
            	int weightedChoice = divisorRes.weight.get();
				if (weightedChoice > currentWeight){
					var childNode = CodeTree.CodeNode.from(rule);
					childNode.addChild(rootNode.getChilds().get(2))
						.addChild(divisorRes.acumulatedCode.get())
						.addChild(contentRes.acumulatedCode.get());
					rootNode.getChilds().set(2, childNode);
					currentWeight = weightedChoice;
				}else{
					var holder = rootNode;
					rootNode =  CodeTree.CodeNode.from(rule)
									.addChild(holder)
									.addChild(divisorRes.acumulatedCode.get())
									.addChild(contentRes.acumulatedCode.get());
					currentWeight = weightedChoice;
				}
            }
        }

		public ParseResult parseToken(Vector<Token> tokens, int i, BlockRule rule) {
			//RULES
			if (rule.isRule) {
				System.out.println("Parse rule");
				switch (rule.chainRule.get()) {
				
				//REPEAT
				case ChainRule.Repeat r -> {
					var codeNode = CodeTree.CodeNode.from(rule);
					while(true){
						var res = parseToken(tokens, i, r.rule);
						if (res.TOKEN_DOESNT_MATCH_RULE) {
							System.out.println("returning: " + i);
							return ParseResult.tokenConsumed(i, codeNode);
							//return res.wrapConsumedWith(codeNode);
						}
						codeNode.addChild(res.acumulatedCode.get());
						i = res.CURRENT_INDEX.get();
					}
				}
				//CHOICE
				case ChainRule.Choice c -> {
					for (var choice : c.choices){
						var res = parseToken(tokens, i, choice);
						if (res.TOKEN_CONSUMED) {
							return res.wrapConsumedWith(rule);
						}
					}
					return ParseResult.tokenDoesntMatchRule();
				}
				case ChainRule.WeightedChoice c -> {
					for (var choice : c.choices){
						var res = parseToken(tokens, i, choice.key());
						if (res.TOKEN_CONSUMED) {
							return res.wrapConsumedWith(rule, choice.value());
						}
					}
					return ParseResult.tokenDoesntMatchRule();
				}
				//SEQUENCE
				case ChainRule.Sequence s -> {
					var codeNode = CodeTree.CodeNode.from(rule);
					
					for (var item : s.rules) {
						ParseResult result = parseToken(tokens, i, item.key());
						if (result.TOKEN_DOESNT_MATCH_RULE) {
							if (item.value())
								return ParseResult.tokenDoesntMatchRule();
						}else {
							if (item.key().instruction != Instruction.Type.None)
								codeNode.addChild(result.acumulatedCode.get());
							i = result.CURRENT_INDEX.get();
							
						}
					}
					return ParseResult.tokenConsumed(i, codeNode);
				}
				case ChainRule.Single single -> {
					var codeNode = CodeTree.CodeNode.from(rule);
					ParseResult result = parseToken(tokens, i, single.rule);
					if (result.TOKEN_DOESNT_MATCH_RULE) {
							return ParseResult.tokenDoesntMatchRule();
					}
					codeNode.addChild(result.acumulatedCode.get());
					return ParseResult.tokenConsumed(result.CURRENT_INDEX.get(), codeNode);
				}
				case ChainRule.AllExcept allExcept -> {
					var codeNode = CodeTree.CodeNode.from(rule);
					
					while(true){
						for (var item : allExcept.rules) {
							var res = parseToken(tokens, i, item);
							if (res.TOKEN_CONSUMED) {
								return ParseResult.tokenConsumed(i, codeNode);
							}
						}
						codeNode.addChild(CodeTree.CodeNode.from(tokens.get(i), Instruction.Type.Pass));
						i = i+1;
					}
				}
				case ChainRule.Interleaved interleaved -> {
					if (interleaved.divisor.isRule){
						final boolean WEIGHTED = interleaved.divisor.getRule() instanceof ChainRule.WeightedChoice;
						if (WEIGHTED)
							return parseInterlievedWeighted(tokens, i, rule);
						}
					var codeNode = CodeTree.CodeNode.from(rule);
					{
						var contentRes = parseToken(tokens,i,interleaved.content);
						if (contentRes.TOKEN_CONSUMED) {
							codeNode.addChild(contentRes.acumulatedCode.get());
							i = contentRes.CURRENT_INDEX.get();
						}else //todo add option to control this
							return ParseResult.tokenConsumed(i, codeNode);
					}
					while(true) {
						var divisorRes = parseToken(tokens,i,interleaved.divisor);
						if (divisorRes.TOKEN_DOESNT_MATCH_RULE) {
							return ParseResult.tokenConsumed(i, codeNode);
						}
						int tempI = divisorRes.CURRENT_INDEX.get();
						var contentRes = parseToken(tokens,tempI,interleaved.content);
						if (divisorRes.TOKEN_DOESNT_MATCH_RULE) {
							return ParseResult.tokenConsumed(i, codeNode);
						}
						i = contentRes.CURRENT_INDEX.get();
						if (!interleaved.discardDivisor)
							codeNode.addChild(divisorRes.acumulatedCode.get());
						codeNode.addChild(contentRes.acumulatedCode.get());
					}
				}
				default -> throw new RuntimeException("unhandled case");
				}

			}else {
				System.out.println("Parse token");
				// TOKEN
				if (i >= tokens.size())
					return ParseResult.tokenDoesntMatchRule();
				if (Token.parseTokenToType(tokens.get(i), rule.token.get())){
					if (rule.checker.isPresent()) {
						if (rule.checker.get().test(tokens.get(i))){
							System.out.println("Token [" + i + "] consumed: " + tokens.get(i).debug());
							return ParseResult.tokenConsumed(i + 1, CodeTree.CodeNode.from(tokens.get(i), rule.instruction));
						}else
							return ParseResult.tokenDoesntMatchRule();
					}
					System.out.println("Token [" + i + "] consumed: " + tokens.get(i).debug());
					return ParseResult.tokenConsumed(i + 1, CodeTree.CodeNode.from(tokens.get(i), rule.instruction));
				}
				return ParseResult.tokenDoesntMatchRule();
			}
			
		}
		public  Result<CodeTree.CodeNode> parse(Vector<Token> tokens) {
			var result = parseToken(tokens, 0, programRule);
			System.out.println("token consumed: " + result.TOKEN_CONSUMED);
			System.out.println("last index: " + result.CURRENT_INDEX.get());
			if (result.TOKEN_CONSUMED && result.CURRENT_INDEX.get() == tokens.size()) {
				result.acumulatedCode.get().print();
				return Result.ok(result.acumulatedCode.get());//success
			}
			return Result.error("failed to parse");
		}
	}
	
}
