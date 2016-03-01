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
package de.xaniox.heavyspleef.addon.java;

import com.google.common.collect.Maps;
import de.xaniox.heavyspleef.addon.AddOnManager;
import de.xaniox.heavyspleef.addon.AddOnProperties;
import de.xaniox.heavyspleef.addon.InvalidAddOnException;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.i18n.I18N;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Logger;

public class AddOnClassLoader extends URLClassLoader {
	
	private final BasicAddOn addOn;
	private final AddOnProperties properties;
	private final SharedClassContext classContext;
	private final Map<String, Class<?>> classes = Maps.newHashMap();
	private final File dataFolder;
	private final File file;
	private I18N i18N;
	private final AddOnManager manager;
	
	public AddOnClassLoader(File file, ClassLoader parent, AddOnProperties properties, AddOnManager manager, SharedClassContext ctx, File dataFolder)
			throws InvalidAddOnException, MalformedURLException {
		super(new URL[] {file.toURI().toURL()}, parent);
		
		this.file = file;
		this.dataFolder = dataFolder;
		this.properties = properties;
		this.classContext = ctx;
		this.manager = manager;
		
		String mainClassName = properties.getMainClass();
		Class<?> mainClass;
		
		try {
			mainClass = Class.forName(mainClassName, true, this);
		} catch (ClassNotFoundException e) {
			throw new InvalidAddOnException("Cannot find main class \"" + mainClassName + "\"", e);
		}
		
		Class<? extends BasicAddOn> addOnClass;
		
		try {
			addOnClass = mainClass.asSubclass(BasicAddOn.class);
		} catch (ClassCastException e) {
			throw new InvalidAddOnException("Main class \"" + mainClassName + "\" does not extend AddOn", e);
		}
		
		try {
			addOn = addOnClass.newInstance();
		} catch (IllegalAccessException e) {
			throw new InvalidAddOnException("Main class \"" + mainClassName + "\" does not declare an public empty constructor", e);
		} catch (InstantiationException e) {
			throw new InvalidAddOnException("Main class \"" + mainClassName + "\" could not be instantiated", e);
		}
	}
	
	public BasicAddOn getAddOn() {
		return addOn;
	}
	
	public void setI18N(I18N i18n) {
		i18N = i18n;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name, true);
	}
	
	Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
		Validate.notNull(name, "Name cannot be null");
		
		Class<?> clazz = classes.get(name);
		
		if (clazz == null) {
			if (checkGlobal) {
				//Get global class
				classContext.getGlobalClass(name);
			}
			
			if (clazz == null) {
				clazz = super.findClass(name);
				
				if (clazz != null) {
					//Register the class in the global class context
					classContext.registerClass(addOn, clazz);
					classes.put(name, clazz);
				}
			}
		}
		
		return clazz;
	}
	
	void initialize(HeavySpleef heavySpleef, Logger logger) {
		addOn.init(heavySpleef, dataFolder, properties, file, this, manager, i18N, logger);
	}

}