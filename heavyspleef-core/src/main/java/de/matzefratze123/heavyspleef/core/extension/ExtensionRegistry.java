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
package de.matzefratze123.heavyspleef.core.extension;

import java.lang.reflect.Constructor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.matzefratze123.heavyspleef.commands.base.CommandManager;
import de.matzefratze123.heavyspleef.core.event.EventManager;

public class ExtensionRegistry {
	
	private final CommandManager commandManager;
	private BiMap<String, Class<? extends GameExtension>> registeredExtensions;
	
	public ExtensionRegistry(CommandManager commandManager) {
		this.registeredExtensions = HashBiMap.create();
		this.commandManager = commandManager;
	}
	
	public void registerExtension(Class<? extends GameExtension> extClass) {
		if (registeredExtensions.containsValue(extClass)) {
			throw new IllegalArgumentException("This extension type has already been registered");
		}
		
		if (!extClass.isAnnotationPresent(Extension.class)) {
			throw new IllegalArgumentException("Extension type " + extClass.getName() + " does not define an @Extension annotation");
		}
		
		Extension annotation = extClass.getAnnotation(Extension.class);
		String name = annotation.name();
		
		if (registeredExtensions.containsKey(name)) {
			Class<? extends GameExtension> alreadyRegisteredClass = registeredExtensions.get(name);
			
			throw new IllegalArgumentException("An extension with the name " + name + " has already been registered ("
					+ alreadyRegisteredClass.getName() + ")");
		}
		
		try {
			Constructor<? extends GameExtension> constructor = extClass.getDeclaredConstructor();
			//Make the constructor accessible
			constructor.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Failed to find empty constructor on extension class", e);
		}
		
		if (annotation.hasCommands()) {
			commandManager.registerSpleefCommands(extClass);
		}
		
		registeredExtensions.put(name, extClass);
	}
	
	public Class<? extends GameExtension> getExtensionClass(String name) {
		return registeredExtensions.get(name);
	}
	
	public String getExtensionName(Class<? extends GameExtension> clazz) {
		return registeredExtensions.inverse().get(clazz);
	}
	
	public ExtensionManager newManagerInstance(EventManager eventManager) {
		ExtensionManager manager = new ExtensionManager(eventManager);
		return manager;
	}
	
}
