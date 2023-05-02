package org.flick.lang.builtins;

import org.flick.lang.BytecodeInterpreter;
import org.flick.lang.FlickException.ExceptionType;
import org.flick.lang.FlickObject;

/**
 * The flick debug function which extends a flick object and can't be subclassed because each of 
 * it's instances will act the same so there's no reason to subclass it
 * @author Q
 *
 */
public final class FlickDebugFunction extends FlickObject {
	/**
	 * The constructor method to pass to the super the function type because the object
	 * is a function
	 */
	public FlickDebugFunction() {
		super(ObjectType.FUNCTION);
	}
	
	@Override
	public void FlickCall(BytecodeInterpreter interpreter, Object[] arguments) {
		if (arguments.length != 0) {
			interpreter.addError("The debug function takes 0 arguments", ExceptionType.RUNTIME_ERROR);
		}
		
		interpreter.debugPerInstruction = !interpreter.debugPerInstruction;
		
		interpreter.addTop(null);
	}
}
