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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Sets;

public class EventBus {
	
	private final Logger logger;
	private Set<EventListenerMethod> registeredEventListeners;
	
	protected EventBus(Logger logger) {
		this.logger = logger;
		this.registeredEventListeners = Sets.newLinkedHashSet();
	}
	
	public void registerListener(SpleefListener listener) {
		Class<? extends SpleefListener> clazz = listener.getClass();
		
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (!method.isAnnotationPresent(Subscribe.class)) {
				continue;
			}
			
			validateMethod(method);
			
			EventListenerMethod listenerMethodHolder = new EventListenerMethod(listener, method);
			registeredEventListeners.add(listenerMethodHolder);
		}
	}
	
	public void unregister(SpleefListener listener) {
		Iterator<EventListenerMethod> methodIterator = registeredEventListeners.iterator();
		while (methodIterator.hasNext()) {
			EventListenerMethod method = methodIterator.next();
			if (!method.getDeclaringClass().isInstance(listener)) {
				continue;
			}
			
			methodIterator.remove();
		}
	}
	
	protected boolean isRegistered(SpleefListener listener) {
		return registeredEventListeners.contains(listener);
	}
	
	public void callEvent(GameEvent event) {
		for (EventListenerMethod method : registeredEventListeners) {
			if (!method.getDeclaringClass().isInstance(event)) {
				continue;
			}
			
			try {
				method.invoke(event);
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Could not pass " + event.getClass().getSimpleName() + " of class " + method.getDeclaringClass().getCanonicalName(), t);
			}
		}
	}
	
	private void validateMethod(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new IllegalGameListenerMethodException(method, "Method " + method.getName() + " in type " + method.getDeclaringClass().getCanonicalName()
					+ " has more or less than 1 parameter");
		}

		Class<?> parameterType = parameterTypes[0];
		if (!GameEvent.class.isAssignableFrom(parameterType)) {
			throw new IllegalGameListenerMethodException(method, "First parameter of method " + method.getName() + " in type "
					+ method.getDeclaringClass().getCanonicalName() + " is not a subtype of GameEvent");
		}
	}
	
}
