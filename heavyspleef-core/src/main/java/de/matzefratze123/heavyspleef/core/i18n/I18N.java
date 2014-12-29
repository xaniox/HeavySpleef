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

import java.io.File;
import java.text.ParseException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.Localization;
import de.matzefratze123.heavyspleef.core.i18n.ParsedMessage.MessageVariable;

public class I18N {
	
	private static final Locale FALLBACK_LOCALE = new Locale("en");
	private static final String CLASSPATH_DIR = "/i18n/";
	
	private final YMLControl defaultControl;
	private final File localeDir;
	private final Logger logger;
	private Locale locale;
	private ResourceBundle bundle;
	
	public I18N(DefaultConfig config, File localeDir, Logger logger) {
		this.defaultControl = new YMLControl(localeDir, CLASSPATH_DIR);
		this.localeDir = localeDir;
		this.logger = logger;
		
		Localization localization = config.getLocalization();
		this.locale = localization.getLocale();
		
		loadBundle();
	}
	
	public void loadBundle() {
		try {
			bundle = ResourceBundle.getBundle("locale", locale, defaultControl);
		} catch (MissingResourceException mre) {
			//Locale could not be found try to load from classpath
			YMLControl classpathControl = new YMLControl(localeDir, CLASSPATH_DIR, true);
			bundle = ResourceBundle.getBundle("locale", FALLBACK_LOCALE, classpathControl);
		}
	}
	
	public String getString(String key) {
		return bundle.getString(key);
	}
	
	public ParsedMessage getVarString(String key) {
		String message = bundle.getString(key);
		
		try {
			return ParsedMessage.parseMessage(message);
		} catch (ParseException e) {
			//Report the exception
			logger.log(Level.SEVERE, "Illegal message \"" + message + "\"", e);
			
			//Return something to prevent expections
			Set<MessageVariable> emptySet = Sets.newHashSet();
			return new ParsedMessage(message, emptySet);
		}
	}
	
	public String[] getStringArray(String key) {
		return bundle.getStringArray(key);
	}
	
}
