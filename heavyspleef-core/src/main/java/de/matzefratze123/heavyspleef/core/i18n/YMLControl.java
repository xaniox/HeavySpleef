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

public class YMLControl extends ResourceBundle.Control {
	
	private static final String YML_FORMAT = "yml";
	
	private File localeDir;
	private String classpathDir;
	private boolean classpath;
	
	public YMLControl(File localeDir, String classpathDir) {
		this.localeDir = localeDir;
		this.classpathDir = classpathDir;
	}
	
	public YMLControl(File localeDir, String classpathDir, boolean classpath) {
		this(localeDir, classpathDir);
		
		this.classpath = classpath;
	}
	
	@Override
	public List<String> getFormats(String baseName) {
		if (baseName == null) {
			throw new NullPointerException();
		}
		
		return Arrays.asList(YML_FORMAT);
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
			
			File resourceFile = new File(localeDir, resourceName);
			if (resourceFile.exists() && resourceFile.isFile() && !classpath) {
				url = resourceFile.toURI().toURL();
			} else {
				url = loader.getResource(classpathDir + baseName);
			}
			
			URLConnection connection = url.openConnection();
			if (reload) {
				connection.setUseCaches(false);
			}
			
			InputStream stream = connection.getInputStream();
			
			if (stream != null) {
				Reader reader = new InputStreamReader(stream);
				YamlConfiguration config = new YamlConfiguration();
				
				try {
					config.load(reader);
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
	public Locale getFallbackLocale(String baseName, Locale locale) {
		return Locale.US;
	}
	
}
