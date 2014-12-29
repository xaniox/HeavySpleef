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
package de.matzefratze123.heavyspleef.core.config;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class DatabaseConnection {
	
	private String identifier;
	private Map<String, Object> properties;
	
	public DatabaseConnection(ConfigurationSection connectionSection) {
		this.identifier = connectionSection.getName();
		
		/* Save all properties from the section */
		for (String key : connectionSection.getKeys(false)) {
			properties.put(key, connectionSection.get(key));
		}
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public Object get(Object key) {
		return properties.get(key);
	}

	public Object getOrDefault(Object key, Object defaultValue) {
		Object value = properties.get(key);
		if (value == null) {
			value = defaultValue;
		}
		
		return value;
	}
	
}
