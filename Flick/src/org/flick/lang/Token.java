package org.flick.lang;

/**
 * The token class for the token parser to just store a token type and a 
 * @author Q
 *
 */
public class Token {
	/**
	 * The type enum to denote what type of token the token is
	 * @author Q
	 *
	 */
	public static enum Type {
		INT, FLOAT, STRING,
		PLUS, DASH, STAR, SLASH, PERCENT, CARET,
		BANG, PIPE,
		LESS_THAN, MORE_THAN, LESS_THAN_OR_EQUAL, MORE_THAN_OR_EQUAL, BOOL_EQUALS, NOT_EQUALS,
		OPEN_PARENS, CLOSE_PARENS, OPEN_BRACKETS, CLOSE_BRACKETS, OPEN_BRACES, CLOSE_BRACES,
		EQUALS_SIGN,
		COLON, SEMI_COLON, AT, COMMA, DOT,
		IDENTIFIER, KEYWORD,
		END_OF_FILE;
		
		public String verbose() {
			switch (this) {
			case AT:
				return "@";
			case BANG:
				return "!";
			case BOOL_EQUALS:
				return "==";
			case CARET:
				return "^";
			case CLOSE_BRACES:
				return "}";
			case CLOSE_BRACKETS:
				return "]";
			case CLOSE_PARENS:
				return ")";
			case COLON:
				return ":";
			case COMMA:
				return ",";
			case DASH:
				return "-";
			case DOT:
				return ".";
			case END_OF_FILE:
				return "End of file";
			case EQUALS_SIGN:
				return "=";
			case FLOAT:
				return "a Float";
			case IDENTIFIER:
				return "a Name";
			case INT:
				return "an Integer";
			case KEYWORD:
				return "a Keyword";
			case LESS_THAN:
				return "<";
			case LESS_THAN_OR_EQUAL:
				return "<=";
			case MORE_THAN:
				return ">";
			case MORE_THAN_OR_EQUAL:
				return ">=";
			case NOT_EQUALS:
				return "!=";
			case OPEN_BRACES:
				return "{";
			case OPEN_BRACKETS:
				return "[";
			case OPEN_PARENS:
				return "(";
			case PERCENT:
				return "%";
			case PIPE:
				return "|";
			case PLUS:
				return "+";
			case SEMI_COLON:
				return ";";
			case SLASH:
				return "/";
			case STAR:
				return "*";
			case STRING:
				return "a String";
			default:
				return "Unhandled enum Token(" + this.toString() + ")";
			}
		}
	}
	
	/**
	 * What type of token the token is
	 */
	public final Type type;
	/**
	 * The value of the token
	 */
	public final String value;
	
	/**
	 * The constructor method of the Token class 
	 * @param type the token's type
	 * @param value the token's value
	 */
	public Token(Type type, String value) {
		this.type = type;
		this.value = value;
	}
	
	/**
	 * Constructor of the Token class for no value, just a type (the value is usually implied)
	 * @param type the type of the token
	 */
	public Token(Type type) {
		this.type = type;
		this.value = null;
	}
	
	/**
	 * @param name the name to check if the token is a keyword of
	 * @return if the token is a keyword with the given name
	 */
	public boolean isKeyword(String name) {
		return this.type == Type.KEYWORD && this.value.equals(name);
	}
}