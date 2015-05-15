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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
	private static final List<String> DEFAULT_CLASSPATH_LOCALE_RESOURCES = Lists.newArrayList("locale_en_US.yml", "locale_de_DE.yml");
	private static final String FALLBACK_FILE = "locale_en_US.yml";
	private static final String FILE_PROTOCOL = "file";
	private static final String JAR_EXTENSION = ".jar";
	private static final String JAR_ENTRY_SEPERATOR = "/";
	private static final String LOCALE_FILE_REGEX = "locale_[a-z]{2}(_[A-Z]{2})?\\.yml";
	private static final YamlRepresenter YAML_REPRESENTER = new YamlRepresenter();
	private static final DumperOptions DUMPER_OPTIONS = new DumperOptions();
	static final Locale FALLBACK_LOCALE = Locale.US;
	
	static {
		DUMPER_OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		DUMPER_OPTIONS.setAllowUnicode(true);
		
		YAML_REPRESENTER.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	}
	
	private Locale locale;
	private ClassLoader classLoader;
	private final Logger logger;
	private LoadingMode mode;
	private final YMLControl defaultControl;
	private final File fileSystemFolder;
	private final String classpathFolder;
	private I18N parent;
	private ResourceBundle bundle;
	
	protected I18N(Locale locale, LoadingMode mode, File fileSystemFolder, String classpathFolder, ClassLoader loader, Logger logger) {
		this.defaultControl = new YMLControl(fileSystemFolder, classpathFolder, mode);
		this.locale = locale;
		this.mode = mode;
		this.fileSystemFolder = fileSystemFolder;
		
		if (!classpathFolder.endsWith(String.valueOf(JAR_ENTRY_SEPERATOR)) && !classpathFolder.isEmpty()) {
			classpathFolder += JAR_ENTRY_SEPERATOR;
		}
		
		this.classpathFolder = classpathFolder;
		this.classLoader = loader;
		this.logger = logger;
		
		load();
	}
	
	public void load() {
		if (mode == LoadingMode.FILE_SYSTEM) {
			//Copy those resource files when we're using the file system loading mode
			try {
				copyClasspathResources();
			} catch (IOException e) {
				// Just inform as the YMLControl is going to load this file from the classpath
				logger.log(Level.WARNING, "Could not copy locale resource file, using classpath resource", e);
			}
		}
		
		loadBundle();
	}
	
	private void loadBundle() throws MissingResourceException {
		//try {
			bundle = ResourceBundle.getBundle("locale", locale, defaultControl);
		/*} catch (MissingResourceException mre) {
			//Locale could not be found
			//YMLControl classpathControl = new YMLControl(null, CLASSPATH_DIR, mode);
			//bundle = ResourceBundle.getBundle("locale", FALLBACK_LOCALE, classpathControl);
			throw new Mis
		}*/
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
		
		loadBundle();
	}
	
	public void setParent(I18N parent) {
		this.parent = parent;
	}
	
	public void copyClasspathResources() throws IOException {
		if (mode != LoadingMode.FILE_SYSTEM) {
			throw new IllegalStateException("Resource files can only be copied on LoadingMode.FILE_SYSTEM mode");
		}
		
		//Try to find a list of all available resources
		List<String> classpathResources = Lists.newArrayList();
		if (classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			URL[] urls = urlClassLoader.getURLs();
			
			//Get a list of all locale resources in this URLClassLoader
			for (URL url : urls) {
				if (!url.getProtocol().equals(FILE_PROTOCOL)) {
					//This url doesn't point to a file
					continue;
				}
				
				File file;
				
				try {
					file = new File(url.toURI());
				} catch (URISyntaxException e) {
					//Shouldn't fire as the classloader should have already
					//validated the url
					throw new RuntimeException(e);
				}
				
				if (!file.getName().endsWith(JAR_EXTENSION)) {
					//This file isn't a jar archive
					continue;
				}
				
				try (JarFile jar = new JarFile(file)) {
					Enumeration<JarEntry> entries = jar.entries();
					
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String entryName = entry.getName();
						
						String[] nameComponents = entryName.split(JAR_ENTRY_SEPERATOR);
						StringBuilder folderBuilder = new StringBuilder();
						for (int i = 0; i < nameComponents.length - 1; i++) {
							folderBuilder.append(nameComponents[i])
										.append(JAR_ENTRY_SEPERATOR);
						}
						
						String folder = folderBuilder.toString();
						
						if (!folder.equals(classpathFolder)) {
							//This resource isn't in the right directory
							continue;
						}
						
						String fileName = nameComponents[nameComponents.length - 1];
						if (!fileName.matches(LOCALE_FILE_REGEX)) {
							//This entry isn't a resource entry
							continue;
						}
						
						//Found a resource file
						classpathResources.add(fileName);
					}
				}
			}
		} else {
			classpathResources.addAll(DEFAULT_CLASSPATH_LOCALE_RESOURCES);
		}
		
		for (String localeRes : classpathResources) {
			File localeFile = new File(fileSystemFolder, localeRes);
			
			if (!localeFile.exists()) {
				URL localeResourceUrl = classLoader.getResource(classpathFolder + localeRes);
				
				HeavySpleef.copyResource(localeResourceUrl, localeFile);
			}
		}
		
		// Check if all messages exist, and add missing messages to the file e.g validate and replace
		for (File localeFile : fileSystemFolder.listFiles()) {
			//Classpath resources can only exist in one folder, so their name is unique
			URL classpathResource = classLoader.getResource(classpathFolder + localeFile.getName());
			if (classpathResource == null) {
				classpathResource = classLoader.getResource(classpathFolder + FALLBACK_FILE);
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
				DUMPER_OPTIONS.setIndent(options.indent());

				final Yaml yaml = new Yaml(new YamlConstructor(), YAML_REPRESENTER, DUMPER_OPTIONS);
				
				try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(localeFile), UTF8_CHARSET)) {
					yaml.dump(fileConfig.getValues(false), writer);
					writer.flush();
				}
			}
		}
	}
	
	public String getString(String key) {
		String msg = bundle.getString(key);
		if (msg == null && parent != null) {
			msg = parent.getString(key);
		}
		
		return msg;
	}
	
	public ParsedMessage getVarString(String key) {
		String message = getString(key);
		
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
		String[] array = bundle.getStringArray(key);
		if (array == null && parent != null) {
			array = parent.getStringArray(key);
		}
		
		return array;
	}
	
	public enum LoadingMode {
		
		CLASSPATH,
		FILE_SYSTEM;
		
	}
	
}
