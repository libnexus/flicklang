package org.flick.lang;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import org.flick.lang.Token.Type;

/**
 * The bytecode parser to parse the source code
 * @author Q
 *
 */
public class BytecodeParser extends TokenParser {
	/**
	 * Opcode enum which includes all operations
	 * @author Q
	 *
	 */
	public static enum Opcode {
		BINARY_ADD, BINARY_MINUS, BINARY_MUL, BINARY_DIV, BINARY_REMAINDER, BINARY_EXPONENT,
		COMPARE_EQUALS, COMPARE_NOT_EQUAL, COMPARE_LESS_THAN, COMPARE_MORE_THAN, 
		COMPARE_LESS_THAN_OR_EQUAL, COMPARE_MORE_THAN_OR_EQUAL, COMPARE_IS, COMPARE_IS_NOT,
		COMPARE_AND, COMPARE_OR,
		UNARY_NOT, UNARY_PLUS, UNARY_MINUS, 
		LOAD_INT, LOAD_FLOAT, LOAD_STRING, LOAD_NAME, 
		LOAD_TRUE, LOAD_FALSE, LOAD_NONE,
		CREATE_FUNCTION, CREATE_LIST, CREATE_TUPLE,
		CREATE_RAND_COINFLIP, CREATE_RAND_BETWEEN,
		STORE_INTO, FORMAT_STRING, CREATE_RANGE, CREATE_MATRIX,
		START_BREAKPOINT,
		GET_ITER, GET_BOOL,
		CALL, MACRO_CALL, GET_MACRO_RANGE, INDEX, GET_ATTR,
		POP_TOP, RETURN_X_VALUES,
		POP_JUMP_IF_FALSE, JUMP_ABSOLUTE,
		PRINT_TOP_X, SETUP_ERROR_HANDLER, POP_ERROR_HANDLER, POP_ERROR, POP_FORGET_ERROR, LOAD_NAME_FAST,
		STORE_NAME, REMOVE_NAME
	}
	/**
	 * The stack of instruction sets
	 */
	private final Stack<BytecodeInstructionSet> instructionSetStack = new Stack<>();
	/**
	 * Words that the parser should class as keywords
	 */
	public static final ArrayList<String> keywords = new ArrayList<>();
	/**
	 * Whether or not the instruction could be jumped to, for disassembly
	 */
	private boolean nextMayBeJump = false;
	/**
	 * Stack used to push loop pointers
	 */
	private final Stack<Object> loopStartStack = new Stack<>(); 
	/**
	 * tack used to push loop closure pointers
	 */
	private final Stack<Object> loopEndStack = new Stack<>();
	
	private final HashMap<String, Integer> labels = new HashMap<>();
	
	/**
	 * Add the keywords to the keywords array
	 */
	static {
		keywords.addAll(Arrays.asList(
				"fun", 
				"if", "else", "try", "catch", "as",
				"for", "while", "continue", "break",
				"print", "var", "throw", "return",
				"and", "or", "is", "not",
				"true", "false", "none", "put", "stringf", "random", "range", "matrix", "breakpoint",
				// keywords for debug
				"FLDBG__label", "FLDBG__goto"));
	}
	
	
	/**
	 * The bytecode class constructor method 
	 * @param source the source code 
	 */
	public BytecodeParser(String source) {
		super(source);
	}
	
	/**
	 * Creates a new set of instructions to add to the instruction set stack
	 * @return the newly created instruction set
	 */
	public BytecodeInstructionSet newInstructionSet(String name) {
		BytecodeInstructionSet newInstructionSet = new BytecodeInstructionSet(this.source, name);
		this.instructionSetStack.add(newInstructionSet);
		return newInstructionSet;
	}
	
	@Override
	public boolean identifierIsKeyword(String name) { 
		return keywords.contains(name);
	}
	
	/**
	 * Pops the current instruction set off the top of the stack
	 * @return the popped off instruction set
	 */
	public BytecodeInstructionSet popInstructionSet() {
		return this.instructionSetStack.pop();
	}
	
	/**
	 * Adds an instruction to the current instruction set
	 */
	private void addInstruction(Opcode opcode, Object... arguments) {
		// TODO: add alternative method to add an instruction which originates from earlier in the source code
		this.instructionSetStack.peek()
			.addInstruction(new BytecodeInstruction(opcode, getlastTokenLine(), getlastTokenLinePos(), getLastTokenPos(), arguments, this.nextMayBeJump));
		this.nextMayBeJump = false;
	}
	
	/**
	 * Gets where the next instruction will be in the instruction set, -1 for the current instruction
	 * @return where the next instruction will be in the instruction set
	 */
	private int nextInstructionPointer() {
		return this.instructionSetStack.peek().length();
	}
	
	public HashMap<String, Object> parseOptions() {
		HashMap<String, Object> options = new HashMap<>();
		if (this.currToken().type != Type.IDENTIFIER || !this.currToken().value.equals("options"))
			return options;

		this.advance();
		this.eat(Type.OPEN_BRACES);
		for (; this.currToken().type == Type.IDENTIFIER; ) {
			String option = this.currToken().value;
			this.advance();
			this.eat(Type.COLON);
			if (this.currToken().isKeyword("true")) {
				options.put(option, true);
				this.advance();
			} else if (this.currToken().isKeyword("false")) {
				options.put(option, false);
				this.advance();
			} else if (this.currToken().type == Type.STRING) {
				options.put(option,  this.currToken().value); 
				this.advance();
			} else {
				this.syntaxError(this.getCurrPos());
			}
			if (this.currToken().type != Type.SEMI_COLON)
				break;
			else
				this.advance();
		}
		this.eat(Type.CLOSE_BRACES);
		return options;
	}
	
	/**
	 * Module	: Function*
	 */
	public void parseModule() {
		while (!(this.currToken().type == Type.END_OF_FILE)) {
			this.parseStatement();
		}
	}
	
	/**
	 * Function	: ("@" Expression)? "fun" t:Identifier "(" FunctionArguments ")" CodeBlock
	 */
	public void parseFunction() {
		this.eat(Type.KEYWORD, "fun");
		String name = this.eat(Type.IDENTIFIER).value;
		this.eat(Type.OPEN_PARENS);
		ArrayList<String> names = null;
		if (!(this.currToken().type == Type.CLOSE_PARENS))
			names = this.parseFunctionArguments();
		this.eat(Type.CLOSE_PARENS);
		BytecodeInstructionSet instructionSet = this.parseNewSetCodeBlock(this.instructionSetStack.peek().name + "." + "<" + name + ">", names);
		this.instructionSetStack.peek().localNames.add(name);
		this.addInstruction(Opcode.CREATE_FUNCTION, name, instructionSet);
	}
	
	/**
	 * FunctionArguments	: (t:Identifier ("," t:Identifier))+
	 * @return the amount of arguments
	 */
	public ArrayList<String> parseFunctionArguments() {
		ArrayList<String> names = new ArrayList<>();
		names.add(this.eat(Type.IDENTIFIER).value);
		for (; this.currToken().type == Type.COMMA;) {
			this.advance();
			names.add(this.eat(Type.IDENTIFIER).value);
		}
		return names;
	}
	
	/**
	 * Parses a new code block and returns the set of instructions that were parsed by the parser
	 * @return instruction set
	 */
	public BytecodeInstructionSet parseNewSetCodeBlock(String name, ArrayList<String> localNames) {
		this.newInstructionSet(name);
		this.instructionSetStack.peek().localNames.addAll(localNames);
		this.instructionSetStack.peek().argumentNames.addAll(localNames);
		this.parseCodeBlock();
		this.addInstruction(Opcode.LOAD_NONE);
		this.addInstruction(Opcode.RETURN_X_VALUES, 1);
		return this.popInstructionSet();
	}
	
	/**
	 * CodeBlock	: "{" Statement* "}"
	 */
	public void parseCodeBlock() {
		this.eat(Type.OPEN_BRACES);
		for (;;) {
			if (this.currToken().type == Type.CLOSE_BRACES) {
				this.advance();
				return;
			} else {
				this.parseStatement();
			}
		}
	}
	
	/**
	 * Statement	: Function
	 * 				| "if" "(" Expression ")" CodeBlock ("else" "if" "(" Expression ")" CodeBlock)* ("else" CodeBlock)?
	 * 				| "try" CodeBlock ("catch" (Expression ("as" t:Identifier)) CodeBlock)* ("finally" CodeBlock)?
	 * 				| "print" Expression ("," Expression)* ";"
	 * 				| "var" Identifier ("," Identifier)* "=" Expression ("," Expression)* ";"
	 * 				| "del" Identifier ("," Identifier)* ";"
	 * 				| "break" | "continue"
	 * 				| "return" (Expression ("," Expression)*)? ";"
	 * 				| CodeBlock
	 * 				| Expression ";"
	 */
	public void parseStatement() {
		if (this.currToken().isKeyword("fun")) {
			this.parseFunction();
			return;
		} else if (this.currToken().isKeyword("if")) {
			this.advance();
			this.eat(Type.OPEN_PARENS);
			this.parseExpression();
			this.eat(Type.CLOSE_PARENS);
			Promise jumpTo = new Promise();
			Promise end = new Promise();
			this.addInstruction(Opcode.POP_JUMP_IF_FALSE, jumpTo);
			this.parseCodeBlock();
			this.addInstruction(Opcode.JUMP_ABSOLUTE, end);
			for (;;) {
				if (this.currToken().isKeyword("else")) {
					this.advance();
					if (this.currToken().isKeyword("if")) {
						this.advance();
						jumpTo.fulfill(this.nextInstructionPointer());
						this.eat(Type.OPEN_PARENS);
						this.parseExpression();
						this.eat(Type.CLOSE_PARENS);
						jumpTo = new Promise();
						this.addInstruction(Opcode.POP_JUMP_IF_FALSE, jumpTo);
						this.nextMayBeJump = true;
						this.parseCodeBlock();
						this.addInstruction(Opcode.JUMP_ABSOLUTE, end);
						jumpTo.fulfill(this.nextInstructionPointer());
					} else {
						jumpTo.fulfill(this.nextInstructionPointer());
						this.nextMayBeJump = true;
						this.parseCodeBlock();
					}
				} else {
					break;
				}
			}
			jumpTo.fulfill(this.nextInstructionPointer());
			end.fulfill(this.nextInstructionPointer());
		} else if (this.currToken().isKeyword("try")) {
			this.advance();
			Promise end = new Promise(); 
			Promise nextErrorHandler = new Promise();
			this.addInstruction(Opcode.SETUP_ERROR_HANDLER, nextErrorHandler);
			this.parseCodeBlock();
			this.addInstruction(Opcode.POP_ERROR_HANDLER);
			this.addInstruction(Opcode.JUMP_ABSOLUTE, end);
			if (this.currToken().isKeyword("catch")) {
				// TODO: setup multiple error handlers with exception filtering
				this.advance();
				nextErrorHandler.fulfill(this.nextInstructionPointer());
				this.addInstruction(Opcode.POP_ERROR);
				this.parseCodeBlock();
				this.addInstruction(Opcode.POP_FORGET_ERROR);
			} else {
				nextErrorHandler.fulfill(this.nextInstructionPointer());
			}
			end.fulfill(this.nextInstructionPointer());
			
		} else if (this.currToken().isKeyword("for")) {
			
		} else if (this.currToken().isKeyword("while")) {
			this.advance();
			this.eat(Type.OPEN_PARENS);
			int boolPointer = this.nextInstructionPointer();
			this.parseExpression();
			this.eat(Type.CLOSE_PARENS);
			Promise endOfWhile = new Promise();
			this.addInstruction(Opcode.POP_JUMP_IF_FALSE, endOfWhile);
			this.loopStartStack.add(boolPointer);
			this.loopEndStack.add(endOfWhile);
			this.parseCodeBlock();
			this.loopStartStack.pop();
			this.loopEndStack.pop();
			this.addInstruction(Opcode.JUMP_ABSOLUTE, boolPointer);
			endOfWhile.fulfill(this.nextInstructionPointer());
			this.nextMayBeJump = true;
		} else if (this.currToken().isKeyword("continue")) {
			if (this.loopStartStack.isEmpty())
				this.syntaxError(getCurrPos(), "continue outside of loop");
			
			this.advance();
			this.eat(Type.SEMI_COLON);
			this.addInstruction(Opcode.JUMP_ABSOLUTE, this.loopStartStack.peek());
		} else if (this.currToken().isKeyword("break")) {
			if (this.loopEndStack.isEmpty()) 
				this.syntaxError(getCurrPos(), "break outside of loop");
			
			this.advance();
			this.eat(Type.SEMI_COLON);
			this.addInstruction(Opcode.JUMP_ABSOLUTE, this.loopEndStack.peek());
		} else if (this.currToken().isKeyword("print")) {
			this.advance();
			
			int length = 0;
			if (this.currToken().type == Type.SEMI_COLON) {
				this.addInstruction(Opcode.PRINT_TOP_X, 1);
				return;
			}
			
			length++;
			this.parseExpression();
			
			for (; this.currToken().type == Type.COMMA; length++) {
				this.advance();
				this.parseExpression();
			}
			
			this.eat(Type.SEMI_COLON);
			this.addInstruction(Opcode.PRINT_TOP_X, length);
			
		} else if (this.currToken().isKeyword("var")) {
			this.advance();
			Stack<String> names = new Stack<>();
			names.add(this.eat(Type.IDENTIFIER).value);
			
			for (;this.currToken().type == Type.COMMA;) {
				this.advance();
				names.add(this.eat(Type.IDENTIFIER).value);
			}
			
			this.eat(Type.EQUALS_SIGN);
			
			this.parseExpression();
			
			for (;this.currToken().type == Type.COMMA;) {
				this.advance();
				this.parseExpression();
			}
			
			while (!names.isEmpty()) {
				String thisName = names.pop();
				// Note: can use localNames.removeAll to remove all occurrences of name in delete statement
				this.instructionSetStack.peek().localNames.add(thisName);
				this.addInstruction(Opcode.STORE_NAME, thisName);
			}
			
			this.eat(Type.SEMI_COLON);
			
		} else if (this.currToken().isKeyword("del")) {
			this.advance();
			String name = this.eat(Type.IDENTIFIER).value;
			this.instructionSetStack.peek().localNames.remove(name);
			this.addInstruction(Opcode.REMOVE_NAME, name);
			
			while (this.currToken().type == Type.COMMA) {
				this.advance();
				name = this.eat(Type.IDENTIFIER).value;
				this.instructionSetStack.peek().localNames.remove(name);
				this.addInstruction(Opcode.REMOVE_NAME, name);
			}
			
		} else if (this.currToken().isKeyword("return")) {
			this.advance();
			int length = 0;
			
			if (this.currToken().type == Type.SEMI_COLON) {
				this.advance();
				this.addInstruction(Opcode.RETURN_X_VALUES, 0);
				return;
			}
			
			length = 1;
			this.parseExpression();
			for (; this.currToken().type == Type.COMMA; length++) {
				this.advance();
				this.parseExpression();
			}
			
			this.eat(Type.SEMI_COLON);
			this.addInstruction(Opcode.RETURN_X_VALUES, length);
				
		} else if (this.currToken().isKeyword("FLDBG__label")) {
			this.advance();
			String labelName = this.eat(Type.IDENTIFIER).value;
			this.labels.put(labelName, this.nextInstructionPointer());
			this.eat(Type.COLON);
			
		} else if (this.currToken().isKeyword("FLDBG__goto")) {
			this.advance();
			String labelName = this.eat(Type.IDENTIFIER).value;
			if (!this.labels.containsKey(labelName))
				this.syntaxError(getCurrPos());
			this.eat(Type.SEMI_COLON);
			this.addInstruction(Opcode.JUMP_ABSOLUTE, this.labels.get(labelName));
			
		} else if (this.currToken().type == Type.OPEN_BRACES) {
			this.parseCodeBlock();
		} else {
			this.parseExpression();
			this.eat(Type.SEMI_COLON);
			this.addInstruction(Opcode.POP_TOP, 1);
		}
	}
	
	/**
	 * Expression	: AndOr 
	 */
	public void parseExpression() {
		this.parseAndOr();
	}
	
	/**
	 * AndOr	: Equality (("and" | "or") Equality)*
	 */
	public void parseAndOr() {
		this.parseEquality();
		
		for (;;) {
			if (this.currToken().isKeyword("and")) {
				this.advance();
				this.parseEquality();
				this.addInstruction(Opcode.COMPARE_AND);
			} else if (this.currToken().isKeyword("or")) {
				this.advance();
				this.parseEquality();
				this.addInstruction(Opcode.COMPARE_OR);
			} else {
				break;
			}
		}
	}
	
	/**
	 * Equality	: PlusMinus (("==" | "!=" | "<=" | ">=" | "<" | ">" | "is" | "is not") PlusMinus)*
	 */
	public void parseEquality() {
		this.parsePlusMinus();
		
		for (;;) {
			if (this.currToken().type == Type.BOOL_EQUALS) {
				this.advance();
				this.parsePlusMinus();
				this.addInstruction(Opcode.COMPARE_EQUALS);
			} else if (this.currToken().type == Type.NOT_EQUALS) {
				this.advance();
				this.parsePlusMinus();
				this.addInstruction(Opcode.COMPARE_NOT_EQUAL);
			} else if (this.currToken().type == Type.LESS_THAN) {
				this.advance();
				this.parsePlusMinus();
				this.addInstruction(Opcode.COMPARE_LESS_THAN);
			} else if (this.currToken().type == Type.MORE_THAN) {
				this.advance();
				this.parsePlusMinus();
				this.addInstruction(Opcode.COMPARE_MORE_THAN);
			} else if (this.currToken().type == Type.MORE_THAN_OR_EQUAL) {
				this.advance();
				this.parsePlusMinus();
				this.addInstruction(Opcode.COMPARE_MORE_THAN_OR_EQUAL);
			} else if (this.currToken().type == Type.LESS_THAN_OR_EQUAL) {
				this.advance();
				this.parsePlusMinus();
				this.addInstruction(Opcode.COMPARE_LESS_THAN_OR_EQUAL);
			} else if (this.currToken().isKeyword("is")) {
				this.advance();
				if (this.currToken().isKeyword("not")) {
					this.advance();
					this.parsePlusMinus();
					this.addInstruction(Opcode.COMPARE_IS_NOT);
				} else {
					this.parsePlusMinus();
					this.addInstruction(Opcode.COMPARE_IS);
				}
			} else {
				break;
			}
		}
	}
	
	/**
	 * PlusMinus	: MulDiv (("+" | "-") MulDiv)*
	 */
	public void parsePlusMinus() {
		this.parseMulDiv();
		
		for (;;) {
			if (this.currToken().type == Type.PLUS) {
				this.advance();
				this.parseMulDiv();
				this.addInstruction(Opcode.BINARY_ADD);
			} else if (this.currToken().type == Type.DASH) {
				this.advance();
				this.parseMulDiv();
				this.addInstruction(Opcode.BINARY_MINUS);
			} else {
				break;
			}
		}
	}
	
	/**
	 * MulDiv		: RemainderPow (("*" | "/") RemainderPow)*
	 */
	public void parseMulDiv() {
		this.parseRemainderPow();
		
		for (;;) {
			if (this.currToken().type == Type.STAR) {
				this.advance();
				this.parseRemainderPow();
				this.addInstruction(Opcode.BINARY_MUL);
			} else if (this.currToken().type == Type.SLASH) {
				this.advance();
				this.parseRemainderPow();
				this.addInstruction(Opcode.BINARY_DIV);
			} else {
				break;
			}
		}
	}
	
	/**
	 * RemainderPow	:	UnaryPre (("%" | "^") UnaryPre)*
	 */
	public void parseRemainderPow() {
		this.parseUnaryPre();
		
		for (;;) {
			if (this.currToken().type == Type.PERCENT) {
				this.advance();
				this.parseUnaryPre();
				this.addInstruction(Opcode.BINARY_REMAINDER);
			} else if (this.currToken().type == Type.CARET) {
				this.advance();
				this.parseUnaryPre();
				this.addInstruction(Opcode.BINARY_EXPONENT);
			} else {
				break;
			}
		}
	}
	
	/**
	 * UnaryPre		: ("not" | "+" | "-")* UnaryPost
	 */
	public void parseUnaryPre() {
		Stack<Opcode> lateOpStack = new Stack<>();
		for (;;) {
			if (this.currToken().isKeyword("not")) {
				lateOpStack.add(Opcode.UNARY_NOT);
			} else if (this.currToken().type == Type.PLUS) {
				lateOpStack.add(Opcode.UNARY_PLUS);
			} else if (this.currToken().type == Type.DASH) {
				lateOpStack.add(Opcode.UNARY_MINUS);
			} else {
				break;
			}
			this.advance();
		}
		
		this.parseUnaryPost();
		
		for (Opcode op : lateOpStack) {
			this.addInstruction(op);
		}
	}
	
	/**
	 * UnaryPost	: Atom (( "(" CallArguments ")" ) 
	 * 					 	| ( "["(Expression ("," Expression)*)? "]" ) 
	 * 						| ( "[" Expression ":" Expression "]"
	 * 						| ( "." t:Identifier ))*
	 */
	public void parseUnaryPost() {
		this.parseAtom();
		
		for (;;) {
			if (this.currToken().type == Type.OPEN_PARENS) {
				this.advance();
				if (this.currToken().type == Type.CLOSE_PARENS) {
					this.advance();
					this.addInstruction(Opcode.CALL, 0);
					continue;
				} 
				int length = this.parseCallArguments();
				this.eat(Type.CLOSE_PARENS);
				this.addInstruction(Opcode.CALL, length);
				continue;
			} else if (this.currToken().type == Type.OPEN_BRACKETS) {
				this.advance();
				if (this.currToken().type == Type.CLOSE_BRACKETS) {
					this.advance();
					this.addInstruction(Opcode.MACRO_CALL);
					continue;
				}
				this.parseExpression();
				if (this.currToken().type == Type.COLON) {
					this.advance();
					this.parseExpression();
					this.eat(Type.CLOSE_BRACKETS);
					this.addInstruction(Opcode.GET_MACRO_RANGE);
					continue;
				} 
				int length = 1;
				for (; this.currToken().type == Type.COMMA; length++) {
					this.advance();
					this.parseExpression();
				}
				this.eat(Type.CLOSE_BRACKETS);
				this.addInstruction(Opcode.INDEX, length);
				continue;
			} else if (this.currToken().type == Type.DOT) {
				this.advance();
				this.addInstruction(Opcode.GET_ATTR, this.eat(Type.IDENTIFIER).value);
				continue;
			} else {
				break;
			}
		}
	}
	
	/**
	 * CallArguments	: (Expression ("," Expression))?
	 * @return the amount of call arguments passed
	 */
	public int parseCallArguments() {
		int length = 1;
		this.parseExpression();
		for (; this.currToken().type == Type.COMMA; length++) {
			this.advance();
			this.parseExpression();
		}
		return length;
	}
		
	/**
	 * Atom			: t:Integer
	 * 				| t:Float
	 * 				| t:String
	 * 				| t:Identifier
	 * 				| ( "[" (Expression ("," Expression)*)? "]")
	 * 				| ( "(" Expression ")" )
	 * 				| ( "(" (Expression ("," Expression)*)? ")" )
	 * 				| "stringf" "(" Expression ("," Expression)+ ")"
	 * 				| "randint "(" (Expression ":" Expression)? ")"
	 * 				| "range" "(" Expression ":" Expression ")"
	 * 				| "matrix" "[" t:Integer=int1 "," t:Integer=int2 "]" ("|" Expression{int2} "|"){int1}
	 * 				| "breakpoint "(" Expression? ")"
	 * 
	 * Integer		: adds LOAD_INT instruction with integer value
	 * Float		: adds LOAD_FLOAT instruction with double value
	 * String		: adds LOAD_STRING instruction with string value 
	 * Identifier	: adds LOAD_NAME instruction with name string
	 * List			: adds LOAD_LIST instruction with a length of items to pop from the stack and add to the list
	 * Tuple		: adds LOAD_TUPLE instruction with list method
	 * Matrix		: takes two integers int1, int2 and parses int1 rows of vectors with int2 expressions
	 * 				| adds LOAD_MATRIX instruction with rows and columns as operands
	 * Breakpoint	: adds START_BREAKPOINT instruction with an optional expression to stay on the stack while 
	 * 				| doing the breakpoint if it needs to happen in the middle of an expression
	 * 
	 * @implNote a double is locally referred to by flick as a float
	 */
	public void parseAtom() {
		Token atom = this.currToken();
		this.advance();
		int topPos = this.getCurrPos();
		
		switch (atom.type) {
		case INT:
			this.addInstruction(Opcode.LOAD_INT, Integer.parseInt(atom.value));
			return;
		case FLOAT:
			this.addInstruction(Opcode.LOAD_FLOAT, Double.parseDouble(atom.value));
			return;
		case STRING:
			this.addInstruction(Opcode.LOAD_STRING, atom.value);
			return;
		case IDENTIFIER: {
			if (this.instructionSetStack.peek().localNames.contains(atom.value)) {
				this.addInstruction(Opcode.LOAD_NAME_FAST, atom.value);
				return;
			} else {
				this.addInstruction(Opcode.LOAD_NAME, atom.value);
				return;
			}
		}
		case OPEN_BRACKETS: {
			if (this.currToken().type == Type.CLOSE_BRACKETS) {
				this.advance();
				this.addInstruction(Opcode.CREATE_LIST, 0);
				return;
			}
			int length = 1;
			this.parseExpression();
			for (; this.currToken().type == Type.COMMA; length++) {
				this.advance();
				if (this.currToken().type == Type.CLOSE_BRACKETS)
					break;
				this.parseExpression();
			}
			this.eat(Type.CLOSE_BRACKETS);
			this.addInstruction(Opcode.CREATE_LIST, length);
			return;
		}
		case OPEN_PARENS: {
			this.parseExpression();
			if (this.currToken().type == Type.CLOSE_PARENS) {
				this.advance();
				return;
			}
			
			int length = 1;
			for (; this.currToken().type == Type.COMMA; length++) {
				this.advance();
				if (this.currToken().type == Type.CLOSE_PARENS)
					break;
				this.parseExpression();
			}
			this.eat(Type.CLOSE_PARENS);
			this.addInstruction(Opcode.CREATE_TUPLE, length);
			return;
		} 
		case KEYWORD: {
			switch (atom.value) {
			case "true": {
				this.addInstruction(Opcode.LOAD_TRUE);
				return;
			}
			case "false": {
				this.addInstruction(Opcode.LOAD_FALSE);
				return;
			}
			case "none": {
				this.addInstruction(Opcode.LOAD_NONE);
				return;
			}
			case "put": {
				this.eat(Type.OPEN_PARENS);
				String name = this.eat(Type.IDENTIFIER).value;
				this.eat(Type.COMMA);
				this.parseExpression();
				this.eat(Type.CLOSE_PARENS);
				this.addInstruction(Opcode.STORE_INTO, name);
				return;
			}
			case "stringf": {
				int length = 0;
				this.eat(Type.OPEN_PARENS);
				String string = this.eat(Type.STRING).value;
				for (; this.currToken().type == Type.COMMA; length++) {
					this.advance();
					this.parseExpression();
				}
				this.addInstruction(Opcode.FORMAT_STRING, string, length);
				return;
			}
			case "random": {
				this.eat(Type.OPEN_PARENS);
				if (this.currToken().type == Type.CLOSE_PARENS) {
					this.advance();
					this.addInstruction(Opcode.CREATE_RAND_COINFLIP);
					return;
				}
				this.parseExpression();
				this.eat(Type.COLON);
				this.parseExpression();
				this.eat(Type.CLOSE_PARENS);
				this.addInstruction(Opcode.CREATE_RAND_BETWEEN);
				return;
			}
			case "range": {
				this.eat(Type.OPEN_PARENS);
				this.parseExpression();
				this.eat(Type.COLON);
				this.parseExpression();
				this.eat(Type.CLOSE_PARENS);
				this.addInstruction(Opcode.CREATE_RANGE);
				return;
			}
			case "matrix": {
				this.eat(Type.OPEN_BRACKETS);
				int rows = Integer.parseInt(this.eat(Type.INT).value);
				if (rows < 1) {
					this.syntaxError(this.getCurrPos(), "can't have less than 1 rows");
				}
				this.eat(Type.COMMA);
				int cols = Integer.parseInt(this.eat(Type.INT).value);
				if (cols < 1) {
					this.syntaxError(this.getCurrPos(), "can't have less than 1 columns");
				}
				this.eat(Type.CLOSE_BRACKETS);
				
				for (int irows = rows; irows > 0; irows--) {
					this.eat(Type.PIPE);
					for (int icols = cols; icols > 0; icols--) {
						this.parseExpression();
					}
					this.eat(Type.PIPE);
				}
				this.addInstruction(Opcode.CREATE_MATRIX, rows, cols);
				return;
			}
			case "breakpoint": 
				this.eat(Type.OPEN_PARENS);
				if (!(this.currToken().type == Type.CLOSE_PARENS))
					this.parseExpression();
				else
					this.addInstruction(Opcode.LOAD_NONE);
				this.eat(Type.CLOSE_PARENS);
				this.addInstruction(Opcode.START_BREAKPOINT);
				return;
			}
		}
		default:
			this.syntaxError(topPos);
		}
	}
}

