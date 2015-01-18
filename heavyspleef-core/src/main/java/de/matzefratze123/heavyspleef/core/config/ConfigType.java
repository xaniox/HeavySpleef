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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.configuration.Configuration;

public enum ConfigType {
	
	DEFAULT_CONFIG("config.yml", "/config.yml", DefaultConfig.class),
	DATABASE_CONFIG("database-config.yml", "/database-config.yml", DatabaseConfig.class);

	private String destinationFileName;
	private String classpathResourceName;
	private Class<? extends ConfigurationObject> configClass;
	
	private ConfigType(String destinationFileName, String classpathResourceName, Class<? extends ConfigurationObject> configClass) {
		this.destinationFileName = destinationFileName;
		this.classpathResourceName = classpathResourceName;
		this.configClass = configClass;
	}
	
	public String getDestinationFileName() {
		return destinationFileName;
	}
	
	public String getClasspathResourceName() {
		return classpathResourceName;
	}
	
	public ConfigurationObject newConfigInstance(Configuration configuration, Object... args) {
		boolean fallback = false;
		boolean hasArgs = false;
		Constructor<?>[] constructors = configClass.getConstructors();
		Constructor<?> constructor = null;
		
		for (Constructor<?> constr : constructors) {
			Class<?>[] parameterClasses = constr.getParameterTypes();
			
			if (parameterClasses.length < 1 || !Configuration.class.isAssignableFrom(parameterClasses[0])) {
				continue;
			}
			
			if (parameterClasses.length > 1 && Object[].class.isAssignableFrom(parameterClasses[1])) {
				hasArgs = true;
				constructor = constr;
				break;
			} else if (parameterClasses.length == 1) {
				constructor = constr;
				break;
			}
		}
		
		if (constructor == null) {
			try {
				constructor = configClass.getConstructor();
				fallback = true;
			} catch (NoSuchMethodException nsme) {
				throw new IllegalStateException("Class " + configClass.getCanonicalName() + " does must define an empty or an "
						+ Configuration.class.getCanonicalName() + " constructor with an optional Object varargs parameter");
			}
		}
		
		Object[] methodArgs = fallback ? new Object[0] : hasArgs ? new Object[] { configuration, args } : new Object[] { configuration };
		ConfigurationObject obj;
		
		try {
			obj = (ConfigurationObject) constructor.newInstance(methodArgs);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		if (fallback) {
			obj.inflate(configuration);
		}
		
		return obj;
	}
	
}
