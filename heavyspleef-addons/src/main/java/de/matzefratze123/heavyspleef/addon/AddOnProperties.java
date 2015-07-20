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
package de.matzefratze123.heavyspleef.addon;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18N.LoadingMode;

@Getter
public class AddOnProperties {
	
	private String name;
	private String version;
	private String author;
	private String mainClass;
	private I18N.LoadingMode loadingMode;
	private List<String> contributors;
	private List<CommandConfiguration> commands;
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
		
		if (yamlMap.containsKey("commandClasses")) {
			commands = Lists.newArrayList();
			Object commandsObj = yamlMap.get("commands");
			if (!(commandsObj instanceof Map<?, ?>)) {
				throw new InvalidPropertiesException("commands are of wrong type");
			}
			
			for (Entry<?, ?> entry : ((Map<?, ?>) yamlMap.get("commands")).entrySet()) {
				String name = (String) entry.getKey();
				if (!(entry.getValue() instanceof Map<?, ?>)) {
					throw new InvalidPropertiesException("Command " + name + "'s values are not a mapping");
				}
				
				Map<?, ?> commandValues = (Map<?, ?>) entry.getValue();
				commands.add(CommandConfiguration.parse(name, commandValues));
			}
		}
		
		if (yamlMap.containsKey("extensions")) {
			extensions = Lists.newArrayList();
			Object extensionsObj = yamlMap.get("extensions");
			if (!(extensionsObj instanceof Iterable<?>)) {
				throw new InvalidPropertiesException("extensions are of wrong type");
			}
			
			for (Object ext : (Iterable<?>) yamlMap.get("extensions")) {
				extensions.add(ext.toString());
			}
		}
		
		if (yamlMap.containsKey("flags")) {
			flags = Lists.newArrayList();
			Object flagObj = yamlMap.get("flags");
			if (!(flagObj instanceof Iterable<?>)) {
				throw new InvalidPropertiesException("flags are of wrong type");
			}
			
			for (Object flag : (Iterable<?>) yamlMap.get("flags")) {
				flags.add(flag.toString());
			}
		}
	}
	
	@Getter
	@AllArgsConstructor
	public static class CommandConfiguration {
				
		private String name;
		private String className;
		
		public static CommandConfiguration parse(String name, Map<?, ?> commandMap) {
			Validate.isTrue(commandMap.containsKey("class"), "");
			String className = (String) commandMap.get("class");
			
			return new CommandConfiguration(name, className);
		}
		
	}
	
}
