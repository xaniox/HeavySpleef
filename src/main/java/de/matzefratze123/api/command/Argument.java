package de.matzefratze123.api.command;

/**
 * Represents an argument
 * 
 * @author matzefratze123
 *
 * @param <T> The type of the argument
 */
public class Argument<T> {
	
	private T value;
	
	public Argument(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
}
