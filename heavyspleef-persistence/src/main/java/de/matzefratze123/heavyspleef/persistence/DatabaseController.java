package de.matzefratze123.heavyspleef.persistence;

import java.util.List;

public interface DatabaseController {
	
	public static final int NO_LIMIT = -1;
	
	public void update(Object object);
	
	public void update(Object object, Object cookie);
	
	public void update(Object[] objects);
	
	public void update(Object[] objects, Object cookie);
	
	public void update(Iterable<?> iterable);
	
	public void update(Iterable<?> iterable, Object cookie);
	
	public List<Object> query(String key, Object value, Object cookie, String orderBy, int limit);
	
	public List<Object> query(String key, Object value, String orderBy, int limit);
	
	public Object queryUnique(String key, Object value);
	
	public Object queryUnique(String key, Object value, Object cookie);
	
	public int delete(Object obj);
	
}
