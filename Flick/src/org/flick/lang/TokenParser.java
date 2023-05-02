package org.flick.lang;
import org.flick.lang.Token.Type;;

/**
 * Extendable token parser class which adds basic functionality for generating tokens and token types
 * @author Q
 *
 */
public class TokenParser {
	/**
	 * The original source code
	 */
	public final String source;
	/**
	 * The current position in the source that the parser is at
	 */
	private int currPos = 0;
	/**
	 * The current line in the source that the parser is at
	 */
	private int currLine = 1;
	/**
	 * The current position on the line that the parser is at 
	 */
	private int currLinePos = 0;
	/**
	 * The position the last token was at
	 */
	private int lastTokenPos = 0;
	/**
	 * The current line the last parsed token is at
	 */
	private int lastTokenLine = 0;
	/**
	 * The current position on the line that the last parsed token is at
	 */
	private int lastTokenLinePos = 0;
	/**
	 * The current and previous token
	 */
	private Token currToken = null;
	
	/**
	 * The bytecode class constructor method 
	 * @param source the source code 
	 */
	public TokenParser(String source) {
		this.source = source;
		this.advance();
	}
	
	/**
	 * Advances to the next parsed token making sure that all inner values are changed
	 */
	public void advance() {
		this.lastTokenPos = this.currPos;
		this.lastTokenLine = this.currLine;
		this.lastTokenLinePos = this.currLinePos;
		this.currToken = this.nextToken();
	}
	
	/**
	 * Parses the next token in the source code
	 * @return the next parsed token
	 */
	private Token nextToken() {
		this.squeeze();
		int startPos = this.currPos;
		
		if (startPos >= this.source.length()) {
			return new Token(Type.END_OF_FILE);
		}
		
		if (startPos + 2 < this.source.length()) {
			String firstTwo = this.source.substring(this.currPos, this.currPos + 2);
			
			if (this.currPos > this.source.length())
				return new Token(Type.END_OF_FILE);
			
			if (firstTwo.equals("==")) {
				this.currLinePos += 2;
				this.currPos += 2;
				return new Token(Type.BOOL_EQUALS);
			} else if (firstTwo.equals("!=")) {
				this.currLinePos += 2;
				this.currPos += 2;
				return new Token(Type.NOT_EQUALS);
			} else if (firstTwo.equals("<=")) {
				this.currLinePos += 2;
				this.currPos += 2;
				return new Token(Type.LESS_THAN_OR_EQUAL);
			} else if (firstTwo.equals(">=")) {
				this.currLinePos += 2;
				this.currPos += 2;
				return new Token(Type.MORE_THAN_OR_EQUAL);
			} 
		}
		
		char first = this.source.charAt(this.currPos);
		this.currLinePos++;
		this.currPos++;
		
		if (Character.isJavaIdentifierStart(first)) {
			String name = Character.toString(first);
			for (; ;this.currPos++) {
				char curr = this.source.charAt(this.currPos);
				if (Character.isJavaIdentifierPart(curr)) {
					name += curr;
				} else {
					break;
				}
				this.currLinePos++;
			}
						
			if (this.identifierIsKeyword(name)) 
				return new Token(Type.KEYWORD, name);
			else
				return new Token(Type.IDENTIFIER, name);
		} else if (Character.isDigit(first)) {
			String number = Character.toString(first);
			for (; this.currPos < this.source.length(); this.currPos++) {
				char curr = this.source.charAt(this.currPos);
				if (Character.isDigit(curr)) {
					number += curr;
				} else if (curr == '_') {
					
				} else {
					break;
				}
				this.currLinePos++;
			}
			
			if (this.currPos < this.source.length() && this.source.charAt(this.currPos) == '.') {
				this.currPos++;
				for (; ;this.currPos++) {
					char curr = this.source.charAt(this.currPos);
					if (Character.isDigit(curr)) {
						number += curr;
					} else if (curr == '_') {
						
					} else {
						break;
					}
					this.currLinePos++;
				}
				return new Token(Type.FLOAT, number);
			} else {
				return new Token(Type.INT, number);
			}
		} else if (first == '"') {
			String string = "";
			for (; this.currPos < this.source.length(); this.currPos++) {
				char curr = this.source.charAt(this.currPos);
				if (curr == '"') {
					this.currPos++;
					this.currLinePos++;
					return new Token(Type.STRING, string);
				} else if (curr == '\n') {
					this.currLine += 1;
					this.currLinePos = 0;
				}	else {
					string += curr;
					this.currLinePos++;
				}
			}
			
			this.syntaxError(++startPos, String.format("Unclosed string at line"));
		}
		
		switch (first) {
		case '+':
			return new Token(Type.PLUS);
		case '-':
			return new Token(Type.DASH);
		case '*':
			return new Token(Type.STAR);
		case '/':
			return new Token(Type.SLASH);
		case '%':
			return new Token(Type.PERCENT);
		case '^':
			return new Token(Type.CARET);
		case '<':
			return new Token(Type.LESS_THAN);
		case '>':
			return new Token(Type.MORE_THAN);
		case '!':
			return new Token(Type.BANG);
		case '|':
			return new Token(Type.PIPE);
		case '=':
			return new Token(Type.EQUALS_SIGN);
		case '.':
			return new Token(Type.DOT);
		case ',':
			return new Token(Type.COMMA);
		case ':':
			return new Token(Type.COLON);
		case ';':
			return new Token(Type.SEMI_COLON);
		case '@':
			return new Token(Type.AT);
		case '(':
			return new Token(Type.OPEN_PARENS);
		case ')':
			return new Token(Type.CLOSE_PARENS);
		case '{':
			return new Token(Type.OPEN_BRACES);
		case '}':
			return new Token(Type.CLOSE_BRACES);
		case '[':
			return new Token(Type.OPEN_BRACKETS);
		case ']':
			return new Token(Type.CLOSE_BRACKETS);
		}
		
		this.syntaxError(startPos);
		
		return null;
	}
	
	/**
	 * Pinpoints a position in the source string
	 * @param finalPos The position to find in the source
	 * @param message the optional syntax error message to display
	 */
	public void syntaxError(int finalPos, String message) {
		if (finalPos > this.source.length())
			System.exit(-10);
			
		String line = "";
		int lineNo = 1, lineCo = 0;
		int pos = 0;
		for (; pos != finalPos; pos++) {
			if (this.source.charAt(pos) == '\n') {
				line = "";
				lineNo++;
				lineCo = 0;
			} else {
				lineCo++;
				line += this.source.charAt(pos);
			}
		}
		
		for (; pos < this.source.length(); pos++) {
			if (this.source.charAt(pos) == '\n')
				break;
			line += this.source.charAt(pos);
		}
		
		// TODO: fix line tracking
		
		if (lineCo == 0)
			lineCo = 1;
		
		if (message == null) {
			System.out.println(String.format(
					"SyntaxError (%d:%d)\n    %s\n    %s", 
					lineNo, lineCo, line, " ".repeat(lineCo - 1) + "*"));
		} else {
			System.out.println(String.format(
					"SyntaxError (%d:%d) %s\n    %s\n    %s", 
					lineNo, lineCo, message, line, " ".repeat(lineCo - 1) + "*"));
		}
		
		System.exit(pos);
	}
	
	/**
	 * 
	 */
	public void syntaxError(int finalPos) {
		this.syntaxError(finalPos, null);
	}
	
	/**
	 * Squeezes all whitespace in the source code, adjusting the current code position, line and line position
	 */
	private void squeeze() {
		char currChar;
		for (;this.currPos < this.source.length() && Character.isWhitespace((currChar = this.source.charAt(this.currPos)));) {
			if (currChar == '\n') {
				this.currLine++;
				this.currLinePos = 0;
			} 
			this.currLinePos++;
			this.currPos++;
		}
	}
	
	/**
	 * @return the current token
	 */
	public Token currToken() {
		return this.currToken;
	}
	
	/**
	 * Getter method for the currPos
	 * @return current position
	 */
	public int getCurrPos() {
		return this.currPos;
	}
	
	/**
	 * Getter method for lastTokenPos
	 * @return the last token actual position
	 */
	public int getLastTokenPos() {
		return this.lastTokenPos;
	}
	
	/**
	 * Getter method for lastTokenLine
	 * @return last token line number
	 */
	public int getlastTokenLine() {
		return this.lastTokenLine;
	}
	
	/**
	 * Getter method for lastTokenLinePos
	 * @return last token line position
	 */
	public int getlastTokenLinePos() {
		return this.lastTokenLinePos;
	}
	
	/**
	 * A method which should implemented by a parser if it requires keywords to be parsed
	 * @param name the name of the identifier to check
	 * @return if the given name is a keyword
	 */
	public boolean identifierIsKeyword(String name) {
		return false;
	}
	
	/**
	 * If the parser current token isn't the correct token, makes a call to syntax error
	 * @param type the token type to eat
	 * @return the eaten token
	 */
	public Token eat(Type type) {
		Token curr = this.currToken;

		if (curr.type == type) {
			this.advance();
			return curr;
		} else {
			this.syntaxError(this.currPos, "Expected \"" + type.verbose() + "\"");
			return null;
		}
	}
	
	/**
	 * Variant of the eat method to additionally check the value of the token
	 * @param type the type of token to eat
	 * @param value the value to eat
	 * @return the eaten token
	 */
	public Token eat(Type type, String value) {
		Token curr = this.currToken;
		if (curr.type == Type.KEYWORD && curr.value.equals(value)) {
			this.advance();
			return curr;
		} else {
			this.syntaxError(this.currPos, "Expected \"" + value + "\"");
			return null;
		}
	}
}
