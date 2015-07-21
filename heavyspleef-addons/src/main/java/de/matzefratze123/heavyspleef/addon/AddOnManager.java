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

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;

import org.apache.commons.lang.Validate;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

import de.matzefratze123.heavyspleef.addon.AddOnProperties.CommandConfiguration;
import de.matzefratze123.heavyspleef.addon.access.CommandManagerAccess;
import de.matzefratze123.heavyspleef.addon.access.ExtensionRegistryAccess;
import de.matzefratze123.heavyspleef.addon.access.FlagRegistryAccess;
import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;
import de.matzefratze123.heavyspleef.addon.java.JavaAddOnLoader;
import de.matzefratze123.heavyspleef.addon.java.SharedClassContext;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.extension.GameExtension;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;

public final class AddOnManager {
	
	private final @Getter HeavySpleef heavySpleef;
	private final @Getter Logger logger;
	private final BiMap<String, AddOn> addOnMap;
	private final @Getter SharedClassContext classContext;
	private final AddOnLoader loader = new JavaAddOnLoader(this);
	
	private final @Getter FlagRegistryAccess flagRegistryAccess;
	private final @Getter ExtensionRegistryAccess extensionRegistryAccess;
	private final @Getter CommandManagerAccess commandManagerAccess;
	
	public AddOnManager(HeavySpleef heavySpleef) {
		this.addOnMap = HashBiMap.create();
		this.classContext = new SharedClassContext();
		this.heavySpleef = heavySpleef;
		this.logger = heavySpleef.getLogger();
		
		this.flagRegistryAccess = new FlagRegistryAccess(heavySpleef.getFlagRegistry());
		this.extensionRegistryAccess = new ExtensionRegistryAccess(heavySpleef.getExtensionRegistry());
		this.commandManagerAccess = new CommandManagerAccess(heavySpleef.getCommandManager());
	}
	
	public void loadAddOns(File baseDir) {
		if (!baseDir.exists()) {
			throw new IllegalArgumentException("Directory '" + baseDir.getName() + "' does not exist");
		}
		
		for (File addOnFile : baseDir.listFiles()) {
			if (!addOnFile.getName().toLowerCase().endsWith(".jar")) {
				continue;
			}
			
			loadAddOn(addOnFile);
		}
	}
	
	void loadAddOnsSafely(File baseDir) {
		if (!baseDir.exists()) {
			throw new IllegalArgumentException("Directory '" + baseDir.getName() + "' does not exist");
		}
		
		for (File addOnFile : baseDir.listFiles()) {
			if (!addOnFile.getName().toLowerCase().endsWith(".jar")) {
				continue;
			}
			
			loadAddOnSafely(addOnFile);
		}
	}
	
	public void loadAddOn(File addOnFile) {
		try {
			AddOn addOn = loader.load(addOnFile);
			addOn.load();
			
			addOnMap.put(addOn.getName(), addOn);
		} catch (InvalidAddOnException e) {
			logger.log(Level.SEVERE, "Could not load add-on " + addOnFile.getName(), e);
		}
	}
	
	public void searchAndLoad(File baseDir, String name) {
		if (!baseDir.exists()) {
			throw new IllegalArgumentException("Directory '" + baseDir.getName() + "' does not exist");
		}
		
		JavaAddOnLoader javaLoader = (JavaAddOnLoader) loader;
		
		for (File addonFile : baseDir.listFiles()) {
			if (!addonFile.getName().toLowerCase().endsWith(".jar")) {
				continue;
			}
			
			try {
				AddOnProperties properties = javaLoader.loadProperties(addonFile);
				String addonName = properties.getName();
				if (!name.equals(addonName)) {
					continue;
				}
				
				loadAddOn(addonFile);
			} catch (InvalidAddOnException e) {
				logger.log(Level.SEVERE, "Could not load add-on " + addonFile.getName(), e);
			}
		}
	}
	
	void loadAddOnSafely(File addOnFile) {
		Validate.notNull(addOnFile, "addOnFile cannot be null");
		Validate.isTrue(addOnFile.exists(), "addOnFile does not exist");
		
		JavaAddOnLoader javaLoader = (JavaAddOnLoader) loader;
		AddOnProperties properties;
		
		try {
			properties = javaLoader.loadProperties(addOnFile);
		} catch (InvalidAddOnException e) {
			logger.log(Level.SEVERE, "Could properties of add-on " + addOnFile.getName() + " safely", e);
			return;
		}
		
		if (addOnMap.containsKey(properties.getName())) {
			//Ignore this request as we're loading safely
			return;
		}
		
		try {
			AddOn addOn = javaLoader.load(addOnFile, properties);
			addOn.load();
			
			addOnMap.put(addOn.getName(), addOn);
		} catch (InvalidAddOnException e) {
			logger.log(Level.SEVERE, "Could not load add-on " + addOnFile.getName() + " safely", e);
		}
	}
	
	public void unloadAddOn(String name) {
		Validate.isTrue(addOnMap.containsKey(name));
		
		AddOn addOn = addOnMap.remove(name);
		if (addOn.isEnabled()) {
			addOn.disable();
		}
		
		BasicAddOn basicAddOn = (BasicAddOn) addOn;
		URLClassLoader classLoader = (URLClassLoader) basicAddOn.getClassLoader();
		
		try {
			classLoader.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not properly close the classloader of add-on " + addOn.getName(), e);
		}
		
		if (addOn.getProperties().getLoadingMode() != null) {
			//Unregister i18n
			I18NManager i18nManager = heavySpleef.getI18NManager();
			i18nManager.unregisterI18N(basicAddOn.getName());
		}
		
		CommandManagerService service = heavySpleef.getCommandManager().getService();
		service.removeArgument(addOn);
		
		//Clear class cache
		classContext.unregister(addOn);
	}
	
	public boolean isAddOnEnabled(String name) {
		if (!addOnMap.containsKey(name)) {
			return false;
		}
		
		BasicAddOn addon = (BasicAddOn) addOnMap.get(name);
		return addon.isEnabled();
	}
	
	public void enableAddOns() {
		for (AddOn addOn : addOnMap.values()) {
			if (addOn.isEnabled()) {
				continue;
			}
			
			enableAddOn(addOn);
		}
	}
	
	public void enableAddOn(String name) {
		AddOn addOn;
		if ((addOn = getAddOn(name)) == null) {
			throw new IllegalStateException("No add-on with the name '" + name + "' has been loaded");
		}
		
		try {
			enableAddOn(addOn);
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Could not enable add-on " + addOn.getName() + " (is the add-on up-to-date?)", t);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void enableAddOn(AddOn addOn) {
		if (addOn.isEnabled()) {
			throw new IllegalStateException("Add-On is already enabled");
		}
		
		AddOnProperties properties = addOn.getProperties();
		
		List<CommandConfiguration> commands = properties.getCommands();
		List<String> flags = properties.getFlags();
		List<String> extensions = properties.getExtensions();
		ClassLoader loader = ((BasicAddOn)addOn).getClassLoader();
		
		logger.log(Level.INFO, "Enabling add-on " + properties.getName() + " v" + properties.getVersion());
		
		if (commands != null) {
			for (CommandConfiguration config : commands) {
				Class<?> clazz;
			
				try {
					clazz = Class.forName(config.getClassName(), true, loader);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Add-On " + addOn.getName() + " defines an unknown command class", e);
				}
				
				commandManagerAccess.registerSpleefCommand(clazz, addOn);
			}
		}
		
		if (flags != null) {
			for (String flagClassName : flags) {
				Class<?> clazz;
				
				try {
					clazz = Class.forName(flagClassName, true, loader);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Add-On " + addOn.getName() + " defines an unknown flag class", e);
				}
				
				Class<? extends AbstractFlag<?>> flagClass;
				
				try {
					flagClass = (Class<? extends AbstractFlag<?>>) clazz.asSubclass(AbstractFlag.class);
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Class " + clazz.getName() + " does not extend AbstractFlag");
				}
				
				flagRegistryAccess.registerFlag(flagClass, addOn);
			}
		}
		
		if (extensions != null) {
			for (String extensionClassName : extensions) {
				Class<?> clazz;
				
				try {
					clazz = Class.forName(extensionClassName, true, loader);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Add-On " + addOn.getName() + " defines an unknown extension class", e);
				}
				
				Class<? extends GameExtension> extClass;
				
				try {
					extClass = clazz.asSubclass(GameExtension.class);
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Class " + clazz.getName() + " does not extend GameExtension");
				}
				
				extensionRegistryAccess.registerExtension(extClass, addOn);
			}
		}
		
		try {
			addOn.enable();
		} catch (Throwable t) {
			getLogger().log(
					Level.SEVERE,
					"Unexpected exception while enabling Add-On " + addOn.getName() + " v" + addOn.getProperties().getVersion()
							+ ". Is it up to date?", t);
		}

		addOn.setEnabled(true);
	}
	
	public void disableAddOn(String name) {
		AddOn addOn;
		if ((addOn = getAddOn(name)) == null) {
			throw new IllegalStateException("No add-on with the name '" + name + "' has been loaded");
		}
		
		disableAddOn(addOn);
	}
	
	public void disableAddOn(AddOn addOn) {
		if (!addOn.isEnabled()) {
			throw new IllegalStateException("Add-On is already disabled");
		}
		
		addOn.setEnabled(false);
		
		try {
			addOn.disable();
		} catch (Throwable t) {
			getLogger().log(
					Level.SEVERE,
					"Unexpected exception while disabling Add-On " + addOn.getName() + " v" + addOn.getProperties().getVersion()
							+ ". Is it up to date?", t);
		}
		
		commandManagerAccess.unregisterSpleefCommands(addOn);
		flagRegistryAccess.unregister(addOn);
		extensionRegistryAccess.unregister(addOn);
	}
	
	public AddOn getAddOn(String name) {
		return addOnMap.get(name);
	}
	
	public Set<AddOn> getAddOns() {
		return getAddOns(null);
	}
	
	public Set<AddOn> getEnabledAddOns() {
		return getAddOns(new Predicate<AddOn>() {

			@Override
			public boolean apply(AddOn input) {
				return input.isEnabled();
			}
		});
	}
	
	private Set<AddOn> getAddOns(Predicate<AddOn> filter) {
		ImmutableSet.Builder<AddOn> builder = ImmutableSet.builder();
		for (AddOn addOn : addOnMap.values()) {
			if (filter == null || filter.apply(addOn)) {
				builder.add(addOn);
			}
		}
		
		return builder.build();
	}
	
}
