package org.flick.lang;

/**
 * The late utility class to simply wrap around a value that will then be set later on
 * @author Q
 *
 */
public class Promise {
	/**
	 * The value of the object that has been promised to be not null
	 */
	private Object value;
	
	/**
	 * Empty constructor method
	 */
	public Promise() {
		
	}
	
	/**
	 * Sets the value to a new value
	 * @param value new value
	 */
	public void fulfill(Object value) {
		if (this.value == null)
			this.value = value;
	}
	
	/**
	 * Getter method for the value contained in the promise
	 * @return value object
	 */
	public Object getValue() {
		return this.value;
	}
}
