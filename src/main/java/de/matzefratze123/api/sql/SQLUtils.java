package de.matzefratze123.api.sql;

import java.util.Iterator;
import java.util.Map;

/**
 * Provides a simple util class for string handling
 * 
 * @author matzefratze123
 */
public class SQLUtils {

	/**
	 * A statement tick (e.g. '\'')
	 */
	static final char TICK = '\'';
	
	static String parseWhereClause(Map<?, ?> where) {
		if (where == null)
			return null;
		
		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = where.keySet().iterator();
		builder.append(" WHERE ");
		
		while(iter.hasNext()) {
			Object next = iter.next();
			Object value = where.get(next);
			
			builder.append(next).append("=").append(TICK).append(value).append(TICK);
			if (iter.hasNext())
				builder.append(" AND ");
		}
		

		return builder.toString();
	}
	
	/**
	 * Turns every element of the iterable into a friendly string which can be used in a statement
	 * 
	 * @param iterable The Iterable of elements
	 * @param seperator The seperator between every element
	 */
	public static String toFriendlyString(Iterable<?> iterable, String seperator) {
		Iterator<?> iter = iterable.iterator();
		StringBuilder builder = new StringBuilder();
		
		while(iter.hasNext()) {
			Object next = iter.next();
			
			builder.append(next);
			if (iter.hasNext())
				builder.append(seperator);
		}
		
		return builder.toString();
	}
	
	/**
	 * Turns every element of the Array into a friendly string which can be used in a statement
	 * 
	 * @param o The Array of elements
	 * @param seperator The seperator between every element
	 */
	public static String toFriendlyString(Object[] o, String seperator) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < o.length; i++) {
			Object next = o[i];
			
			builder.append(next);
			if (o.length >= i + 2)
				builder.append(seperator);
		}
		
		return builder.toString();
	}

}
