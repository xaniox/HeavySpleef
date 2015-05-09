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
package de.matzefratze123.heavyspleef.core.flag;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;

import org.apache.commons.lang.Validate;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.commands.base.CommandManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.collection.DualKeyBiMap;
import de.matzefratze123.heavyspleef.core.collection.DualKeyHashBiMap;
import de.matzefratze123.heavyspleef.core.collection.DualKeyMap.DualKeyPair;
import de.matzefratze123.heavyspleef.core.collection.DualMaps;
import de.matzefratze123.heavyspleef.core.collection.DualMaps.Mapper;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;

public class FlagRegistry {
	
	private static final String FLAG_PATH_SEPERATOR = ":";
	
	
	private final HeavySpleef heavySpleef;
	
	private final I18NSupplier GLOBAL_SUPPLIER = new I18NSupplier() {
		
		@Override
		public I18N supply() {
			return I18NManager.getGlobal();
		}
	};
	private final Mapper<FlagClassHolder, Class<? extends AbstractFlag<?>>> valueMapper = new Mapper<FlagRegistry.FlagClassHolder, Class<? extends AbstractFlag<?>>>() {

		@Override
		public Class<? extends AbstractFlag<?>> map(FlagClassHolder from) {
			return from.flagClass;
		}
	};
	private DualKeyBiMap<String, Flag, FlagClassHolder> registeredFlagsMap;
	private Queue<Method> queuedInitMethods;
	
	public FlagRegistry(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
		this.registeredFlagsMap = new DualKeyHashBiMap<String, Flag, FlagClassHolder>(String.class, Flag.class);
		this.queuedInitMethods = Lists.newLinkedList();
	}
	
	public void registerFlag(Class<? extends AbstractFlag<?>> clazz) {
		registerFlag(clazz, null);
	}
	
	public void registerFlag(Class<? extends AbstractFlag<?>> clazz, I18NSupplier i18nSupplier) {
		Validate.notNull(clazz, "clazz cannot be null");
		Validate.isTrue(!isFlagPresent(clazz), "Cannot register flag twice");
		
		if (i18nSupplier == null) {
			i18nSupplier = GLOBAL_SUPPLIER;
		}
		
		/* Check if the class provides the required Flag annotation */
		Validate.isTrue(clazz.isAnnotationPresent(Flag.class), "Flag-Class must be annotated with the @Flag annotation");
		
		Flag flagAnnotation = clazz.getAnnotation(Flag.class);
		String name = flagAnnotation.name();
		
		Validate.isTrue(!name.isEmpty(), "name() of annotation of flag for class " + clazz.getCanonicalName() + " cannot be empty");
		
		/* Generate a path */
		StringBuilder pathBuilder = new StringBuilder();
		Flag parentFlagData = flagAnnotation;
		
		do {
			pathBuilder.insert(0, parentFlagData.name());
			
			Class<? extends AbstractFlag<?>> parentFlagClass = parentFlagData.parent();
			parentFlagData = parentFlagClass.getAnnotation(Flag.class);
			
			if (parentFlagData != null && parentFlagClass != NullFlag.class) {
				pathBuilder.insert(0, FLAG_PATH_SEPERATOR);
			}
		} while (parentFlagData != null);
		
		String path = pathBuilder.toString();
		
		/* Check for name collides */
		for (String flagPath : registeredFlagsMap.primaryKeySet()) {
			if (flagPath.equalsIgnoreCase(path)) {
				throw new IllegalArgumentException("Flag " + clazz.getName() + " collides with " + registeredFlagsMap.get(flagPath).flagClass.getName());
			}
		}
		
		/* Check if the class can be instantiated */
		try {
			Constructor<? extends AbstractFlag<?>> constructor = clazz.getDeclaredConstructor();
			
			//Make the constructor accessible for future uses
			constructor.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Flag-Class must provide an empty constructor");
		}
		
		HookManager hookManager = heavySpleef.getHookManager();
		boolean allHooksPresent = true;
		for (HookReference ref : flagAnnotation.depend()) {
			if (!hookManager.getHook(ref).isProvided()) {
				allHooksPresent = false;
			}
		}
		
		if (allHooksPresent) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (!method.isAnnotationPresent(FlagInit.class)) {
					continue;
				}
				
				if ((method.getModifiers() & Modifier.STATIC) == 0) {
					throw new IllegalArgumentException("Flag initialization method " + method.getName() + " in type " + clazz.getCanonicalName()
							+ " is not declared as static");
				}
				
				queuedInitMethods.add(method);
			}
			
			if (flagAnnotation.hasCommands()) {
				CommandManager manager = heavySpleef.getCommandManager();
				manager.registerSpleefCommands(clazz);
			}
		}
		
		FlagClassHolder holder = new FlagClassHolder();
		holder.flagClass = clazz;
		holder.supplier = i18nSupplier;
		
		registeredFlagsMap.put(path, flagAnnotation, holder);
	}
	
	public void unregister(Class<? extends AbstractFlag<?>> flagClass) {
		String path = null;
		
		for (Entry<DualKeyPair<String, Flag>, FlagClassHolder> entry : registeredFlagsMap.entrySet()) {
			FlagClassHolder holder = entry.getValue();
			
			if (holder.flagClass != flagClass) {
				continue;
			}
			
			Flag annotation = entry.getKey().getSecondaryKey();
			if (annotation.hasCommands()) {
				CommandManager manager = heavySpleef.getCommandManager();
				manager.unregisterSpleefCommand(flagClass);
			}
			
			Iterator<Method> methodIterator = queuedInitMethods.iterator();
			while (methodIterator.hasNext()) {
				Method method = methodIterator.next();
				if (method.getDeclaringClass() == flagClass) {
					methodIterator.remove();
				}
			}
			
			path = entry.getKey().getPrimaryKey();
			break;
		}
		
		if (path != null) {
			registeredFlagsMap.remove(path);
		}
	}
	
	public void flushAndExecuteInitMethods() {
		while (!queuedInitMethods.isEmpty()) {
			Method method = queuedInitMethods.poll();
			
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
				throw new IllegalArgumentException("Could not invoke flag initialization method " + method.getName() + " of type "
						+ method.getDeclaringClass().getCanonicalName() + ": ", e);
			} finally {
				method.setAccessible(accessible);
			}
		}
	}
	
	public Flag getFlagData(Class<? extends AbstractFlag<?>> clazz) {
		for (Entry<FlagClassHolder, DualKeyPair<String, Flag>> entry : registeredFlagsMap.inverse().entrySet()) {
			FlagClassHolder holder = entry.getKey();
			if (clazz != holder.flagClass) {
				continue;
			}
			
			return entry.getValue().getSecondaryKey();
		}
		
		throw new NoSuchFlagException(clazz.getName());
	}
	
	public boolean isFlagPresent(String flagPath) {
		return getFlagClass(flagPath) != null;
	}
	
	public boolean isFlagPresent(Class<? extends AbstractFlag<?>> flagClass) {
		for (FlagClassHolder holder : registeredFlagsMap.values()) {
			if (holder.flagClass == flagClass) {
				return true;
			}
		}
		
		return false;
	}
	
	/* Reverse path lookup */
	public Class<? extends AbstractFlag<?>> getFlagClass(String flagPath) {
		FlagClassHolder holder = registeredFlagsMap.get(flagPath);
		if (holder == null) {
			return null;
		}
		
		return holder.flagClass;
	}
	
	public DualKeyBiMap<String, Flag, Class<? extends AbstractFlag<?>>> getAvailableFlags() {
		return DualMaps.valueMappedImmutableDualBiMap(registeredFlagsMap, valueMapper);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractFlag<?>> T newFlagInstance(String name, Class<T> expected) {
		FlagClassHolder holder = registeredFlagsMap.get(name);
		if (holder == null) {
			throw new NoSuchFlagException(name);
		}
		
		if (expected == null || !expected.isAssignableFrom(holder.flagClass)) {
			throw new NoSuchFlagException("Expected class " + expected.getName() + " is not compatible with " + holder.flagClass.getName());
		}
		
		try {
			AbstractFlag<?> flag = holder.flagClass.newInstance();
			flag.setHeavySpleef(heavySpleef);
			flag.setI18N(holder.supplier.supply());
			
			return (T) flag;
		} catch (InstantiationException | IllegalAccessException e) {
			//This should not happen as we made the constructor
			//accessible while the class was registered
			
			//But to be sure throw a RuntimeException
			throw new RuntimeException(e);
		}
	}
	
	public boolean isChildFlag(Class<? extends AbstractFlag<?>> parent, Class<? extends AbstractFlag<?>> childCandidate) {
		Validate.notNull(parent, "Parent cannot be null");
		Validate.notNull(childCandidate, "Child candidate cannot be null");
		
		BiMap<FlagClassHolder, DualKeyPair<String, Flag>> inverse = registeredFlagsMap.inverse();
		FlagClassHolder foundHolder = null;
		
		for (FlagClassHolder holder : inverse.keySet()) {
			if (holder.flagClass == childCandidate) {
				foundHolder = holder;
				break;
			}
		}
		
		Validate.isTrue(foundHolder != null, "Child candidate flag " + childCandidate.getName() + " has not been registered");
		
		Flag annotation = inverse.get(childCandidate).getSecondaryKey();
		
		Validate.isTrue(annotation != null, "ChildCandidate has not been registered");
		return annotation.parent() != null && annotation.parent() != NullFlag.class && annotation.parent() == childCandidate;
	}
	
	public static interface I18NSupplier {
		
		public I18N supply();
		
	}
	
	private class FlagClassHolder {
		
		private Class<? extends AbstractFlag<?>> flagClass;
		private I18NSupplier supplier;
		
	}
	
}
