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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;

public class EventListenerMethod {
	
	private Object instance;
	private Method method;
	private Class<? extends GameEvent> eventClass;
	
	@SuppressWarnings("unchecked")
	public EventListenerMethod(Object instance, Method method) {
		this.instance = instance;
		this.method = method;
		
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		
		Class<?>[] parameters = method.getParameterTypes();
		Validate.isTrue(parameters.length == 1, "method must have only one parameter which must be a subtype of GameEvent");
		
		Class<?> eventClass = parameters[0];
		Validate.isTrue(GameEvent.class.isAssignableFrom(eventClass), "First parameter of method must be a subtype of GameEvent");
		
		this.eventClass = (Class<? extends GameEvent>) eventClass;
	}
	
	public String getMethodName() {
		return method.getName();
	}
	
	public Class<? extends GameEvent> getEventClass() {
		return eventClass;
	}
	
	public Class<?> getMethodClass() {
		return method.getDeclaringClass();
	}
	
	public void invoke(GameEvent event) {
		Validate.isTrue(eventClass.isAssignableFrom(event.getClass()), "event must be either " + eventClass.getName() + " or a subtype");
		
		try {
			method.invoke(instance, event);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
