package org.flick.lang;

import java.util.ArrayList;
import java.util.HashMap;

import org.flick.lang.BytecodeParser.Opcode;
import org.flick.lang.Token.Type;

/**
 * Bytecode compiler class to compile a decompiled bytecode file into useable bytecode instructions
 * @author Q
 *
 */
public class BytecodeCompilerParser extends TokenParser {
	public final HashMap<Integer, BytecodeInstructionSet> instructionMap = new HashMap<>();
	public final String sourceCode;
	
	public BytecodeCompilerParser(String compileSource, String originalSource) {
		super(compileSource);
		this.sourceCode = originalSource;
	}

	/**
	 * Disassembler and decompiler
	 * @param instructionSet the instruction set to decompile
	 * @return
	 */
	public static String disDecompile(BytecodeInstructionSet instructionSet) {
		String decompiled = String.format("\"%s\" %d (%s):\n", instructionSet.name, instructionSet.hashCode(), String.join(", ", instructionSet.argumentNames));
		
		int pointer = 0;
		
		for (BytecodeInstruction currInstruction = null; pointer < instructionSet.length(); pointer++) {
			currInstruction = instructionSet.getInstruction(pointer);
			ArrayList<String> args = new ArrayList<>();
			for (Object arg : currInstruction.arguments) {
				if (arg instanceof BytecodeInstructionSet) {
					decompiled = disDecompile((BytecodeInstructionSet) arg) + decompiled;
				}
				args.add(" " + disassembleToString(arg));
			}
			String argString;
			if (args.isEmpty())
				argString = "";
			else
				argString = String.join(", ", args);
			
			decompiled += String.format("    %d, %d, %d %s%s;\n", currInstruction.actualPos, currInstruction.line, currInstruction.linePos, currInstruction.opcode, argString);
		}
		
		return decompiled + "\n\n";
	}
	
	public static String disassembleToString(Object object) {
		if (object instanceof String) {
			return '"' + (String) object + '"';
		} else if (object instanceof Integer) {
			return ((Integer) object).toString();
		} else if (object instanceof Float) {
			return ((Float) object).toString();
		} else if (object instanceof Promise) {
			return disassembleToString(((Promise) object).getValue());
		} else if (object instanceof BytecodeInstructionSet) {
			BytecodeInstructionSet instructions = (BytecodeInstructionSet) object;
			return String.format("code @ %d", instructions.hashCode());
		} else {
			return null;
		}
	}
	
	public int compileModule() {
		int compiledInstructionSet = 0;
		while (this.currToken().type != Type.END_OF_FILE) {
			compiledInstructionSet = compileInstructionSet();
		}
		return compiledInstructionSet;
	}
	
	private int compileInstructionSet() {
		String instructionSetName = this.eat(Type.STRING).value;
		Integer instructionSetLocation = Integer.parseInt(this.eat(Type.INT).value);
				
		this.eat(Type.OPEN_PARENS);
		ArrayList<String> argumentNames = new ArrayList<>();
		if (this.currToken().type != Type.CLOSE_PARENS) {
			for (;;) {
				argumentNames.add(this.eat(Type.IDENTIFIER).value);
				if (this.currToken().type != Type.COMMA)
					break;
				this.advance();
			}
		}
		this.eat(Type.CLOSE_PARENS);
		this.eat(Type.COLON);
		
		BytecodeInstructionSet instructions = new BytecodeInstructionSet(this.sourceCode, instructionSetName);

		// parses individual instructions
		
		for (;;) {
			if (this.currToken().type == Type.STRING || this.currToken().type == Type.END_OF_FILE)
				break;
			
			
			int actualPos = Integer.parseInt(this.eat(Type.INT).value);
			this.eat(Type.COMMA);
			int line = Integer.parseInt(this.eat(Type.INT).value);
			this.eat(Type.COMMA);
			int linePos = Integer.parseInt(this.eat(Type.INT).value);
			Opcode opcode = Opcode.valueOf(this.eat(Type.IDENTIFIER).value);
			ArrayList<Object> args = new ArrayList<>();
			if (!(this.currToken().type == Type.SEMI_COLON)) {
				for (;;) {
					if (this.currToken().type == Type.INT) {
						args.add(Integer.parseInt(this.currToken().value));
						this.advance();
					} else if (this.currToken().type == Type.FLOAT) {
						args.add(Float.parseFloat(this.currToken().value));
						this.advance();
					} else if (this.currToken().type == Type.STRING) {
						args.add(this.currToken().value);
						this.advance();
					} else if (this.currToken().type == Type.KEYWORD) {
						this.advance();
						this.eat(Type.AT);
						args.add(this.instructionMap.get(Integer.parseInt(this.eat(Type.INT).value)));
					} else if (this.currToken().type == Type.COMMA) {
						this.advance();
						continue;
					} else {
						this.eat(Type.SEMI_COLON);
						break;
					}
				}
			} else {
				this.advance();
			}
			BytecodeInstruction instruction = new BytecodeInstruction(opcode, line, linePos, actualPos, args.toArray(), false);
			instructions.addInstruction(instruction);
		}
		
		this.instructionMap.put(instructionSetLocation, instructions);
		
		return instructionSetLocation;
	}
	
	@Override
	public boolean identifierIsKeyword(String name) {
		return name.equals("code");
	}
}
