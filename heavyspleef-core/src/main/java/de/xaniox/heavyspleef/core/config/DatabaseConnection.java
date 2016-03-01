/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core.config;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnection {
	
	private String identifier;
	private Map<String, Object> properties;
	
	public DatabaseConnection(ConfigurationSection connectionSection, File basedir) {
		this.identifier = connectionSection.getName();
		this.properties = new HashMap<String, Object>();
		
		/* Save all properties from the section */
		for (String key : connectionSection.getKeys(false)) {
			Object value = connectionSection.get(key);
			if (value instanceof String) {
				//Replace {basedir} variable
				value = ((String)value).replace("{basedir}", basedir.getPath());
			}
			
			properties.put(key, value);
		}
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public Object get(String key) {
		return properties.get(key);
	}
	
	public String getString(String key) {
		Object value = get(key);
		return value instanceof String || value == null ? (String) value : value.toString();
	}
	
	public int getInt(String key) {
		Object value = get(key);
		
		int result = 0;
		if (value instanceof Integer) {
			result = ((Integer)value).intValue();
		} else {
			String strValue = value instanceof String || value == null ? (String) value : value.toString();
			
			try {
				result = Integer.parseInt(strValue);
			} catch (NumberFormatException nfe) {
				//Do nothing and return 0
			}
		}
		
		return result;
	}

	public Object getOrDefault(String key, Object defaultValue) {
		Object value = properties.get(key);
		if (value == null) {
			value = defaultValue;
		}
		
		return value;
	}
	
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
}