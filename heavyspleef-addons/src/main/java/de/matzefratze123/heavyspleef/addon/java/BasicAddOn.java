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
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.Setter;
import de.matzefratze123.heavyspleef.addon.AddOn;
import de.matzefratze123.heavyspleef.addon.AddOnManager;
import de.matzefratze123.heavyspleef.addon.AddOnProperties;
import de.matzefratze123.heavyspleef.addon.access.CommandManagerAccess;
import de.matzefratze123.heavyspleef.addon.access.ExtensionRegistryAccess;
import de.matzefratze123.heavyspleef.addon.access.FlagRegistryAccess;
import de.matzefratze123.heavyspleef.core.HeavySpleef;

public class BasicAddOn implements AddOn {
	
	private @Getter HeavySpleef heavySpleef;
	private @Getter File dataFolder;
	private @Getter AddOnProperties properties;
	private @Getter AddOnLogger logger;
	private @Getter @Setter boolean enabled;
	private @Getter File file;
	private @Getter ClassLoader classLoader;
	private AddOnManager manager;
	
	protected BasicAddOn() {}
	
	public void load() {}
	
	public void enable() {}
	
	public void disable() {}
	
	@Override
	public String getName() {
		return properties.getName();
	}
	
	@Override
	public FlagRegistryAccess getFlagRegistry() {
		return manager.getFlagRegistryAccess();
	}
	
	@Override
	public ExtensionRegistryAccess getExtensionRegistry() {
		return manager.getExtensionRegistryAccess();
	}
	
	@Override
	public CommandManagerAccess getCommandManager() {
		return manager.getCommandManagerAccess();
	}
	
	void init(HeavySpleef heavySpleef, File dataFolder, AddOnProperties properties, File addOnFile, AddOnClassLoader classLoader, AddOnManager manager) {
		this.heavySpleef = heavySpleef;
		this.manager = manager;
		this.dataFolder = dataFolder;
		this.properties = properties;
		this.file = addOnFile;
		this.classLoader = classLoader;
		this.logger = new AddOnLogger();
		this.logger.setParent(heavySpleef.getLogger());
		this.logger.setUseParentHandlers(true);
	}
	
	public class AddOnLogger extends Logger {

		private static final String ADD_ON_PREFIX = "[AddOn]";
		private String loggerPrefix;
		
		public AddOnLogger() {
			super(BasicAddOn.this.getClass().getCanonicalName(), null);
			
			this.loggerPrefix = "[" + properties.getName() + "]";
		}		
		
		@Override
		public void log(LogRecord record) {
			record.setMessage("[" + heavySpleef.getPlugin().getName() + "]" + ADD_ON_PREFIX + loggerPrefix + " " + record.getMessage());
			super.log(record);
		}
		
	}

}
