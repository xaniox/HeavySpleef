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
package de.xaniox.heavyspleef.core.extension;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import de.xaniox.heavyspleef.commands.base.CommandManager;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Unregister;
import de.xaniox.heavyspleef.core.event.EventBus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ExtensionRegistry {
	
	private final HeavySpleef heavySpleef;
	private BiMap<String, Class<? extends GameExtension>> registeredExtensions;
	
	public ExtensionRegistry(HeavySpleef heavySpleef) {
		this.registeredExtensions = HashBiMap.create();
		this.heavySpleef = heavySpleef;
	}
	
	@SuppressWarnings("unchecked")
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
		
		Class<? extends GameExtension> currentClass = extClass;
		
		do {
			for (Method method : currentClass.getDeclaredMethods()) {
				if (!method.isAnnotationPresent(ExtensionInit.class)) {
					continue;
				}
				
				if ((method.getModifiers() & Modifier.STATIC) == 0) {
					throw new IllegalArgumentException("Init method " + method.getName() + " of type " + extClass.getCanonicalName() + " is not static!");
				}
				
				boolean accessible = method.isAccessible();
				if (!accessible) {
					method.setAccessible(true);
				}
				
				Class<?>[] parameters = method.getParameterTypes();
				Object[] args = new Object[parameters.length];
				
				for (int i = 0; i < parameters.length; i++) {
					Class<?> parameter = parameters[i];
					if (parameter == HeavySpleef.class) {
						args[i] = heavySpleef;
					}
				}
				
				try {
					method.invoke(null, args);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new IllegalArgumentException(
							"Cannot invoke init method " + method.getName() + " of type " + extClass.getCanonicalName() + ": ", e);
				} finally {
					method.setAccessible(accessible);
				}
			}
			
			currentClass = (Class<? extends GameExtension>) currentClass.getSuperclass();
		} while (!isRegistered(currentClass) && currentClass != GameExtension.class); 
		
		if (annotation.hasCommands()) {
			CommandManager manager = heavySpleef.getCommandManager();
			manager.registerSpleefCommands(extClass);
		}
		
		registeredExtensions.put(name, extClass);
	}
	
	private boolean isRegistered(Class<? extends GameExtension> clazz) {
		for (Class<? extends GameExtension> extClass : registeredExtensions.values()) {
			if (clazz.isAssignableFrom(extClass)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void unregister(Class<? extends GameExtension> extClass) {
		if (!registeredExtensions.containsValue(extClass)) {
			throw new IllegalArgumentException("Extension " + extClass.getCanonicalName() + " is not registered");
		}
		
		Unregister.Unregisterer.runUnregisterMethods(extClass, heavySpleef, true, true);
		Extension extAnnotation = extClass.getAnnotation(Extension.class);
		
		if (extAnnotation.hasCommands()) {
			CommandManager manager = heavySpleef.getCommandManager();
			manager.unregisterSpleefCommand(extClass);
		}
		
		registeredExtensions.inverse().remove(extClass);
	}
	
	public Class<? extends GameExtension> getExtensionClass(String name) {
		return registeredExtensions.get(name);
	}
	
	public String getExtensionName(Class<? extends GameExtension> clazz) {
		return registeredExtensions.inverse().get(clazz);
	}
	
	public ExtensionManager newManagerInstance(EventBus eventManager) {
		ExtensionManager manager = new ExtensionManager(heavySpleef, eventManager);
		return manager;
	}

	@SuppressWarnings("unchecked")
	public <T extends GameExtension> Set<Class<? extends T>> getExtensionsByType(Class<T> clazz) {
		Set<Class<? extends T>> set = Sets.newHashSet();
		for (Class<? extends GameExtension> extClass : registeredExtensions.values()) {
			if (clazz.isAssignableFrom(extClass)) {
				set.add((Class<? extends T>) extClass);
			}
		}
		
		return set;
	}
	
}