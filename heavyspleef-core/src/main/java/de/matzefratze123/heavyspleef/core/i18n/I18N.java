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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.ParsedMessage.MessageVariable;

public class I18N {
	
	public static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
	private static final List<String> CLASSPATH_LOCALE_RESOURCES = Lists.newArrayList("locale_en_US.yml", "locale_de_DE.yml");
	private static final String FALLBACK_FILE = "locale_en_US.yml";
	
	static final Locale FALLBACK_LOCALE = Locale.US;
	static final String CLASSPATH_DIR = "/i18n/";
	
	private static I18N instance;
	
	private final YMLControl defaultControl;
	private final File localeDir;
	private final Logger logger;
	private Locale locale;
	private ResourceBundle bundle;
	
	public static void initialize(Locale locale, File localeDir, Logger logger) {
		instance = new I18N(locale, localeDir, logger);
	}
	
	public static void setDefaultLocale(Locale locale) {
		validateInstance();
		instance.setLocale(locale);
	}
	
	public static I18N getInstance() {
		validateInstance();
		return instance;
	}
	
	private static void validateInstance() {
		if (instance == null) {
			throw new IllegalStateException("I18N has not been initialized yet");
		}
	}
	
	private I18N(Locale locale, File localeDir, Logger logger) {
		this.defaultControl = new YMLControl(localeDir, CLASSPATH_DIR);
		this.localeDir = localeDir;
		this.logger = logger;
		
		this.locale = locale;
		
		load();
	}
	
	public void reload() {
		load();
	}
	
	private void load() {
		try {
			checkResourcesAndCopy();
		} catch (IOException e) {
			// Just inform as the YMLControl is going to load this file from the classpath
			logger.log(Level.WARNING, "Could not copy locale resource file, using classpath resource", e);
		}
		
		loadBundle();
	}
	
	private void loadBundle() {
		try {
			bundle = ResourceBundle.getBundle("locale", locale, defaultControl);
		} catch (MissingResourceException mre) {
			//Locale could not be found try to load from classpath
			YMLControl classpathControl = new YMLControl(localeDir, CLASSPATH_DIR, true);
			bundle = ResourceBundle.getBundle("locale", FALLBACK_LOCALE, classpathControl);
		}
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
		
		loadBundle();
	}
	
	private void checkResourcesAndCopy() throws IOException {
		for (String localeRes : CLASSPATH_LOCALE_RESOURCES) {
			File localeFile = new File(localeDir, localeRes);
			
			if (!localeFile.exists()) {
				URL localeResourceUrl = getClass().getResource(CLASSPATH_DIR + localeRes);
				
				HeavySpleef.copyResource(localeResourceUrl, localeFile);
			}
		}
		
		// Check if all messages exist, and add missing messages to the file e.g validate and replace
		for (File localeFile : localeDir.listFiles()) {
			URL classpathResource = getClass().getResource(CLASSPATH_DIR + localeFile.getName());
			if (classpathResource == null) {
				classpathResource = getClass().getResource(CLASSPATH_DIR + FALLBACK_FILE);
			}
			
			URLConnection connection = classpathResource.openConnection();
			connection.setUseCaches(false);
			
			StringBuilder fileBuilder;
			StringBuilder resourceBuilder;
			
			try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(localeFile), UTF8_CHARSET));
				 BufferedReader resourceReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), UTF8_CHARSET))) {
				
				fileBuilder = new StringBuilder();
				resourceBuilder = new StringBuilder();
				
				String read;
				while ((read = fileReader.readLine()) != null) {
					fileBuilder.append(read).append('\n');
				}
				
				while ((read = resourceReader.readLine()) != null) {
					resourceBuilder.append(read).append('\n');
				}
			}
			
			YamlConfiguration fileConfig = new YamlConfiguration();
			YamlConfiguration resourceConfig = new YamlConfiguration();
			
			try {
				fileConfig.loadFromString(fileBuilder.toString());
				resourceConfig.loadFromString(resourceBuilder.toString());
			} catch (InvalidConfigurationException e) {
				logger.log(Level.SEVERE, "Could not validate " + localeFile.getName() + ", ignoring file", e);
				continue;
			}
			
			boolean changesToCommit = false;
			
			for (String key : resourceConfig.getKeys(true)) {
				if (resourceConfig.isConfigurationSection(key)) {
					continue;
				}
				
				if (!fileConfig.contains(key)) {
					fileConfig.set(key, resourceConfig.get(key));
					changesToCommit = true;
				}
			}
			
			if (changesToCommit) {
				//Manually write the configuration as Bukkit's
				//configuration API does not provide custom encoding when
				//the standard charset does not support unicode...
				final YamlConfigurationOptions options = fileConfig.options();
				final YamlRepresenter representer = new YamlRepresenter();
				
				final DumperOptions dumperOptions = new DumperOptions();
				dumperOptions.setIndent(options.indent());
				dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				dumperOptions.setAllowUnicode(true);
				
				representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				
				final Yaml yaml = new Yaml(new YamlConstructor(), representer, dumperOptions);
				
				try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(localeFile), UTF8_CHARSET)) {
					yaml.dump(fileConfig.getValues(false), writer);
					writer.flush();
				}
			}
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
			List<MessageVariable> emptySet = Lists.newArrayList();
			return new ParsedMessage(message, emptySet);
		}
	}
	
	public String[] getStringArray(String key) {
		return bundle.getStringArray(key);
	}
	
}
