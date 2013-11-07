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
package de.matzefratze123.heavyspleef.core.flag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.util.Base64Helper;

public abstract class ListFlag<T> extends Flag<List<T>> {

	private static final String ELEMENT_SEPERATOR = ";";

	public ListFlag(String name, List<T> defaulte) {
		super(name, defaulte);
	}

	/**
	 * Serializes the current list </br></br> Please note that your list
	 * contents have to implement the serializeable interface
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String serialize(Object object) {
		List<T> list = (List<T>) object;

		StringBuilder builder = new StringBuilder();
		Iterator<T> iterator = list.iterator();
		
		while (iterator.hasNext()) {
			T element = iterator.next();
			
			Serializable s = (Serializable) element;
			String base64String = Base64Helper.toBase64(s);
			
			builder.append(base64String);
			
			if (iterator.hasNext()) {
				builder.append(ELEMENT_SEPERATOR);
			}
		}
		
		return getName() + ":" + builder.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2) {
			return null;
		}
		
		String[] elements = parts[1].split(ELEMENT_SEPERATOR);
		
		List<T> list = new ArrayList<T>();
		
		for (String e : elements) {
			Object fromBase64 = Base64Helper.fromBase64(e);
			
			T element = (T) fromBase64;
			list.add(element);
		}
		
		this.name = parts[0];
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> parse(Player player, String input, Object previousObject) {
		List<T> previousList;
		
		if (previousObject == null) {
			previousList = new ArrayList<T>();
		} else {
			//Create a copy of the list as we don't want to modificate the list directly (just parse and return it)
			previousList = new ArrayList<T>((List<T>) previousObject);
		}
		
		putElement(player, input, previousList);
		
		return previousList;
	}
	
	public abstract void putElement(Player player, String input, List<T> existing);

	@Override
	public FlagType getType() {
		return FlagType.LISTFLAG;
	}

}
