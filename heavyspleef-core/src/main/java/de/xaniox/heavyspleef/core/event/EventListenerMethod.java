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

import org.apache.commons.lang.Validate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventListenerMethod implements Comparable<EventListenerMethod> {
	
	private Object instance;
	private Method method;
	private Subscribe subscribe;
	private Class<? extends Event> eventClass;
	
	@SuppressWarnings("unchecked")
	public EventListenerMethod(Object instance, Method method) {
		this.instance = instance;
		this.method = method;
		
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		
		subscribe = method.getAnnotation(Subscribe.class);
		
		Class<?>[] parameters = method.getParameterTypes();
		Validate.isTrue(parameters.length == 1, "method must have only one parameter which must be a subtype of Event");
		
		Class<?> eventClass = parameters[0];
		Validate.isTrue(Event.class.isAssignableFrom(eventClass), "First parameter of method must be a subtype of Event");
		
		this.eventClass = (Class<? extends Event>) eventClass;
	}
	
	public String getMethodName() {
		return method.getName();
	}
	
	public Class<? extends Event> getEventClass() {
		return eventClass;
	}
	
	public Class<?> getMethodClass() {
		return method.getDeclaringClass();
	}
	
	public void invoke(Event event) {
		Validate.isTrue(eventClass.isAssignableFrom(event.getClass()), "event must be either " + eventClass.getName() + " or a subtype");
		
		try {
			method.invoke(instance, event);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int compareTo(EventListenerMethod o) {
		int thisPriority = subscribe.priority().getOrderId();
		int otherPriority = o.subscribe.priority().getOrderId();
		
		//Don't return 0, as we're using a set (a bit hacky)
		if (thisPriority < otherPriority) {
			return -1;
		} else {
			return 1;
		}
	}
	
}