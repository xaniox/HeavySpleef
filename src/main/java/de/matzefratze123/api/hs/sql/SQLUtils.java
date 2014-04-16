/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
package de.matzefratze123.api.hs.sql;

import java.util.Collection;
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
	static final char	TICK		= '\'';
	static final char	HIGH_TICK	= '`';

	static String parseWhereClause(Map<?, ?> where) {
		if (where == null)
			return null;

		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = where.keySet().iterator();
		builder.append(" WHERE ");

		while (iter.hasNext()) {
			Object next = iter.next();
			Object value = where.get(next);

			builder.append(next).append("=").append(TICK).append(value).append(TICK);
			if (iter.hasNext())
				builder.append(" AND ");
		}

		return builder.toString();
	}

	static String createParameterizedWhereClause(Collection<?> columns) {
		StringBuilder builder = new StringBuilder();
		builder.append(" WHERE ");

		Iterator<?> iter = columns.iterator();
		while (iter.hasNext()) {
			Object column = iter.next();

			builder.append(HIGH_TICK + column.toString() + HIGH_TICK + " = ?");

			if (iter.hasNext()) {
				builder.append(" AND ");
			}
		}

		return builder.toString();
	}

	/**
	 * Turns every element of the iterable into a friendly string which can be
	 * used in a statement
	 * 
	 * @param iterable
	 *            The Iterable of elements
	 * @param seperator
	 *            The seperator between every element
	 */
	public static String toFriendlyString(Iterable<?> iterable, String seperator) {
		Iterator<?> iter = iterable.iterator();
		StringBuilder builder = new StringBuilder();

		while (iter.hasNext()) {
			Object next = iter.next();

			builder.append(next);
			if (iter.hasNext())
				builder.append(seperator);
		}

		return builder.toString();
	}

	/**
	 * Turns every element of the Array into a friendly string which can be used
	 * in a statement
	 * 
	 * @param o
	 *            The Array of elements
	 * @param seperator
	 *            The seperator between every element
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
