/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.addon;

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18N.LoadingMode;

import java.util.List;
import java.util.Map;

public class AddOnProperties {
	
	private String name;
	private String version;
	private String author;
	private String mainClass;
	private I18N.LoadingMode loadingMode;
	private List<String> contributors;
	private List<String> commands;
	private List<String> flags;
	private List<String> extensions;
	
	public AddOnProperties(Map<?, ?> yamlMap) throws InvalidPropertiesException {
		if (!yamlMap.containsKey("name")) {
			throw new InvalidPropertiesException(AddOnLoader.ADD_ON_PROPERTIES_FILE + " does not declare a 'name'");
		}
		
		this.name = yamlMap.get("name").toString();
		
		if (!yamlMap.containsKey("version")) {
			throw new InvalidPropertiesException(AddOnLoader.ADD_ON_PROPERTIES_FILE + " does not declare a 'version'");
		}
		
		this.version = yamlMap.get("version").toString();
		
		if (yamlMap.containsKey("author")) {
			this.author = yamlMap.get("author").toString();
		}
		
		if (!yamlMap.containsKey("main")) {
			throw new InvalidPropertiesException(AddOnLoader.ADD_ON_PROPERTIES_FILE + " does not declare a 'main'");
		}
		
		if (yamlMap.containsKey("i18n")) {
			String mode = (String) yamlMap.get("i18n");
			if (mode.equalsIgnoreCase("resource")) {
				loadingMode = LoadingMode.CLASSPATH;
			} else if (mode.equalsIgnoreCase("dynamic")) {
				loadingMode = LoadingMode.FILE_SYSTEM;
			}
		}
		
		this.mainClass = yamlMap.get("main").toString();
		if (yamlMap.containsKey("contributors")) {
			contributors = Lists.newArrayList();
			Object contributorsObj = yamlMap.get("contributors");
			if (!(contributorsObj instanceof Iterable<?>)) {
				throw new InvalidPropertiesException("contributors are of wrong type");
			}
			
			for (Object contr : (Iterable<?>) yamlMap.get("contributors")) {
				contributors.add(contr.toString());
			}
		}
		
		if (yamlMap.containsKey("commands")) {
			commands = Lists.newArrayList();
			Object commandsObj = yamlMap.get("commands");
			if (!(commandsObj instanceof Iterable<?>)) {
				throw new InvalidPropertiesException("commands are of wrong type");
			}
			
			for (Object clazzObj : (Iterable<?>) commandsObj) {
				commands.add(clazzObj.toString());
			}
		}
		
		if (yamlMap.containsKey("extensions")) {
			extensions = Lists.newArrayList();
			Object extensionsObj = yamlMap.get("extensions");
			if (!(extensionsObj instanceof Iterable<?>)) {
				throw new InvalidPropertiesException("extensions are of wrong type");
			}
			
			for (Object ext : (Iterable<?>) extensionsObj) {
				extensions.add(ext.toString());
			}
		}
		
		if (yamlMap.containsKey("flags")) {
			flags = Lists.newArrayList();
			Object flagObj = yamlMap.get("flags");
			if (!(flagObj instanceof Iterable<?>)) {
				throw new InvalidPropertiesException("flags are of wrong type");
			}
			
			for (Object flag : (Iterable<?>) flagObj) {
				flags.add(flag.toString());
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getAuthor() {
		return author;
	}

	public String getMainClass() {
		return mainClass;
	}

	public I18N.LoadingMode getLoadingMode() {
		return loadingMode;
	}

	public List<String> getContributors() {
		return contributors;
	}

	public List<String> getCommands() {
		return commands;
	}

	public List<String> getFlags() {
		return flags;
	}

	public List<String> getExtensions() {
		return extensions;
	}
	
}