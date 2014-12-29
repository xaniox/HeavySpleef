/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
