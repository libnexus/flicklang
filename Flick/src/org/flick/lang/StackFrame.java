package org.flick.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

/**
 * Stack frame class tracked by the bytecode interpreter
 * @author Q
 *
 */
public class StackFrame {
	/**
	 * The set of instructions that the stack frame is interpreting
	 */
	private final BytecodeInstructionSet chunk;
	/**
	 * The length of the instruction set that is being interpreted
	 */
	public final int chunkLength;
	/**
	 * The internal instruction pointer
	 */
	public int instructionPointer;
	/**
	 * The object stack
	 */
	public final Stack<Object> objectStack = new Stack<>();
	/**
	 * The error handler stack to push a new goto for the error handler
	 */
	public final Stack<Integer> errorHandlerStack = new Stack<>();
	/**
	 * The scope that the frame takes place in
	 */
	public final ScopedTable scope;
	
	
	/**
	 * Constructor method for the stack frame which is used by the interpreter
	 * @param scopedTable 
	 */
	public StackFrame(BytecodeInstructionSet instructions, ScopedTable scope) {
		this.chunk = instructions;
		this.chunkLength = instructions.length();
		this.scope = scope;
	}
	
	public int prepareFrame(Object[] arguments, BytecodeInstructionSet instructionSet) {
		Iterator<String> argNames = instructionSet.argumentNames.iterator();
		Collections.reverse(Arrays.asList(arguments));
		for (Object argument : arguments) {
			if (!argNames.hasNext()) {
				return 1;
			}
			this.scope.symbols.put(argNames.next(), argument);
		}
		
		// It's a late call so the pointer needs to point to behind the first instruction
		this.instructionPointer = -1;
		
		if (argNames.hasNext()) {
			return 2;
		}
		
		return 0;
	}
	
	/**
	 * @param pointer the number pointer that points towards the instruction
	 * @return the instruction that sits at the given pointer index of the chunk
	 */
	public BytecodeInstruction getInstructionAtPointer(int pointer) {
		return this.chunk.getInstruction(pointer);
	}
	
	/**
	 * Gets the instruction at the current internal instruction pointer
	 */
	public BytecodeInstruction getCurrentInstruction() {
		return this.chunk.getInstruction(this.instructionPointer);
	}
	
	/**
	 * Gets the name of the instruction set
	 */
	public String getChunkName() {
		return this.chunk.name;
	}
	
	/**
	 * Gets the source code of the instruction set
	 */
	public String getChunkSource() {
		return this.chunk.source;
	}
}
