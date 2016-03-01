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
package de.xaniox.heavyspleef.core.config;

import de.xaniox.heavyspleef.core.config.ThrowingConfigurationObject.UnsafeException;
import org.bukkit.configuration.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum ConfigType {
	
	DEFAULT_CONFIG("config.yml", "/config.yml", DefaultConfig.class),
	DATABASE_CONFIG("database-config.yml", "/database-config.yml", DatabaseConfig.class),
	JOIN_SIGN_LAYOUT_CONFIG("layout/layout_join-sign.yml", "/layout/layout_join-sign.yml", SignLayoutConfiguration.class),
	LEAVE_SIGN_LAYOUT_CONFIG("layout/layout_leave-sign.yml", "/layout/layout_leave-sign.yml", SignLayoutConfiguration.class),
	START_SIGN_LAYOUT_CONFIG("layout/layout_start-sign.yml", "/layout/layout_start-sign.yml", SignLayoutConfiguration.class),
	VOTE_SIGN_LAYOUT_CONFIG("layout/layout_vote-sign.yml", "/layout/layout_vote-sign.yml", SignLayoutConfiguration.class),
	SPECTATE_SIGN_LAYOUT_CONFIG("layout/layout_spectate-sign.yml", "/layout/layout_spectate-sign.yml", SignLayoutConfiguration.class),
	INFO_WALL_SIGN_LAYOUT_CONFIG("layout/layout_info-wall-sign.yml", "/layout/layout_info-wall-sign.yml", SignLayoutConfiguration.class);

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
				throw new IllegalStateException("Class " + configClass.getCanonicalName() + " must define an empty or an "
						+ Configuration.class.getCanonicalName() + " constructor with an optional Object varargs parameter");
			}
		}
		
		Object[] methodArgs = fallback ? new Object[0] : hasArgs ? new Object[] { configuration, args } : new Object[] { configuration };
		ConfigurationObject obj;
		
		try {
			obj = (ConfigurationObject) constructor.newInstance(methodArgs);
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			
			if (cause != null && cause instanceof UnsafeException) {
				throw (UnsafeException) cause;
			} else {
				throw new RuntimeException(cause);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		
		if (fallback) {
			obj.inflate(configuration);
		}
		
		return obj;
	}
	
}