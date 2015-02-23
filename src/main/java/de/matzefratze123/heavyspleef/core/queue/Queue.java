/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core.queue;

/**
 * Represents a queue for any value
 * 
 * @author matzefratze123
 * @param <T>
 *            The type of elements held in this queue
 */
public interface Queue<T> {

	/**
	 * Adds an item to the queue at the end of it
	 * 
	 * @param i
	 *            The item to add
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
