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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.Validate;

import com.google.common.io.Closer;

import de.matzefratze123.heavyspleef.addon.AddOn;
import de.matzefratze123.heavyspleef.addon.AddOnManager;
import de.matzefratze123.heavyspleef.addon.AddOnProperties;
import de.matzefratze123.heavyspleef.addon.access.CommandManagerAccess;
import de.matzefratze123.heavyspleef.addon.access.ExtensionRegistryAccess;
import de.matzefratze123.heavyspleef.addon.access.FlagRegistryAccess;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.I18N;

public class BasicAddOn implements AddOn {
	
	private static final int BUFFER_SIZE = 1024;
	
	private @Getter HeavySpleef heavySpleef;
	private @Getter AddOnManager addOnManager;
	private @Getter File dataFolder;
	private @Getter AddOnProperties properties;
	private @Getter Logger logger;
	private @Getter @Setter boolean enabled;
	private @Getter File file;
	private @Getter ClassLoader classLoader;
	private @Getter I18N i18n;
	
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
		return addOnManager.getFlagRegistryAccess();
	}
	
	@Override
	public ExtensionRegistryAccess getExtensionRegistry() {
		return addOnManager.getExtensionRegistryAccess();
	}
	
	@Override
	public CommandManagerAccess getCommandManager() {
		return addOnManager.getCommandManagerAccess();
	}
	
	protected void copyResource(String name, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		copyResource(name, out);
	}
	
	protected void copyResource(String name, OutputStream out) throws IOException {
		Closer closer = Closer.create();
		
		try {
			Validate.notNull(name, "Name cannot be null");
			Validate.notNull(out, "Out cannot be null");
		
			closer.register(out);
			InputStream in = closer.register(classLoader.getResourceAsStream(name));
			if (in == null) {
				throw new IOException("Resource with name '" + name + "' does not exist");
			}			
			
			final byte[] buffer = new byte[BUFFER_SIZE];
			int read;
			
			while ((read = in.read(buffer, 0, buffer.length)) > 0) {
				out.write(buffer, 0, read);
			}
		} finally {
			if (closer != null) {
				closer.close();
			}
		}
	}
	
	void init(HeavySpleef heavySpleef, File dataFolder, AddOnProperties properties, File addOnFile, AddOnClassLoader classLoader, AddOnManager manager, I18N i18n, Logger logger) {
		this.heavySpleef = heavySpleef;
		this.addOnManager = manager;
		this.dataFolder = dataFolder;
		this.properties = properties;
		this.file = addOnFile;
		this.classLoader = classLoader;
		this.i18n = i18n;
		this.logger = logger;
	}
	
	public class AddOnLogger extends Logger {
		
		private String loggerPrefix;
		
		public AddOnLogger() {
			super(BasicAddOn.this.getClass().getCanonicalName(), null);
		}		
		
		@Override
		public void log(LogRecord record) {
			if (loggerPrefix == null) {
				loggerPrefix = "[" + properties.getName() + "]";
			}
			
			record.setMessage("[" + heavySpleef.getPlugin().getName() + "] " + loggerPrefix + " " + record.getMessage());
			super.log(record);
		}
		
	}

}
