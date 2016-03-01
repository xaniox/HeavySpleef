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
package de.xaniox.heavyspleef.core.module;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModuleManager {
	
	private final Logger logger;
	private Map<Module, LoadPolicy.Lifecycle> modules;
	
	public ModuleManager(Logger logger) {
		this.logger = logger;
		this.modules = Maps.newLinkedHashMap();
	}
	
	public void registerModule(Module module) {
		LoadPolicy.Lifecycle lifecycle = LoadPolicy.Lifecycle.POST_LOAD;
		Class<? extends Module> clazz = module.getClass();
		
		if (clazz.isAnnotationPresent(LoadPolicy.class)) {
			LoadPolicy policy = clazz.getAnnotation(LoadPolicy.class);
			lifecycle = policy.value();
		}
		
		modules.put(module, lifecycle);
	}
	
	public void enableModules(LoadPolicy.Lifecycle lifecycle) {
		for (Entry<Module, LoadPolicy.Lifecycle> entry : modules.entrySet()) {
			if (entry.getValue() != lifecycle) {
				continue;
			}
			
			try {
				entry.getKey().enable();
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Unexpected exception occured while enabling module " + entry.getKey().getClass().getName(), t);
			}
		}
	}
	
	public void disableModules() {
		for (Module module : modules.keySet()) {
			try {
				module.disable();
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Unexpected exception occured while disabling module " + module.getClass().getName(), t);
			}
		}
	}

	public void reloadModules() {
		for (Module module : modules.keySet()) {
			try {
				module.reload();
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Unexpected exception occured while reloading module " + module.getClass().getName(), t);
			}
		}
	}

	public Module getModule(Class<? extends Module> clazz) {
		for (Module module : modules.keySet()) {
			if (module.getClass() != clazz) {
				continue;
			}
			
			return module;
		}
		
		return null;
	}
	
}