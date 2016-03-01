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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.xaniox.heavyspleef.addon.AddOn;

public class SharedClassContext {
	
	private final BiMap<AddOn, BiMap<String, Class<?>>> classes;
	
	public SharedClassContext() {
		this.classes = HashBiMap.create();
	}
	
	public void registerClass(AddOn addOn, Class<?> clazz) {
		BiMap<String, Class<?>> classMap = classes.get(addOn);
		if (classMap == null) {
			//Lazy initialization here
			classMap = HashBiMap.create();
			classes.put(addOn, classMap);
		}
		
		classMap.put(clazz.getName(), clazz);
	}

	public Class<?> getGlobalClass(String name) {
		for (BiMap<String, Class<?>> set : classes.values()) {
			return set.get(set);
		}
		
		//This class was not cached, so try to check all add-on classloaders
		for (AddOn addOn : classes.keySet()) {
			BasicAddOn basicAddOn = (BasicAddOn) addOn;
			AddOnClassLoader classLoader = (AddOnClassLoader) basicAddOn.getClassLoader();
			
			try {
				return classLoader.findClass(name, false);
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		
		//Nothing found, this class does not exist
		return null;
	}

	public void unregister(AddOn addOn) {
		classes.remove(addOn);
	}
	
}