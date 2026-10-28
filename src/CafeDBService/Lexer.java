package CafeDBService;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiPredicate;

public class Lexer {
	
	enum CurrentToken{
		None,
		WhiteSpace,
		Word
	}
	//turns string into primitive tokens
	public static Vector<Token> lexer(String string) {
		Vector<Token> tokens = new Vector<>();
		//tokens.add(new Token.WhiteSpaces(" "));
		CurrentToken currentToken = CurrentToken.None;
		final String WHITESPACES = " \t\n";
		BiPredicate<Integer, String> isChar = (i, of) -> of.indexOf(string.charAt(i)) != -1;
		final String SYMBOLS = "'\"`+=-/*%(){}[];:,.";
		for (int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);
			if (isChar.test(i, WHITESPACES)) {
				if (currentToken == CurrentToken.WhiteSpace) {
					((Token.WhiteSpaces)tokens.elementAt(tokens.size() - 1)).value += c;
				}else {
					tokens.add(new Token.WhiteSpaces(String.valueOf(c)));
					currentToken = CurrentToken.WhiteSpace;
				}
			}else if (isChar.test(i, SYMBOLS)) {
				currentToken = CurrentToken.None;
				tokens.add(Token.charToSymbol(c));
			}else if (Character.isLetterOrDigit(c) || c == '_'){
				if (currentToken == CurrentToken.Word) {
					((Token.Word)tokens.elementAt(tokens.size() - 1)).value += c;
				}else {
					tokens.add(new Token.Word(String.valueOf(c)));
					currentToken = CurrentToken.Word;
				}
			}else {
				System.out.println("Unhandled char:[" + c + ']');
			}
		}
		return tokens;
	}
}
	
