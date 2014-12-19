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
	
	public Class<? extends GameEvent> getGameEventType() {
		return eventClass;
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
