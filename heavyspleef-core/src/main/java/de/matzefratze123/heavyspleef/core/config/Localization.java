package de.matzefratze123.heavyspleef.core.config;

import java.util.Locale;

import org.bukkit.configuration.ConfigurationSection;

public class Localization {
	
	public Locale locale;
	
	public Localization(ConfigurationSection section) {
		String localeString = section.getString("locale");
		String[] parts = localeString.split("_");

		String language = parts[0];
		String country = parts.length > 1 ? parts[1] : null;
		String variant = parts.length > 2 ? parts[2] : null;
		
		locale = new Locale(language, country, variant);
	}
	
	public Locale getLocale() {
		return locale;
	}

}
