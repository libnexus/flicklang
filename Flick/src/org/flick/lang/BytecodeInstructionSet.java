package org.flick.lang;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * Equivalent to a "chunk" of code which contains all the instructions and important information for a set of
 * instructions
 * @author Q
 *
 */
public class BytecodeInstructionSet {
	/**
	 * The source code of the instruction set
	 */
	public final String source;
	/**
	 * The name of the instruction set
	 */
	public final String name;
	/**
	 * The stack of instructions which will be enumerated over
	 */
	private final Stack<BytecodeInstruction> instructions = new Stack<>();
	/**
	 * A list to track the type of name load operation that is needed at some point
	 */
	public final ArrayList<String> localNames = new ArrayList<>();
	/**
	 * A list to track if a local name overwrites a global name
	 */
	public final ArrayList<String> localOverwritesGlobalNames = new ArrayList<>();
	/**
	 * A list to track the argument names of the instruction set which is essentially to say names required to load properly
	 */
	public final ArrayList<String> argumentNames = new ArrayList<>();

	/**
	 * Constructor method for the BytecodeInstructionSet class
	 * @param source the source code that the instruction set came from
	 */
	public BytecodeInstructionSet(String source, String name) {
		this.source = source;
		this.name = name;
	}
	
	/**
	 * Adds a new instruction to the instruction set
	 * @param instruction the instruction to add
	 */
	public void addInstruction(BytecodeInstruction instruction) {
		this.instructions.add(instruction);
	}
	
	/**
	 * @param pointer the index of the stack
	 * @return the instruction at the given index
	 */
	public BytecodeInstruction getInstruction(int pointer) {
		return this.instructions.get(pointer);
	}
	
	/**
	 * @return getter method that wraps around the {@code this.instructions} for the iterator
	 */
	public Iterator<BytecodeInstruction> iterator() {
		return this.instructions.iterator();
	}
	
	/**
	 * @return the size of the instruction set which would point to the next instruction
	 */
	public int length() {
		return this.instructions.size();
	}
	
	/**
	 * Disassembles the instruction set
	 */
	public void disassemble() {
		BytecodeDisassembler.disassemble(this);
	}
}
