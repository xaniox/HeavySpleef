package de.matzefratze123.api.sql;

import java.util.Iterator;
import java.util.Map;

class SQLUtils {

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

}
