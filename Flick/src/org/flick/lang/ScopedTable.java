package org.flick.lang;

import java.util.HashMap;

/**
 * A scope symbol table which can store a reference to outer tables so that the stack isn't relied on so much
 * @author Q
 *
 */
public class ScopedTable {
	/**
	 * The symbol table hash map
	 */
	public final HashMap<String, Object> symbols = new HashMap<>();
	/**
	 * The possible parent scope of the current table
	 */
	public final ScopedTable parent;
	
	public static final Object NO_NAME = new Object();
	
	/**
	 * The constructor method for the scope table
	 * @param parent the possible parent of the table
	 */
	public ScopedTable(ScopedTable parent) {
		this.parent = parent;
	}
	
	/**
	 * Searches for a named value
	 * @param name
	 * @return
	 */
	public Object search(String name) {
		if (symbols.containsKey(name))
			return symbols.get(name);
		else if (this.parent != null)
			return this.parent.search(name);
		else
			return NO_NAME;
	}
}
