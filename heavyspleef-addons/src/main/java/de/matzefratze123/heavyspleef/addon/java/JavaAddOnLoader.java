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
package de.matzefratze123.heavyspleef.addon.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.yaml.snakeyaml.Yaml;

import de.matzefratze123.heavyspleef.addon.AddOn;
import de.matzefratze123.heavyspleef.addon.AddOnLoader;
import de.matzefratze123.heavyspleef.addon.AddOnManager;
import de.matzefratze123.heavyspleef.addon.AddOnProperties;
import de.matzefratze123.heavyspleef.addon.InvalidAddOnException;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18N.LoadingMode;
import de.matzefratze123.heavyspleef.core.i18n.I18NBuilder;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;

public class JavaAddOnLoader implements AddOnLoader {
	
	private final Yaml yaml = new Yaml();
	private final AddOnManager manager;
	
	public JavaAddOnLoader(AddOnManager manager) {
		this.manager = manager;
	}
	
	@Override
	public AddOn load(File addOnFile) throws InvalidAddOnException {
		if (!addOnFile.getName().toLowerCase().endsWith(".jar")) {
			throw new InvalidAddOnException("File " + addOnFile.getName() + " does not have the extension .jar");
		}
		
		AddOnProperties properties = loadProperties(addOnFile);
		return load(addOnFile, properties);
	}
	
	public AddOn load(File addOnFile, AddOnProperties properties) throws InvalidAddOnException {
		File folder = addOnFile.getAbsoluteFile().getParentFile();
		File dataFolder = new File(folder, properties.getName());
		if (dataFolder.exists() && !dataFolder.isDirectory()) {
			throw new InvalidAddOnException("Data-Folder '" + dataFolder.getPath() + "' is not a directory");
		}
		
		manager.getLogger().info("Loading add-on " + properties.getName() + " v" + properties.getVersion());
		AddOnClassLoader classLoader;
		
		try {
			classLoader = new AddOnClassLoader(addOnFile, getClass().getClassLoader(), properties, manager, manager.getClassContext(), dataFolder);
		} catch (MalformedURLException e) {
			throw new InvalidAddOnException("Invalid add-on file", e);
		}
		
		BasicAddOn addon = classLoader.getAddOn();
		
		HeavySpleef heavySpleef = manager.getHeavySpleef();
		DefaultConfig config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
		Locale locale = config.getLocalization().getLocale();
		
		I18N.LoadingMode loadingMode = properties.getLoadingMode();
		if (loadingMode != null) {
			if (loadingMode == LoadingMode.FILE_SYSTEM && !dataFolder.exists()) {
				dataFolder.mkdir();
			}
			
			I18N i18n = I18NBuilder.builder()
					.setLoadingMode(loadingMode)
					.setClassLoader(classLoader)
					.setLocale(locale)
					.setFileSystemFolder(dataFolder)
					.setClasspathFolder("/")
					.setLogger(addon.getLogger())
					.build();
			
			I18NManager manager = heavySpleef.getI18NManager();
			manager.registerI18N(i18n);
			classLoader.setI18N(i18n);
		} else {
			classLoader.setI18N(I18NManager.getGlobal());
		}
		
		classLoader.initialize(manager.getHeavySpleef());
		return addon;
	}
	
	public AddOnProperties loadProperties(File file) throws InvalidAddOnException {
		try (JarFile jarFile = new JarFile(file)) {
			JarEntry propertiesEntry = jarFile.getJarEntry(ADD_ON_PROPERTIES_FILE);
			
			if (propertiesEntry == null) {
				throw new InvalidAddOnException("Add-On file " + file.getName() + " does not contain an " + ADD_ON_PROPERTIES_FILE);
			}
			
			InputStream in = jarFile.getInputStream(propertiesEntry);
			Object obj = yaml.load(in);
			
			if (!(obj instanceof Map<?, ?>)) {
				throw new InvalidAddOnException("Unknown yaml response object " + obj.getClass().getCanonicalName());
			}
			
			Map<?, ?> result = (Map<?, ?>) obj;
			return new AddOnProperties(result);
		} catch (IOException e) {
			throw new InvalidAddOnException(e);
		}
	}
	
}
