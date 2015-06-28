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
package de.matzefratze123.heavyspleef.addon.access;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.addon.AddOn;
import de.matzefratze123.heavyspleef.core.extension.ExtensionRegistry;
import de.matzefratze123.heavyspleef.core.extension.GameExtension;

public class ExtensionRegistryAccess {
	
	private ExtensionRegistry registry;
	private Map<AddOn, Set<Class<? extends GameExtension>>> addOnRegistrations;
	
	public ExtensionRegistryAccess(ExtensionRegistry registry) {
		this.registry = registry;
		this.addOnRegistrations = Maps.newHashMap();
	}
	
	public void registerExtension(Class<? extends GameExtension> extClass, AddOn addOn) {
		Set<Class<? extends GameExtension>> extensions = addOnRegistrations.get(addOn);
		if (extensions == null) {
			extensions = Sets.newHashSet();
			addOnRegistrations.put(addOn, extensions);
		}
		
		registry.registerExtension(extClass);
		extensions.add(extClass);
	}
	
	public void unregister(Class<? extends GameExtension> extClass) {
		Iterator<Set<Class<? extends GameExtension>>> iterator = addOnRegistrations.values().iterator();
		
		while (iterator.hasNext()) {
			Set<Class<? extends GameExtension>> set = iterator.next();
			if (!set.contains(extClass)) {
				continue;
			}
			
			registry.unregister(extClass);
			set.remove(extClass);
			
			if (set.isEmpty()) {
				iterator.remove();
			}
		}
	}
	
	public void unregister(AddOn addOn) {
		Set<Class<? extends GameExtension>> set = addOnRegistrations.remove(addOn);
		if (set == null) {
			return;
		}
		
		for (Class<? extends GameExtension> clazz : set) {
			registry.unregister(clazz);
		}
	}
	
}
