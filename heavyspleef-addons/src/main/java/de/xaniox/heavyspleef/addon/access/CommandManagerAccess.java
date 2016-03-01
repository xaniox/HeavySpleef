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
package de.xaniox.heavyspleef.addon.access;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.xaniox.heavyspleef.addon.AddOn;
import de.xaniox.heavyspleef.commands.base.CommandManager;
import org.apache.commons.lang.Validate;

import java.util.Map;
import java.util.Set;

public class CommandManagerAccess {
	
	private Map<AddOn, Set<Class<?>>> registeredAddOnCommands;
	private CommandManager delegate;
	
	public CommandManagerAccess(CommandManager delegate) {
		this.delegate = delegate;
		this.registeredAddOnCommands = Maps.newHashMap();
	}
	
	public void registerSpleefCommand(Class<?> clazz, AddOn addOn) {
		Validate.isTrue(!isClassRegistered(clazz), "Command class " + clazz.getName() + " has already been registered");
		
		Set<Class<?>> classes = registeredAddOnCommands.get(addOn);
		if (classes == null) {
			classes = Sets.newHashSet();
			registeredAddOnCommands.put(addOn, classes);
		}
		
		classes.add(clazz);
		delegate.registerSpleefCommands(clazz);
	}
	
	public void unregisterSpleefCommand(Class<?> clazz) {
		for (Set<Class<?>> set : registeredAddOnCommands.values()) {
			if (set.contains(clazz)) {
				set.remove(clazz);
				delegate.unregisterSpleefCommand(clazz);
				return;
			}
		}
		
		throw new IllegalArgumentException("Command class " + clazz.getName() + " is not registered");
	}
	
	public void unregisterSpleefCommands(AddOn addOn) {
		Set<Class<?>> set = registeredAddOnCommands.get(addOn);
		if (set == null) {
			return;
		}
		
		for (Class<?> clazz : set) {
			delegate.unregisterSpleefCommand(clazz);
		}
		
		registeredAddOnCommands.remove(addOn);
	}
	
	private boolean isClassRegistered(Class<?> clazz) {
		for (Set<Class<?>> set : registeredAddOnCommands.values()) {
			if (set.contains(clazz)) {
				return true;
			}
		}
		
		return false;
	}
	
}