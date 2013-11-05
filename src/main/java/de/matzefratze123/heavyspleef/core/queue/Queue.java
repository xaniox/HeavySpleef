package de.matzefratze123.heavyspleef.core.queue;

/**
 * Represents a queue for any value
 * 
 * @author matzefratze123
 *
 * @param <T> The type of elements held in this queue
 */
public interface Queue<T> {

	/**
	 * Adds an item to the queue at the end of it
	 * 
	 * @param i The item to add
	 * 
	 * @return Returns the place of this item
	 */
	public int add(T i);
	
	/**
	 * Removes oldest item from the queue and returns it
	 * 
	 * @return The item which was removed
	 */
	public T remove();
	
	/**
	 * Removes an item from this queue
	 */
	public T remove(T item);
	
	/**
	 * Checks if the queue contains an item
	 */
	public boolean contains(T item);
	
	/**
	 * Checks if the queue is empty
	 */
	public boolean isEmpty();
	
	/**
	 * Gets the size of this queue
	 */
	public int size();
	
	/**
	 * Clears the queue
	 */
	public void clear();
	
}
