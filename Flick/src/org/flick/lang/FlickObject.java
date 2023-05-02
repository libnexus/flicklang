package org.flick.lang;

import java.util.HashMap;

/**
 * The flick object class
 * The flick object class is an object that is able to be influenced by manipulated by flick bytecode with the following
 * features:
 * 	- method lookup 
 *  - types
 * @author Q
 *
 */
public class FlickObject {
	public static class ObjectType {
		/**
		 * The object type from which all types inherit from
		 */
		public static final ObjectType OBJECT = new ObjectType("object", null);
		/**
		 * The type type object Object -> Type
		 */
		public static final ObjectType TYPE = new ObjectType("type", OBJECT);
		/**
		 * The callable type object Object -> Callable
		 */
		public static final ObjectType CALLABLE = new ObjectType("callable", OBJECT);
		/**
		 * The function type object Object -> Callable -> Function
		 */
		public static final ObjectType FUNCTION = new ObjectType("function", CALLABLE);
		/**
		 * The boolean type Object -> Boolean
		 */
		public static final ObjectType BOOLEAN = new ObjectType("boolean", OBJECT);
		/**
		 * The none type Object -> Boolean -> None
		 */
		public static final ObjectType NONE = new ObjectType("nonetype", BOOLEAN);
		/**
		 * The scalar/number type Object -> Scalar 
		 */
		public static final ObjectType SCALAR = new ObjectType("scalar", OBJECT);
		/**
		 * The integer type Object -> Scalar -> Integer
		 */
		public static final ObjectType INTEGER = new ObjectType("integer", SCALAR);
		/**
		 * The float type Object -> Scalar -> Float
		 */
		public static final ObjectType FLOAT = new ObjectType("float", SCALAR);
		/**
		 * The string object type Object -> String
		 */
		public static final ObjectType STRING = new ObjectType("string", OBJECT);
		
		/**
		 * The name of the object type
		 */
		public final String name;
		/**
		 * The type from which the actual type inherits from, if there is no parent 
		 */
		public final ObjectType parent;
		/**
		 * A set of methods for the object to operate with
		 */
		public final HashMap<String, BytecodeInstructionSet> methods = new HashMap<>();
		/**
		 * A set of static attributes for the object to manipulate
		 */
		public final HashMap<String, Object> staticAttributes = new HashMap<>();
		
		/**
		 * The obejct type constructor method 
		 * @param name the name of the type
		 * @param parent the parent type of the type if any
		 */
		public ObjectType(String name, ObjectType parent) {
			this.name = name;
			this.parent = parent;
		}
				
		/**
		 * The flick to string method for converting an object to something representative of it inside of a string
		 * @param object
		 * @return
		 */
		public String FlickToString(FlickObject self) {
			// TODO: implement for custom objects
			return String.format("<%s at %d>", this.name, self.hashCode());
		}
		
		/**
		 * The flick call method to start calling stuff
		 * @param interpreter
		 * @param arguments
		 */
		public void FlickCall(FlickObject self, BytecodeInterpreter interpreter, Object[] arguments) {
			if (!self.FlickUtilInstanceOf(CALLABLE)) {
				interpreter.addError(String.format("%s is not a callable", self.FlickToString()), FlickException.ExceptionType.RUNTIME_ERROR);
				return;
			} 
			
			BytecodeInstructionSet instructionSet = (BytecodeInstructionSet) self.instanceAttributes.get("Flick.Callable.instructionSet");
			ScopedTable parentScope = (ScopedTable) self.instanceAttributes.get("Flick.Callable.parentScope");
			
			StackFrame callFrame = new StackFrame(instructionSet, new ScopedTable(parentScope));
			
			int callFramePrepStatus = callFrame.prepareFrame(arguments, instructionSet);
			
			if (callFramePrepStatus == 1)
				interpreter.addError(String.format("%s was given too many arguments", self.FlickToString()), FlickException.ExceptionType.RUNTIME_ERROR);
			else if (callFramePrepStatus == 2)
				interpreter.addError(String.format("%s wasn't given enough arguments", self.FlickToString()), FlickException.ExceptionType.RUNTIME_ERROR);

			interpreter.addStackFrame(callFrame);
		}
		
	}
	
	public static final FlickObject TRUE = new FlickObject(ObjectType.BOOLEAN);
	
	/**
	 * The object type that the object belongs to, handles the methods of the object
	 */
	public final ObjectType type;
	/**
	 * The object's instance attributes which are manipulated internally by flick
	 */
	public final HashMap<String, Object> instanceAttributes = new HashMap<>();
	
	/**
	 * The flick object constructor method
	 * @param type the type that the object is to perform call operations on
	 */
	public FlickObject(ObjectType type) {
		this.type = type;
	}
	
	public boolean FlickUtilInstanceOf(ObjectType type) {
		ObjectType next = this.type;
		for (;;) {
			if (next == type)
				return true;
			
			if (next.parent != null)
				next = next.parent;
			else
				return false;
		}
	}
	
	/**
	 * Wraps around the object type's to string method passing the object as an argument
	 * @return
	 */
	public String FlickToString() {
		return this.type.FlickToString(this);
	}
	
	/**
	 * Wraps around the object type's call method
	 * @param interpreter the interpreter
	 * @param arguments the call arguments
	 */
	public void FlickCall(BytecodeInterpreter interpreter, Object[] arguments) {
		this.type.FlickCall(this, interpreter, arguments);
	}
}
