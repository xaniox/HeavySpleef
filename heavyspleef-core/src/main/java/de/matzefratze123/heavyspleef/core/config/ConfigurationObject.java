package de.matzefratze123.heavyspleef.core.config;

import org.bukkit.configuration.Configuration;

public abstract class ConfigurationObject {
	
	public ConfigurationObject(Configuration config) {
		inflate(config);
	}
	
	public abstract void inflate(Configuration config);
	
}
