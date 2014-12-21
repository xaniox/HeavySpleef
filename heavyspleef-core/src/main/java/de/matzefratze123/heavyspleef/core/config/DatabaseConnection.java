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
