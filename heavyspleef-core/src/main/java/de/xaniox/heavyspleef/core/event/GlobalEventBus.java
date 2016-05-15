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
package de.xaniox.heavyspleef.core.event;

import com.google.common.collect.Sets;
import org.apache.commons.lang.Validate;

import java.util.Set;
import java.util.logging.Logger;

public class GlobalEventBus extends EventBus {
	
	private final Logger logger;
	private Set<EventBus> singleInstanceBusMap;
	private Set<SpleefListener> globalListeners;
	
	public GlobalEventBus(Logger logger) {
		super(logger);
		
		this.logger = logger;
		this.singleInstanceBusMap = Sets.newHashSet();
		this.globalListeners = Sets.newHashSet();
	}

    @Override
    public void registerListener(SpleefListener listener) {
        registerListener(listener, false);
    }
	
	@Override
	public void registerListener(SpleefListener listener, boolean registerSuper) {
		Validate.isTrue(!globalListeners.contains(listener), "Global listener already registered");
		for (EventBus bus : singleInstanceBusMap) {
			if (bus.isRegistered(listener)) {
				throw new IllegalArgumentException("This listener has already been registered on a child EventBus");
			}
		}
		
		globalListeners.add(listener);
		for (EventBus bus : singleInstanceBusMap) {
			bus.registerListener(listener, registerSuper);
		}
	}
	
	@Override
	public void unregister(SpleefListener listener) {
		Validate.isTrue(globalListeners.contains(listener), "Global listener has not been registered");
		
		globalListeners.remove(listener);
		for (EventBus bus : singleInstanceBusMap) {
			bus.unregister(listener);
		}
	}
	
	@Override
	public void callEvent(Event event) {}
	
	public EventBus newChildBus() {
		EventBus bus = new EventBus(logger);
		
		singleInstanceBusMap.add(bus);
		for (SpleefListener listener : globalListeners) {
			bus.registerListener(listener);
		}
		
		return bus;
	}

}