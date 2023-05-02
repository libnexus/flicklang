package org.flick.lang;

import java.util.ArrayList;
import java.util.Stack;

/**
 * The bytecode interpreter class which is to say a stack manipulator which
 * reads the instructions formatted by the parser
 * 
 * @author Q
 *
 */
public class BytecodeInterpreter {
	/**
	 * The stack frames
	 */
	private final Stack<StackFrame> frameStack = new Stack<>();
	/**
	 * Reserved stack for when a snapshot of the frame stack needs to be stored
	 */
	private Stack<StackFrame> frameStackSnapshot = new Stack<>();
	/**
	 * Stack used to push new exceptions to be handled
	 */
	private final Stack<FlickException> errorStack = new Stack<>();
	/**
	 * Stack used to push exceptions which are in the process of being handled
	 */
	private final Stack<FlickException> errorHandlingStack = new Stack<>();
	/**
	 * State of whether each instruction should be debugged or not
	 */
	public boolean debugPerInstruction = false;
	
	/**
	 * The constructor method of the bytecode interpreter
	 */
	public BytecodeInterpreter() {
		
	}

	/**
	 * Manages the current instruction pointer
	 */
	public void execute() {
		for (; this.frameStack.peek().instructionPointer < this.frameStack.peek().chunkLength; this.frameStack
				.peek().instructionPointer++) {
			BytecodeInstruction currentInstruction = this.frameStack.peek().getCurrentInstruction();
			if (this.debugPerInstruction)
				this.debugInstruction(currentInstruction);

			switch (currentInstruction.opcode) {
			case BINARY_ADD:
				this.binaryAdd();
				break;
			case BINARY_MINUS:
				this.binaryMinus();
				break;
			case BINARY_MUL:
				this.binaryMul();
				break;
			case BINARY_DIV:
				this.binaryDiv();
				break;
			case LOAD_TRUE:
				this.loadConstant(true);
				break;
			case LOAD_FALSE:
				this.loadConstant(false);
				break;
			case LOAD_NONE:
				this.loadConstant(null);
				break;
			case LOAD_INT:
				this.loadConstant(currentInstruction.arguments[0]);
				break;
			case LOAD_STRING:
				this.loadConstant(currentInstruction.arguments[0]);
				break;
			case PRINT_TOP_X:
				this.printTopX((Integer) currentInstruction.arguments[0]);
				break;
			case POP_JUMP_IF_FALSE:
				this.popJumpIfFalse((Integer) getPossiblePromise(currentInstruction.arguments[0]));
				break;
			case JUMP_ABSOLUTE:
				this.frameStack
						.peek().instructionPointer = (Integer) getPossiblePromise(currentInstruction.arguments[0]) - 1;
				break;
			case CREATE_RAND_COINFLIP:
				// TODO: fix random coin flip always true
				this.loadConstant(getBooleanOf(Math.random()));
				break;
			case SETUP_ERROR_HANDLER:
				this.setupErrorHandler(currentInstruction.arguments);
				break;
			case POP_ERROR_HANDLER:
				this.frameStack.peek().errorHandlerStack.pop();
				break;
			case POP_ERROR:
				this.errorHandlingStack.add(this.errorStack.pop());
				break;
			case POP_FORGET_ERROR:
				this.errorHandlingStack.pop();
				break;
			case LOAD_NAME:
				this.loadName((String) currentInstruction.arguments[0]);
				break;
			case LOAD_NAME_FAST:
				this.loadNameFast((String) currentInstruction.arguments[0]);
				break;
			case STORE_NAME:
				this.frameStack.peek().scope.symbols.put((String) currentInstruction.arguments[0], this.popTop());
				break;
			case START_BREAKPOINT:
				this.startBreakpoint();
				break;
			case BINARY_EXPONENT:
				// TODO: implement binary exponent
				// this.binaryExponent();
				break;
			case BINARY_REMAINDER:
				// TODO: implement binary remainder
				// this.binaryRemainder();
				break;
			case CALL:
				this.call((Integer) currentInstruction.arguments[0]);
				break;
			case COMPARE_AND:
				// TODO: implement compare and
				// this.compareAnd();
				break;
			case COMPARE_EQUALS:
				// TODO: implement compare equals
				// this.compareEquals();
				break;
			case COMPARE_IS:
				// TODO: implement compare is
				// this.compareIs();
				break;
			case COMPARE_IS_NOT:
				// TODO: implement compare is not
				// this.compareIsNot();
				break;
			case COMPARE_LESS_THAN:
				// TODO: implement compare less than
				// this.compareLessThan();
				break;
			case COMPARE_LESS_THAN_OR_EQUAL:
				// TODO: implement compare less than or equal
				// this.compareLessThanOrEqual();
				break;
			case COMPARE_MORE_THAN:
				// TODO: implement compare more than
				// this.compareMoreThan();
				break;
			case COMPARE_MORE_THAN_OR_EQUAL:
				// TODO: implement compare more than or equal
				// this.compareMoreThanOrEqual();
				break;
			case COMPARE_NOT_EQUAL:
				// TODO: implement compare not equal
				// this.compareNotEqual();
				break;
			case COMPARE_OR:
				// TODO: implement compare or
				// this.comapreOr();
				break;
			case CREATE_FUNCTION:
				this.createFunction((String) currentInstruction.arguments[0],
						(BytecodeInstructionSet) currentInstruction.arguments[1]);
				break;
			case CREATE_LIST:
				// TODO: implement create list
				// this.createList();
				break;
			case CREATE_MATRIX:
				// TODO: implement create matrix
				// this.createMatrix((Integer) currentInstruction.arguments[0],
				// currentInstruction.arguments[1]);
				break;
			case CREATE_RAND_BETWEEN:
				// TODO: implement create random between
				// this.createRandBetween();
				break;
			case CREATE_RANGE:
				// TODO: implement create range
				// this.createRange();
				break;
			case CREATE_TUPLE:
				// TODO: implement create tuple
				// this.createTuple((Integer) currentInstruction.arguments[0]);
				break;
			case FORMAT_STRING:
				// TODO: implement format string
				// this.formatString((Integer) currentInstruction.arguments[0]);
				break;
			case GET_ATTR:
				// TODO: implement get attribute
				// this.getAttr((String) currentInstruction.arguments[0]);
				break;
			case GET_BOOL:
				this.addTop(getBooleanOf(this.popTop()));
				break;
			case GET_ITER:
				// TODO: implement get iter
				// this.getIter();
				break;
			case GET_MACRO_RANGE:
				// TODO: implement get macro range
				// this.macroRange();
				break;
			case INDEX:
				// TODO: implement index object
				// this.index((Integer) currentInstruction.arguments[0]);
				break;
			case LOAD_FLOAT:
				// TODO: implement load float
				// this.addTop((Double) currentInstruction.arguments[0]);
				break;
			case MACRO_CALL:
				// TODO: implement macro call
				// this.macroCall();
				break;
			case POP_TOP:
				this.popTop();
				break;
			case REMOVE_NAME:
				// TODO: implement remove name
				// this.removeName();
				break;
			case RETURN_X_VALUES:
				this.returnXValues((Integer) currentInstruction.arguments[0]);
				break;
			case STORE_INTO:
				// TODO: implement store into
				// this.storeInto((String) currentInstruction.arguments[0]);
				break;
			case UNARY_MINUS:
				// TODO: implement unary minus
				// this.unaryMinus();
				break;
			case UNARY_NOT:
				// TODO: implement unary not
				// this.unaryNot();
				break;
			case UNARY_PLUS:
				// TODO: implement unary plus
				// this.unaryPlus();
				break;
			default:
				this.errorStack.add(
						new FlickException(String.format("%s is not a recognised opcode", currentInstruction.opcode),
								FlickException.ExceptionType.RUNTIME_ERROR));
				break;
			}

			if (this.errorStack.size() > 0) {
				this.frameStackSnapshot.clear();
				this.frameStackSnapshot.addAll(this.frameStack);

				/**
				 * TODO: handle keeping an error on the error stack whilst handling it current
				 * solution is to pop it from the error stack, add it to the exception handling
				 * stack and finally pop it from the exception handling stack after being
				 * handled
				 */

				// Go through the stack in order of top to bottom without popping frames off
				boolean errorWillBeHandled = false;

				for (int i = this.frameStackSnapshot.size() - 1; i >= 0; i--) {
					// Remove the top frame from the main frame stack, it will still exist in the
					// snapshot
					StackFrame thisFrame = this.frameStackSnapshot.get(i);

					if (!thisFrame.errorHandlerStack.isEmpty()) {
						// There is an error handler at this level
						// The instruction pointer needs to be set to where it's pointing - 1 for the
						// execute increment
						this.frameStack.peek().instructionPointer = thisFrame.errorHandlerStack.pop() - 1;
						errorWillBeHandled = true;
						break;
					}

					this.frameStack.pop();
				}

				if (!errorWillBeHandled)
					this.reportError();
			}
		}
	}

	/**
	 * Adds the top two objects on the stack
	 */
	public void binaryAdd() {
		Object right = this.popTop();
		Object left = this.popTop();

		// Number constants

		if (left instanceof Integer && right instanceof Integer)
			this.addTop((Integer) left + (Integer) right);
		else if (left instanceof Integer && right instanceof Float)
			this.addTop((Integer) left + (Float) right);
		else if (left instanceof Float && right instanceof Integer)
			this.addTop((Float) left + (Integer) right);
		else if (left instanceof Float && right instanceof Float)
			this.addTop((Float) left + (Float) right);

		// Strings

		else if (left instanceof String && right instanceof String)
			this.addTop((String) left + (String) right);

		else
			this.errorStack
					.add(new FlickException(String.format("Cannot add %s to %s", getStringOf(right), getStringOf(left)),
							FlickException.ExceptionType.OPERATIONAL_ERROR));
	}

	/**
	 * Minuses the top two objects on the stack
	 */
	public void binaryMinus() {
		Object right = this.popTop();
		Object left = this.popTop();

		// Number constants

		if (left instanceof Integer && right instanceof Integer)
			this.addTop((Integer) left - (Integer) right);
		else if (left instanceof Integer && right instanceof Float)
			this.addTop((Integer) left - (Float) right);
		else if (left instanceof Float && right instanceof Integer)
			this.addTop((Float) left - (Integer) right);
		else if (left instanceof Float && right instanceof Float)
			this.addTop((Float) left - (Float) right);
		else
			this.errorStack.add(
					new FlickException(String.format("Cannot minus %s from %s", getStringOf(right), getStringOf(left)),
							FlickException.ExceptionType.OPERATIONAL_ERROR));
	}

	/**
	 * Multiplies the top two objects on the stack
	 */
	public void binaryMul() {
		Object right = this.popTop();
		Object left = this.popTop();

		// Number constants

		if (left instanceof Integer && right instanceof Integer)
			this.addTop((Integer) left * (Integer) right);
		else if (left instanceof Integer && right instanceof Float)
			this.addTop((Integer) left * (Float) right);
		else if (left instanceof Float && right instanceof Integer)
			this.addTop((Float) left * (Integer) right);
		else if (left instanceof Float && right instanceof Float)
			this.addTop((Float) left * (Float) right);

		else
			this.errorStack.add(
					new FlickException(String.format("Cannot multiply %s by %s", getStringOf(left), getStringOf(right)),
							FlickException.ExceptionType.OPERATIONAL_ERROR));
	}

	/**
	 * Divides the top two objects on the stack
	 */
	public void binaryDiv() {
		Object right = this.popTop();
		Object left = this.popTop();

		// Number constants

		if (left instanceof Integer && right instanceof Integer)
			if ((Integer) right == 0)
				this.errorStack.add(new FlickException(String.format("Cannot divide %s by 0", getStringOf(right)),
						FlickException.ExceptionType.DIVISION_BY_ZERO));
			else
				this.addTop((Integer) left / (Integer) right);
		else if (left instanceof Integer && right instanceof Float)
			if ((Integer) right == 0)
				this.errorStack.add(new FlickException(String.format("Cannot divide %s by 0", getStringOf(right)),
						FlickException.ExceptionType.DIVISION_BY_ZERO));
			else
				this.addTop((Integer) left / (Float) right);
		else if (left instanceof Float && right instanceof Integer)
			if ((Integer) right == 0)
				this.errorStack.add(new FlickException(String.format("Cannot divide %s by 0", getStringOf(right)),
						FlickException.ExceptionType.DIVISION_BY_ZERO));
			else
				this.addTop((Float) left / (Integer) right);
		else if (left instanceof Float && right instanceof Float)
			if ((Integer) right == 0)
				this.errorStack.add(new FlickException(String.format("Cannot divide %s by 0", getStringOf(right)),
						FlickException.ExceptionType.DIVISION_BY_ZERO));
			else
				this.addTop((Float) left / (Float) right);
		else
			this.errorStack.add(
					new FlickException(String.format("Cannot divide %s by %s", getStringOf(left), getStringOf(right)),
							FlickException.ExceptionType.GENERIC_EXCEPTION));
	}

	/**
	 * Loads a constant value to the top of the stack
	 * 
	 * @param constant the constant to load
	 */
	public void loadConstant(Object constant) {
		this.addTop(constant);
	}

	/**
	 * Prints the top x amount of objects on top of the stack
	 * 
	 * @param argumentsAmount the amount of items from the top to print
	 */
	public void printTopX(int argumentsAmount) {
		ArrayList<String> stringParts = new ArrayList<>();
		for (int i = 0; i < argumentsAmount; i++) {
			stringParts.add(0, getStringOf(this.popTop()));
		}
		System.out.println(String.join(" ", stringParts));
	}

	/**
	 * Jumps to the instruction pointer given as an argument if the top of the stack
	 * is false
	 * 
	 * @param jumpLocation the position in the instruction set to jump to 
	 */
	public void popJumpIfFalse(int jumpLocation) {
		boolean eval = getBooleanOf(this.popTop());
		if (eval)
			return;

		this.frameStack.peek().instructionPointer = jumpLocation - 1;
	}

	/**
	 * Adds a new error handler to the error handler stack of the current frame
	 * 
	 * @param arguments 0 -> the pointer in the current stack to jump to when there
	 *                  is an error
	 */
	public void setupErrorHandler(Object[] arguments) {
		this.frameStack.peek().errorHandlerStack.add((Integer) getPossiblePromise(arguments[0]));
	}

	/**
	 * Gets a given name that isn't local to the current frame
	 * 
	 * @param name the name to load
	 */
	public void loadName(String name) {
		Object value = this.frameStack.peek().scope.search(name);
		if (value != ScopedTable.NO_NAME) {
			this.addTop(value);
		} else {
			this.errorStack.add(new FlickException(String.format("\"%s\" is not defined", name),
					FlickException.ExceptionType.RUNTIME_ERROR));
		}
	}

	/**
	 * Gets a given name from the current frame
	 * 
	 * @param name the name to load
	 */
	public void loadNameFast(String name) {
		if (this.frameStack.peek().scope.symbols.containsKey(name)) {
			this.addTop(this.frameStack.peek().scope.symbols.get(name));
		} else {
			this.errorStack.add(new FlickException(String.format("\"%s\" is not defined", name),
					FlickException.ExceptionType.RUNTIME_ERROR));
		}
	}

	/**
	 * Starts an internal debugger allowing access to information about everything
	 * necessary for debugging the language or scripts
	 */
	public void startBreakpoint() {
		System.out.println("<--- flick debugger -->\n");

		// Print the frame stack

		System.out.println("<--- objects on stack --->");

		for (Object object : this.frameStack.peek().objectStack) {
			System.out.println(getStringOf(object));

		}

		System.out.println("\n<--- flick debugger -->");
	}

	/**
	 * Calls the object on the top of the stack after popping off call arguments
	 * 
	 * @param argumentsLength the amount of arguments to pop
	 */
	public void call(int argumentsLength) {
		Stack<Object> callArguments = new Stack<>();

		for (int i = 0; i < argumentsLength; i++) {
			callArguments.add(this.popTop());
		}

		Object callable = this.popTop();

		if (callable instanceof FlickObject) {
			((FlickObject) callable).FlickCall(this, callArguments.toArray());
		} else {
			this.errorStack.add(new FlickException(String.format("%s is not callable", getStringOf(callable)),
					FlickException.ExceptionType.RUNTIME_ERROR));
		}
	}

	/**
	 * Returns an amount of values from the top of the stack as either a single or a
	 * tuple of values
	 * 
	 * @param argumentsLength the amount of arguments to pop
	 */
	public void returnXValues(int argumentsLength) {
		// TODO: add tuple for multiple return values
		Stack<Object> values = new Stack<>();
		for (int i = 0; i < argumentsLength; i++) {
			values.add(this.frameStack.peek().objectStack.pop());
		}
		this.frameStack.pop();
		for (Object value : values) {
			this.frameStack.peek().objectStack.add(value);
		}
	}

	/**
	 * Creates a function
	 * 
	 * @param name            the name of the function
	 * @param codeObject      the code object
	 */
	public void createFunction(String name, BytecodeInstructionSet codeObject) {
		FlickObject functionObject = new FlickObject(FlickObject.ObjectType.FUNCTION);
		functionObject.instanceAttributes.put("Flick.Function.name", name);
		functionObject.instanceAttributes.put("Flick.Callable.instructionSet", codeObject);
		functionObject.instanceAttributes.put("Flick.Callable.parentScope", this.frameStack.peek().scope);

		this.frameStack.peek().scope.symbols.put(name, functionObject);
	}

	/**
	 * Converts a possible promise to a certain value
	 * 
	 * @param promise the possible promise
	 * @return the object from the promise or th eobject itself
	 */
	public static Object getPossiblePromise(Object promise) {
		if (promise instanceof Promise)
			return ((Promise) promise).getValue();
		else
			return promise;
	}

	/**
	 * Pops the top of the stack
	 * 
	 * @return top of the stack
	 */
	public Object popTop() {
		return this.frameStack.peek().objectStack.pop();
	}

	/**
	 * Adds an object to the top of the stack
	 * @param object th eobject to add on the top
	 */
	public void addTop(Object object) {
		this.frameStack.peek().objectStack.add(object);
	}

	/**
	 * Pops the top stack frame
	 * @return the popped stack frame
	 */
	public StackFrame popStackFrame() {
		return this.frameStack.pop();
	}

	/**
	 * Adds a new stack frame
	 * @param frame the frame to add on top
	 */
	public void addStackFrame(StackFrame frame) {
		this.frameStack.add(frame);
	}

	/**
	 * The main method for printing out the information on an instruction 
	 * @param instruction the instruction to print
	 */
	public void debugInstruction(BytecodeInstruction instruction) {
		ArrayList<String> args = new ArrayList<>();

		for (Object arg : instruction.arguments) {
			args.add(BytecodeDisassembler.disassembleToString(arg));
		}

		String mayBeJump = "";
		if (instruction.mayBeJump)
			mayBeJump = ">>";

		System.out.println(BytecodeDisassembler.ANSI_CYAN + String.format("%-20s %-2s  %-4d  %-4d  %-20s    %-20s",
				this.frameStack.peek().getChunkName(), mayBeJump, this.frameStack.peek().instructionPointer,
				instruction.linePos, instruction.opcode, String.join(", ", args)) + BytecodeDisassembler.ANSI_BLACK);
	}

	/**
	 * The interpreter's method for adding an error
	 * @param message the exception message of the exception
	 * @param exceptionType the type of the exception 
	 */
	public void addError(String message, FlickException.ExceptionType exceptionType) {
		this.errorStack.add(new FlickException(message, exceptionType));
	}

	/**
	 * The interpreter's way of handling errors
	 */
	public void reportError() {
		// TODO: add faulty instruction to FlickException

		FlickException exception = this.errorStack.pop();
		BytecodeDisassembler.printStackError(exception.error.name + "\n    " + exception.message,
				this.frameStackSnapshot);

		if (!this.errorHandlingStack.isEmpty()) {
			System.out.println(BytecodeDisassembler.ANSI_RED
					+ "\nThe above error occured whilst handling the below error\n" + BytecodeDisassembler.ANSI_BLACK);

			for (FlickException exceptionLeft : this.errorHandlingStack) {
				BytecodeDisassembler.printStackError(exceptionLeft.error.name + "\n    " + exception.message,
						frameStack);
			}
		}
	}

	/**
	 * Gets the string representation of an object value 
	 * @param object the object to get a string representation of
	 * @return a string representation of the given object, either a constant or
	 *         flick based object
	 */
	public static String getStringOf(Object object) {
		if (object == null)
			return "none";
		else if (object.equals(true))
			return "true";
		else if (object.equals(false))
			return "false";
		else if (object instanceof Integer)
			return ((Integer) object).toString();
		else if (object instanceof Float)
			return ((Float) object).toString();
		else if (object instanceof String)
			return (String) object;
		else if (object instanceof FlickObject)
			return ((FlickObject) object).FlickToString();
		else
			// TODO: implement for flick based objects
			return object.toString();
	}

	/**
	 * Get the boolean representation of an object
	 * @param object the object to get the boolean value of
	 * @return the boolean value of the given object
	 */
	public static boolean getBooleanOf(Object object) {
		if (object instanceof Integer)
			return ((Integer) object) != 0;
		else if (object instanceof Float)
			return ((Float) object) != 0;
		else if (object instanceof Double)
			return ((Double) object) != 0;
		else if (object instanceof String)
			return !((String) object).isEmpty();
		else if (object.equals(true))
			return true;
		else if (object.equals(false) || object.equals(null))
			return false;
		else
			// TODO: implement for flick based objects
			return true;
	}
}
