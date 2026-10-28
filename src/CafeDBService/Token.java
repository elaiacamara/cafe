package CafeDBService;

public sealed interface Token {
	public static enum Type{
		Word,
		SimpleQuotes,
		DoubleQuotes,
		SymbolSimpleQuotes,
		SymbolDoubleQuotes,
		SymbolBackTick,
		SymbolSemicolon,
		SymbolDot,
		SymbolComma,
		SymbolAssign,
		SymbolAdd,
        SymbolSub,
        SymbolMul,
        SymbolDiv,
        SymbolMod,
		SymbolOpenParenthesis,
		SymbolCloseParenthesis,
		SymbolOpenBrace,
		SymbolCloseBrace,
		None
	}
	public static Token of(char c) {
		return charToSymbol(c);
	}
	public static Token charToSymbol(char c) {
		return switch (c) {
		case ';' -> new SymbolSemicolon();
		case '=' -> new SymbolAssign();
		case '+' -> new SymbolAdd();
        case '-' -> new SymbolSub();
        case '*' -> new SymbolMul();
        case '/' -> new SymbolDiv();
        case '%' -> new SymbolMod();
        case '\'' -> new SymbolSimpleQuote();
		case '"' -> new SymbolDoubleQuote();
		case '`' -> new SymbolBackTick();
		case '(' -> new SymbolOpenParenthesis();
		case ')' -> new SymbolCloseParenthesis();
		case '.' -> new SymbolDot();
		case ',' -> new SymbolComma();
		case ':' -> new SymbolColon();
        case '{' -> new SymbolOpenBrace();
        case '}' -> new SymbolCloseBrace();
		default -> throw new RuntimeException("unhandled case");
		};
	}

	public static Type getType(Token token) {
		return switch (token) {
        case Word w                    -> Word.getType();
        case SimpleQuotes sq           -> SimpleQuotes.getType();
        case DoubleQuotes dq           -> DoubleQuotes.getType();
        case SymbolSimpleQuote s       -> SymbolSimpleQuote.getType();
        case SymbolDoubleQuote s       -> SymbolDoubleQuote.getType();
        case SymbolBackTick s          -> SymbolBackTick.getType();
        case SymbolSemicolon s         -> SymbolSemicolon.getType();
        case SymbolDot s               -> SymbolDot.getType();
        case SymbolComma s             -> SymbolComma.getType();
        case SymbolAssign s            -> SymbolAssign.getType();
        case SymbolAdd s               -> SymbolAdd.getType();
        case SymbolSub s               -> SymbolSub.getType();
        case SymbolMul s               -> SymbolMul.getType();
        case SymbolDiv s               -> SymbolDiv.getType();
        case SymbolMod s               -> SymbolMod.getType();
        case SymbolOpenParenthesis s   -> SymbolOpenParenthesis.getType();
        case SymbolCloseParenthesis s  -> SymbolCloseParenthesis.getType();
        case WhiteSpaces ws            -> WhiteSpaces.getType();
        case SymbolColon s             -> SymbolColon.getType();
        case CodeRule cr               -> CodeRule.getType();
        case BeginProgram s            -> BeginProgram.getType();
        case EndProgram s              -> EndProgram.getType();
        case SymbolOpenBrace s         -> SymbolOpenBrace.getType();
        case SymbolCloseBrace s        -> SymbolCloseBrace.getType();
        case None s                    -> None.getType();
        default                        -> Type.None;
    };
	}
	public static boolean parseTokenToType(Token token, Type type) {
		return getType(token) == type;/*
	    return switch (token) {
	        case Word w                    -> Word.getType() == type;
	        case SimpleQuotes sq           -> SimpleQuotes.getType() == type;
	        case DoubleQuotes dq           -> DoubleQuotes.getType() == type;
	        case SymbolSimpleQuote s       -> SymbolSimpleQuote.getType() == type;
	        case SymbolDoubleQuote s       -> SymbolDoubleQuote.getType() == type;
	        case SymbolBackTick s          -> SymbolBackTick.getType() == type;
	        case SymbolSemicolon s         -> SymbolSemicolon.getType() == type;
	        case SymbolDot s               -> SymbolDot.getType() == type;
	        case SymbolComma s             -> SymbolComma.getType() == type;
	        case SymbolAssign s            -> SymbolAssign.getType() == type;
	        case SymbolMath sm             -> SymbolMath.getType() == type;
	        case SymbolOpenParenthesis s   -> SymbolOpenParenthesis.getType() == type;
	        case SymbolCloseParenthesis s  -> SymbolCloseParenthesis.getType() == type;
	        case WhiteSpaces ws            -> WhiteSpaces.getType() == type;
	        case SymbolColon s             -> SymbolColon.getType() == type;
	        case CodeRule cr               -> CodeRule.getType() == type;
	        case BeginProgram s            -> BeginProgram.getType() == type;
	        case EndProgram s              -> EndProgram.getType() == type;
	        case None s                    -> None.getType() == type;
	        default                        -> false;
	    };*/
	}
	public String debug();
	public String value();
	
	final class Word implements Token {
        public String value;
        public Word(String value) { this.value = value;}
        //Token.getType()
        public static Type getType() {return Type.Word;}
        @Override
        public String debug() {
        	return String.format("Token: Word; value: [%s]", value);
        }
        @Override
        public String value() {
        	return value;
        }
    }

    final class SimpleQuotes implements Token {
        public String value;
        public SimpleQuotes(String value) { this.value = value; }
        
        public static Type getType() {return Type.SimpleQuotes;}
        @Override
        public String debug() {
        	return String.format("Token: SimpleQuotes; value: [%s]", value);
        }
        @Override
        public String value() {
        	return value;
        }
    }

    final class DoubleQuotes implements Token {
        public String value;
        public DoubleQuotes(String value) { this.value = value; }
        
        public static Type getType() {return Type.DoubleQuotes;}
        @Override
        public String debug() {
        	return String.format("Token: DoubleQuotes; value: [%]", value);
        }
        @Override
        public String value() {
        	return value;
        }
    }
    final class SymbolOpenBrace implements Token{
    	
        public static Type getType() {return Type.SymbolOpenBrace;}
        @Override
        public String debug() {
        	return "Token: }";
        }
        @Override
        public String value() {
        	return "}";
        }
    }
    final class SymbolCloseBrace implements Token{
    	
        public static Type getType() {return Type.SymbolCloseBrace;}
        @Override
        public String debug() {
        	return "Token: {";
        }
        @Override
        public String value() {
        	return "{";
        }
    }
    final class SymbolSimpleQuote implements Token{
    	
        public static Type getType() {return Type.SymbolSimpleQuotes;}
        @Override
        public String debug() {
        	return "Token: '";
        }
        @Override
        public String value() {
        	return "'";
        }
    }
    final class SymbolDoubleQuote implements Token{
    	
        public static Type getType() {return Type.SymbolDoubleQuotes;}
        @Override
        public String debug() {
        	return "Token: \"";
        }
        @Override
        public String value() {
        	return "\"";
        }
    }
    final class SymbolBackTick implements Token{
    	
        public static Type getType() {return Type.SymbolBackTick;}
        @Override
        public String debug() {
        	return "Token: `";
        }
        @Override
        public String value() {
        	return "`";
        }
    }
    final class SymbolSemicolon implements Token {
    	
        public static Type getType() {return Type.SymbolSemicolon;}
        @Override
        public String debug() {
        	return "Token: ;";
        }
        @Override
        public String value() {
        	return ";";
        }
    }
    final class SymbolDot implements Token {
    	
        public static Type getType() {return Type.SymbolDot;}
        @Override
        public String debug() {
        	return "Token: .";
        }
        @Override
        public String value() {
        	return ".";
        }
    }
    final class SymbolComma implements Token {
    	
        public static Type getType() {return Type.SymbolComma;}
        @Override
        public String debug() {
        	return "Token: ,";
        }
        @Override
        public String value() {
        	return ",";
        }
    }

    final class SymbolAssign implements Token {
    	
        public static Type getType() {return Type.SymbolAssign;}
        @Override
        public String debug() {
        	return "Token: =";
        }
        @Override
        public String value() {
        	return "=";
        }
    }

    final class SymbolAdd implements Token {
        
        public static Type getType() {return Type.SymbolAdd;}
        @Override
        public String debug() {
        	return "Token: Add";
        }
        @Override
        public String value() {
        	return "+";
        }
    }
    final class SymbolSub implements Token {
        
        public static Type getType() {return Type.SymbolSub;}
        @Override
        public String debug() {
        	return "Token: Subtract";
        }
        @Override
        public String value() {
        	return "-";
        }
    }
    final class SymbolMul implements Token {
        
        public static Type getType() {return Type.SymbolMul;}
        @Override
        public String debug() {
        	return "Token: Multiply";
        }
        @Override
        public String value() {
        	return "*";
        }
    }
    final class SymbolDiv implements Token {
        
        public static Type getType() {return Type.SymbolDiv;}
        @Override
        public String debug() {
        	return "Token: Divide";
        }
        @Override
        public String value() {
        	return "/";
        }
    }
    final class SymbolMod implements Token {
        
        public static Type getType() {return Type.SymbolMod;}
        @Override
        public String debug() {
        	return "Token: Modulo";
        }
        @Override
        public String value() {
        	return "%";
        }
    }

    final class SymbolOpenParenthesis implements Token {
    	
        public static Type getType() {return Type.SymbolOpenParenthesis;}
        @Override
        public String debug() {
        	return "Token: (";
        }
        @Override
        public String value() {
        	return "(";
        }
    }

    final class SymbolCloseParenthesis implements Token {
    	
        public static Type getType() {return Type.SymbolCloseParenthesis;}
        @Override
        public String debug() {
        	return "Token: )";
        }
        @Override
        public String value() {
        	return ")";
        }
    }

    final class WhiteSpaces implements Token {
        public String value;
        public WhiteSpaces(String value) { this.value = value; }
        
        public static Type getType() {return Type.SymbolBackTick;}
        @Override
        public String debug() {
        	return String.format("Token: Whitespaces; Value: [%s]", value);
        }
        @Override
        public String value() {
        	return value;
        }
    }

    final class SymbolColon implements Token {
    	
        public static Type getType() {return Type.SymbolBackTick;}
        @Override
        public String debug() {
        	return "Token: :";
        }
        @Override
        public String value() {
        	return ":";
        }
    }

    final class CodeRule implements Token{
    		public Tokenizer.BlockRule value;
    		public CodeRule(Tokenizer.BlockRule blockRule) {
    		
    		}
    	
        public static Type getType() {return Type.SymbolBackTick;}
        @Override
        public String debug() {
        	return "Token: CodeRule";
        }
        
        @Override
        public String value() {
        	return "code rule...";
        }
    }
    final class BeginProgram implements Token{
    	
        public static Type getType() {return Type.SymbolBackTick;}
        @Override
        public String debug() {
        	return "Token: Begin Program";
        }
        @Override
        public String value() {
        	return "begin program";
        }
    }
    final class EndProgram implements Token{
    	
        public static Type getType() {return Type.SymbolBackTick;}
        @Override
        public String debug() {
        	return "Token: EndProgram";
        }
        @Override
        public String value() {
        	return "end program";
        }
    }
    final class None implements Token {
    	
        public static Type getType() {return Type.SymbolBackTick;}
        @Override
        public String debug() {
        	return "Token: Unregistered token";
        }
        @Override
        public String value() {
        	return "None";
        }
    }
}