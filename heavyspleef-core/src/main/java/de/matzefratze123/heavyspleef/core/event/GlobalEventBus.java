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
