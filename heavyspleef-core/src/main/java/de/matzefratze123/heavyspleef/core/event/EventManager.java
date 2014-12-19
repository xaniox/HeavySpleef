package de.matzefratze123.heavyspleef.core.event;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

public class EventManager {
	
	private Set<EventListenerMethod> registeredEventListeners;
	
	public EventManager() {
		this.registeredEventListeners = Sets.newLinkedHashSet();
	}
	
	public void registerListener(SpleefListener listener) {
		Class<? extends SpleefListener> clazz = listener.getClass();
		
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (!method.isAnnotationPresent(GameListener.class)) {
				continue;
			}
			
			validateMethod(method);
			
			EventListenerMethod listenerMethodHolder = new EventListenerMethod(listener, method);
			registeredEventListeners.add(listenerMethodHolder);
		}
	}
	
	public void callEvent(GameEvent event) {
		registeredEventListeners.stream()
			.filter(method -> method.getGameEventType() == event.getClass())
			.forEach(method -> method.invoke(event));
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
