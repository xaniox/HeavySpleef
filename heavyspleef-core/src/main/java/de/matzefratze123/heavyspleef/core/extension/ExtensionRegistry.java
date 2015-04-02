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

import java.util.Set;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.commands.base.CommandManager;
import de.matzefratze123.heavyspleef.core.event.EventManager;

public class ExtensionRegistry {
	
	private final CommandManager commandManager;
	private Set<Class<? extends GameExtension>> registeredExtensions;
	
	public ExtensionRegistry(CommandManager commandManager) {
		this.registeredExtensions = Sets.newHashSet();
		this.commandManager = commandManager;
	}
	
	public void registerExtension(Class<? extends GameExtension> extClass) {
		if (registeredExtensions.contains(extClass)) {
			throw new IllegalArgumentException("This extension type has already been registered");
		}
		
		if (extClass.isAnnotationPresent(CustomCommands.class)) {
			commandManager.registerSpleefCommands(extClass);
		}
		
		registeredExtensions.add(extClass);
	}
	
	public ExtensionManager newManagerInstance(EventManager eventManager) {
		ExtensionManager manager = new ExtensionManager(eventManager);
		return manager;
	}
	
}
