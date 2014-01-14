/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.core.queue;

public class ArrayQueue<T> implements Queue<T> {
	
	private Object[] array;
	private int size;
	
	public ArrayQueue() {
		this(10);
	}
	
	public ArrayQueue(int initSize) {
		if (initSize <= 0)
			throw new IllegalArgumentException("initSize is lower 0 or less");
		this.array = new Object[initSize];
	}
	
	@Override
	public int add(T i) {
		//Lookup if queue is full
		if (isFull()) {
			//Extend array
			Object[] newArray = new Object[array.length * 2];
			
			//Transfer old values
			for (int in = 0; in < array.length; in++) {
				newArray[in] = array[in];
			}
			
			//Set the new array
			array = newArray;
		}
		
		int place = 0;
		
		//Push item into the first empty place
		for (int c = 0; c < array.length; c++) {
			if (array[c] == null) {
				//Wert hinten einfügen
				array[c] = i;
				place = c;
				break;
			}
		}
		
		++size;
		return place;
	}
	
	@SuppressWarnings("unchecked")
    static <T> T cast(Object item) {
        return (T) item;
    }
	
	@Override
	public T remove() {
		if (isEmpty()) {
			return null;
		}
		
		//Delete first entry
		T item = ArrayQueue.<T>cast(array[0]);
		array[0] = null;
		
		//Move up values left
		if (array.length > 1) {
			for (int i = 1; i < array.length; i++) {
				array[i - 1] = array[i];
				
				//Remove last entry
				if (array.length < i + 1) {
					array[i] = null;
				}
			}
		}
		
		--size;
		return item;
	}
	
	@Override
	public boolean contains(T item) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				continue;
			}
			
			if (item.equals(array[i]))
				return true;
		}
		
		return false;
	}
	
	@Override
	public T remove(T item) {
		T itemFound = null;
		
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null)
				continue;
			
			if (array[i].equals(item)) {
				itemFound = ArrayQueue.<T>cast(array[i]);
				array[i] = null;
				--size;
			}
		}
		
		return itemFound;
	}

	@Override
	public boolean isEmpty() {
		boolean empty = true;
		
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				empty = false;
				break;
			}
		}
		
		return empty;
	}
	
	private boolean isFull() {
		boolean full = true;
		
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				full = false;
				break;
			}
		}
		
		return full;
	}

	@Override
	public int size() {
		if (size < 0) {
			size = 0;
		}
		
		return size;
	}
	
	@Override
	public void clear() {
		array = new Object[10];
	}

}
