package org.flick.lang;
import org.flick.lang.BytecodeParser.Opcode;

/**
 * The bytecode instruction class which contains information for an instruction
 * @author Q
 *
 */
public class BytecodeInstruction {
	/**
	 * The opcode for the instruction
	 */
	public final Opcode opcode;
	/**
	 * The line that the instruction was parsed on
	 */
	public final int line;
	/**
	 * The position on the line that the instruction was parsed from
	 */
	public final int linePos;
	/**
	 * The position in the source code the instruction was at
	 */
	public final int actualPos;
	/**
	 * The amount of arguments for the object
	 */
	public final int argumentCount;
	/**
	 * The actual arguments of the object
	 */
	public final Object[] arguments;
	/**
	 * If the instruction could be a jump
	 */
	public final boolean mayBeJump;
	
	/**
	 * The constructor method for the BytecodeInstruction class 
	 * @param opcode see opcode field
	 * @param line see line field
	 * @param linePos see linePos field
	 * @param arguments see arguments field
	 * @param mayBeJump see mayBeJump field
	 */
	public BytecodeInstruction(Opcode opcode, int line, int linePos, int actualPos, Object[] arguments, boolean mayBeJump) {
		this.opcode = opcode;
		this.line = line;
		this.linePos = linePos;
		this.actualPos = actualPos;
		this.argumentCount = arguments.length;
		this.arguments = arguments;
		this.mayBeJump = mayBeJump;
	}
	
	
}
