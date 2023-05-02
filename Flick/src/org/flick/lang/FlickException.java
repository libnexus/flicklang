package org.flick.lang;

import java.util.ArrayList;

/**
 * The flick exception class which is an error that will be handled by flick
 * @author Q
 *
 */
public class FlickException {
	/**
	 * The exception type class for creating exception types with an amount of existing exception 
	 * types that can be used by the program where needed
	 * @author Q
	 *
	 */
	public static class ExceptionType {
		/**
		 * An array list of known exception types
		 */
		private final static ArrayList<ExceptionType> exceptionTypes = new ArrayList<>();
		/**
		 * The name of the exception type
		 */
		public final String name;
		
		/**
		 * Runtime exception
		 */
		public static final ExceptionType RUNTIME_ERROR = newExceptionType("RuntimeError");
		/**
		 * Generic exception
		 */
		public static final ExceptionType GENERIC_EXCEPTION = newExceptionType("Exception");
		/**
		 * Error for division by zero
		 */
		public static final ExceptionType DIVISION_BY_ZERO = newExceptionType("DivisionByZeroError");
		/**
		 * Error for incorrect operational arguments
		 */
		public static final ExceptionType OPERATIONAL_ERROR = newExceptionType("OperationalError");
		
		
		/**
		 * Constructor method of the exception type class
		 * @param name the name of the exception type
		 */
		public ExceptionType(String name) {
			this.name = name;
		}
		
		/**
		 * Adds a new exception type to the logged exception types
		 * @param name the name of the exception
		 * @return the exception type created with the given name
		 */
		public static ExceptionType newExceptionType(String name) {
			ExceptionType exceptionType = new ExceptionType(name);
			exceptionTypes.add(exceptionType);
			return exceptionType;
		}
	}
	/**
	 * The error message
	 */
	public final String message;
	/**
	 * The error type
	 */
	public final ExceptionType error;
	/**
	 * The error arguments
	 */
	public final Object[] arguments;
	
	/**
	 * The constructor method of the flick exception class
	 * @param message the error message
	 * @param error the actual error type
	 * @param arguments the argment sof the error if required
	 */
	public FlickException(String message, ExceptionType error, Object... arguments) {
		this.message = message;
		this.error = error;
		this.arguments = arguments;
	}
}
