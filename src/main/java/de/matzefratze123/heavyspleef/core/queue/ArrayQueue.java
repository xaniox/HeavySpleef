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
	
	public int add(T i) {
		//Schauen ob Queue voll ist
		if (isFull()) {
			//Queue erweitern
			Object[] newArray = new Object[array.length * 2];
			
			//Alte Werte übertragen
			for (int in = 0; in < array.length; in++) {
				newArray[in] = array[in];
			}
			
			//Array neu setzen
			array = newArray;
		}
		
		int place = 0;
		
		//Alles durchgehen und schauen ob bereits Wert enthalten ist
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
	
	public T remove() {
		if (isEmpty()) {
			return null;
		}
		
		//Ersten Eintrag löschen
		T item = ArrayQueue.<T>cast(array[0]);
		array[0] = null;
		
		//Werte nach links nachrücken lassen
		if (array.length > 1) {
			for (int i = 1; i < array.length; i++) {
				array[i - 1] = array[i];
				
				//Letzten Eintrag nullen
				if (array.length < i + 1) {
					array[i] = null;
				}
			}
		}
		
		--size;
		return item;
	}
	
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
