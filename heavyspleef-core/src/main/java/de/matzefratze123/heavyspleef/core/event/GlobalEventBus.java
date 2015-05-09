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
package de.matzefratze123.heavyspleef.core.event;

import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;

public class GlobalEventBus {
	
	private final Logger logger;
	private Set<EventBus> singleInstanceBusMap;
	private Set<SpleefListener> globalListeners;
	
	public GlobalEventBus(Logger logger) {
		this.logger = logger;
		this.singleInstanceBusMap = Sets.newHashSet();
		this.globalListeners = Sets.newHashSet();
	}
	
	public void registerGlobalListener(SpleefListener listener) {
		Validate.isTrue(!globalListeners.contains(listener), "Global listener already registered");
		for (EventBus bus : singleInstanceBusMap) {
			if (bus.isRegistered(listener)) {
				throw new IllegalArgumentException("This listener has already been registered on a child EventBus");
			}
		}
		
		globalListeners.add(listener);
		for (EventBus bus : singleInstanceBusMap) {
			bus.registerListener(listener);
		}
	}
	
	public void unregisterGlobalListener(SpleefListener listener) {
		Validate.isTrue(globalListeners.contains(listener), "Global listener has not been registered");
		
		globalListeners.remove(listener);
		for (EventBus bus : singleInstanceBusMap) {
			bus.unregister(listener);
		}
	}
	
	public EventBus newChildBus() {
		EventBus bus = new EventBus(logger);
		
		singleInstanceBusMap.add(bus);
		for (SpleefListener listener : globalListeners) {
			bus.registerListener(listener);
		}
		
		return bus;
	}

}
