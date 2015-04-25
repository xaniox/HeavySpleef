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
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;

public class FlagRegistryAccess {
	
	private FlagRegistry registry;
	private Map<AddOn, Set<Class<? extends AbstractFlag<?>>>> addOnRegistrations;
	
	public FlagRegistryAccess(FlagRegistry registry) {
		this.registry = registry;
		this.addOnRegistrations = Maps.newHashMap();
	}
	
	public void registerFlag(Class<? extends AbstractFlag<?>> flagClass, AddOn addOn) {
		Set<Class<? extends AbstractFlag<?>>> flags = addOnRegistrations.get(addOn);
		if (flags == null) {
			flags = Sets.newHashSet();
			addOnRegistrations.put(addOn, flags);
		}
		
		registry.registerFlag(flagClass);
		flags.add(flagClass);
	}
	
	public void unregister(Class<? extends AbstractFlag<?>> flagClass) {
		Iterator<Set<Class<? extends AbstractFlag<?>>>> iterator = addOnRegistrations.values().iterator();
		
		while (iterator.hasNext()) {
			Set<Class<? extends AbstractFlag<?>>> set = iterator.next();
			if (!set.contains(flagClass)) {
				continue;
			}
			
			registry.unregister(flagClass);
			set.remove(flagClass);
			
			if (set.isEmpty()) {
				iterator.remove();
			}
		}
	}
	
	public void unregister(AddOn addOn) {
		Set<Class<? extends AbstractFlag<?>>> set = addOnRegistrations.get(addOn);
		if (set == null) {
			return;
		}
		
		for (Class<? extends AbstractFlag<?>> clazz : set) {
			registry.unregister(clazz);
		}
	}
	
}
