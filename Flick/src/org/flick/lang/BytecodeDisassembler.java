package org.flick.lang;

import java.util.Iterator;
import java.util.Stack;
import java.util.ArrayList;

public class BytecodeDisassembler {
	public static ArrayList<BytecodeInstructionSet> codeObjects = new ArrayList<>();
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public static void printFromSource(String sourceName, String source, int finalPos) {
		if (finalPos > source.length())
			System.exit(-10);
			
		String line = "";
		int lineNo = 1, lineCo = 0;
		int pos = 0;
		for (; pos != finalPos; pos++) {
			if (source.charAt(pos) == '\n') {
				line = "";
				lineNo++;
				lineCo = 0;
			} else {
				lineCo++;
				line += source.charAt(pos);
			}
		}
		
		for (; pos < source.length(); pos++) {
			if (source.charAt(pos) == '\n')
				break;
			line += source.charAt(pos);
		}
		

		
		System.out.println(String.format(ANSI_RED + "%s (%d:%d)\n    %s\n    %s" + ANSI_BLACK, sourceName, lineNo, lineCo, line, " ".repeat(lineCo - 1) + "*"));
	}
	
	public static void printStackError(String message, Stack<StackFrame> frameStack) {
		System.out.println(ANSI_RED + "Traceback (most recent call last)");
		for (StackFrame currFrame: frameStack) {
			printFromSource(currFrame.getChunkName(), currFrame.getChunkSource(), currFrame.getCurrentInstruction().actualPos);
		}
		System.out.println(ANSI_RED + message + ANSI_BLACK);
	}
	
	public static void disassemble(BytecodeInstructionSet instructions) {		
		Iterator<BytecodeInstruction> instructionsIterator = instructions.iterator();
		
		int instructionPointer = 0;
		BytecodeInstruction instruction;
		int lineNo = -1;
		boolean f = true;
		
		System.out.println(String.format(ANSI_GREEN + "Disassembly of code object %s at %d" + ANSI_BLACK, instructions.name, instructions.hashCode()));
		
		for (; instructionsIterator.hasNext();) {
			instruction = instructionsIterator.next();
			ArrayList<String> args = new ArrayList<>();
			
			for (Object arg : instruction.arguments) {
				args.add(disassembleToString(arg));
			}
			
			String mayBeJump = "";
			if (instruction.mayBeJump)
				mayBeJump = ">>";
			
			if (instruction.line != lineNo) {
				if (!f)
					System.out.println();
				
				lineNo = instruction.line;
				System.out.println(String.format(ANSI_GREEN + "%-4d    %-2s  %-4d  %-4d  %-20s    %-20s" + ANSI_BLACK, lineNo, mayBeJump, instructionPointer, instruction.linePos, instruction.opcode, String.join(", ", args)));
			} else {
				System.out.println(String.format(ANSI_GREEN + "        %-2s  %-4d  %-4d  %-20s    %-20s" + ANSI_BLACK, mayBeJump, instructionPointer, instruction.linePos, instruction.opcode, String.join(", ", args)));
			}
			instructionPointer++;
			f = false;
		}
		
		while (codeObjects.size() > 0) {
			System.out.println("\n");
			disassemble(codeObjects.remove(0));
		}
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
			codeObjects.add((BytecodeInstructionSet) object);
			return String.format("<code object %s @ %d>", instructions.name, instructions.hashCode());
		} else {
			return null;
		}
	}
}
