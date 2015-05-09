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
package de.matzefratze123.heavyspleef.core.i18n;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class YMLResourceBundle extends ResourceBundle {
	
	private static final char TRANSLATION_CHAR = '&';
	private YamlConfiguration config;
	
	public YMLResourceBundle(YamlConfiguration config) {
		this.config = config;
	}
	
	@Override
	protected Object handleGetObject(String key) {
		Object value = config.get(key);
		
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			int size = list.size();
			
			String[] array = new String[size];
			
			for (int i = 0; i < size; i++) {
				array[i] = ChatColor.translateAlternateColorCodes(TRANSLATION_CHAR, list.get(i).toString());
			}
			
			value = array;
		} else if (value instanceof String) {
			String str = (String) value;
			str = str.replace("\\n", "\n");
			value = ChatColor.translateAlternateColorCodes(TRANSLATION_CHAR, str);
		}
		
		return value;
	}

	@Override
	public Enumeration<String> getKeys() {
		return new YamlKeyEnumeration();
	}
	
	private class YamlKeyEnumeration implements Enumeration<String> {
		
		private Iterator<String> keyIterator;
		
		public YamlKeyEnumeration() {
			Set<String> allKeys = config.getKeys(true);
			Iterator<String> iterator = allKeys.iterator();
			
			while (iterator.hasNext()) {
				String key = iterator.next();
				
				if (config.isConfigurationSection(key)) {
					iterator.remove();
				}
			}
			
			keyIterator = allKeys.iterator();
		}
		
		@Override
		public boolean hasMoreElements() {
			return keyIterator.hasNext();
		}

		@Override
		public String nextElement() {
			return keyIterator.next();
		}
		
	}

}
