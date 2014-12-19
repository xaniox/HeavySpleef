package de.matzefratze123.heavyspleef.persistence;

import java.util.List;

public interface DatabaseController {
	
	public static final int NO_LIMIT = -1;
	
	public default void update(Object object) {
		update(object, null);
	}
	
	public void update(Object object, Object cookie);
	
	public default void update(Object[] objects) {
		update(objects, null);
	}
	
	public default void update(Object[] objects, Object cookie) {
		for (Object obj : objects) {
			update(obj);
		}
	}
	
	public default void update(Iterable<?> iterable) {
		update(iterable, null);
	}
	
	public default void update(Iterable<?> iterable, Object cookie) {
		iterable.forEach(obj -> update(obj));
	}
	
	public List<Object> query(String key, Object value, Object cookie, String orderBy, int limit);
	
	public default List<Object> query(String key, Object value, String orderBy, int limit) {
		return query(key, value, orderBy, limit);
	}
	
	public default Object queryUnique(String key, Object value) {
		return queryUnique(key, value, null);
	}
	
	public default Object queryUnique(String key, Object value, Object cookie) {
		List<?> result = query(key, value, cookie, null, NO_LIMIT);
		return !result.isEmpty() ? result.get(0) : null;
	}
	
}
