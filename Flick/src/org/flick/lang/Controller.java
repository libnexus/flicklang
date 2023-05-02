package org.flick.lang;

import java.util.HashMap;

import org.flick.lang.builtins.FlickDebugFunction;

/**
 * The controller / codex class for flick to manage plugins for flick
 * @author Q
 *
 */
public class Controller {
	/**
	 * The flick builtins
	 */
	public static final HashMap<String, FlickObject> builtins = new HashMap<>();
	
	static {
		builtins.put("debug", new FlickDebugFunction());
	}
	
	public static void makeModuleInstructionSet(BytecodeInstructionSet instructionSet) {
		instructionSet.localNames.addAll(builtins.keySet());
	}
	
	public static void makeModuleStackFrame(StackFrame frame) {
		frame.scope.symbols.putAll(builtins); 
	}
}
