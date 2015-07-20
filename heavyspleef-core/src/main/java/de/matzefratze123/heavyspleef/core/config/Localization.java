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
		
		if (country == null && variant == null) {
			locale = new Locale(language);
		} else if (country != null && variant == null) {
			locale = new Locale(language, country);
		} else {
			locale = new Locale(language, country, variant);
		}
	}
	
	public Locale getLocale() {
		return locale;
	}

}
