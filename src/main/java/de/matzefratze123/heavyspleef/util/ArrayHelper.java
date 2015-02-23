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
package de.matzefratze123.heavyspleef.util;

import java.util.ArrayList;

public class ArrayHelper {

	public static <T> ArrayList<T> mergeArrays(T[]... arrays) {

		ArrayList<T> list = new ArrayList<T>();

		for (T[] array : arrays) {
			if (array.length <= 0)
				continue;
			for (T t : array) {
				list.add(t);
			}
		}

		return list;
	}
	
	public static <T> boolean contains(T[] array, T element) {
		for (T t : array) {
			if (t == element) {
				return true;
			}
		}
		
		return false;
	}
	
	public static <T> int getIndex(T[] array, T element) {
		for (int i = 0; i < array.length; i++) {
			T t = array[i];
			
			if (t == element) {
				return i;
			}
		}
		
		return -1;
	}

}
