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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.i18n.I18N.LoadingMode;

public class YMLControl extends ResourceBundle.Control {
	
	private static final String YML_FORMAT = "yml";
	
	private File localeDir;
	private String classpathDir;
	private LoadingMode mode;
	
	public YMLControl(File localeDir, String classpathDir, LoadingMode mode) {
		this.localeDir = localeDir;
		this.classpathDir = classpathDir;
		this.mode = mode;
	}
	
	@Override
	public List<String> getFormats(String baseName) {
		if (baseName == null) {
			throw new NullPointerException();
		}
		
		return Arrays.asList(YML_FORMAT);
	}
	
	@Override
	public List<Locale> getCandidateLocales(String baseName, Locale locale) {
		List<Locale> candidates = Lists.newArrayList();
		
		candidates.add(locale);
		
		if (!locale.getLanguage().isEmpty() && !locale.getCountry().isEmpty() && !locale.getVariant().isEmpty()) {
			candidates.add(new Locale(locale.getLanguage(), locale.getCountry()));
			candidates.add(new Locale(locale.getLanguage()));
		} else if (!locale.getLanguage().isEmpty() && !locale.getCountry().isEmpty()) {
			candidates.add(new Locale(locale.getLanguage()));
		}
		
		candidates.add(Locale.US);
		candidates.add(Locale.ROOT);
		return candidates;
	}
	
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException,
			InstantiationException, IOException {
		Validate.notNull(baseName);
		Validate.notNull(locale);
		Validate.notNull(format);
		Validate.notNull(loader);
		
		ResourceBundle bundle = null;
		
		if (YML_FORMAT.equals(format)) {
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, format);
			
			URL url = null;
			
			if (mode == LoadingMode.CLASSPATH) {
				//The mode forces us to load the resource from classpath
				url = getClass().getResource(classpathDir + resourceName);
			} else if (mode == LoadingMode.FILE_SYSTEM) {
				//If we use the file system mode, try to load the resource from file first
				//and load it from classpath if it fails
				File resourceFile = new File(localeDir, resourceName);
				
				if (resourceFile.exists() && resourceFile.isFile()) {
					url = resourceFile.toURI().toURL();
				} else {
					url = getClass().getResource(classpathDir + resourceName);
				}
			}
			
			URLConnection connection = url.openConnection();
			if (reload) {
				connection.setUseCaches(false);
			}
			
			InputStream stream = connection.getInputStream();
			
			if (stream != null) {
				Reader reader = new InputStreamReader(stream, I18N.UTF8_CHARSET);
				YamlConfiguration config = new YamlConfiguration();
				
				StringBuilder builder;
				
				try (BufferedReader bufferedReader = new BufferedReader(reader)) {
					builder = new StringBuilder();
					
					String read;
					while ((read = bufferedReader.readLine()) != null) {
						builder.append(read);
						builder.append('\n');
					}
				}
				
				try {
					config.loadFromString(builder.toString());
				} catch (InvalidConfigurationException e) {
					throw new InstantiationException(e.getMessage());
				}
				
				bundle = new YMLResourceBundle(config);
			}
		} else {
			bundle = super.newBundle(baseName, locale, format, loader, reload);
		}
		
		return bundle;
	}
	
	@Override
	public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
		boolean reload;
		
		if (format.equals(YML_FORMAT)) {
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, format);
			
			File resourceFile = new File(localeDir, resourceName);
			if (resourceFile.exists() && resourceFile.isFile()) {
				long lastModified = resourceFile.lastModified();
				
				reload = lastModified > loadTime;
			} else {
				reload = super.needsReload(baseName, locale, format, loader, bundle, loadTime);	
			}
		} else {
			reload = super.needsReload(baseName, locale, format, loader, bundle, loadTime);
		}
		
		return reload;
	}
	
	@Override
	public long getTimeToLive(String baseName, Locale locale) {
		return TTL_DONT_CACHE;
	}
	
	@Override
	public Locale getFallbackLocale(String baseName, Locale locale) {
		return Locale.US;
	}
	
}
