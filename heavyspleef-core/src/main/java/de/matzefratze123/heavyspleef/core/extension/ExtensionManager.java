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

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.core.event.EventManager;

public class ExtensionManager {
	
	private final EventManager eventManager;
	private Set<GameExtension> extensions;
	
	protected ExtensionManager(EventManager eventManager) {
		this.eventManager = eventManager;
		this.extensions = Sets.newHashSet();
	}
	
	public void addExtension(GameExtension extension) {
		if (extensions.contains(extension)) {
			throw new IllegalArgumentException("This extension has already been registered on this ExtensionManager");
		}
		
		eventManager.registerListener(extension);
		extensions.add(extension);
	}
	
	public void removeExtension(GameExtension extension) {
		eventManager.unregister(extension);
		extensions.remove(extension);
	}
	
	public Set<GameExtension> getExtensions() {
		return Collections.unmodifiableSet(extensions);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GameExtension> Set<T> getExtensionsByType(Class<T> extClass, boolean strict) {
		Set<T> typeSet = Sets.newHashSet();
		
		for (GameExtension ext : extensions) {
			if (strict && extClass != ext.getClass() || !extClass.isInstance(ext)) {
				continue;
			}
			
			typeSet.add((T) ext);
		}
		
		return typeSet;
	}
	
}
