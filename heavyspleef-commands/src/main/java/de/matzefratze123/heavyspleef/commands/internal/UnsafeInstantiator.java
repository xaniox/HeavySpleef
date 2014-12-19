package de.matzefratze123.heavyspleef.commands.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UnsafeInstantiator implements Instantiator {

	private static Object theUnsafe;
	private static Method allocateInstanceMethod;
	private static Exception failCause;
	
	static {
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			
			theUnsafe = theUnsafeField.get(null);
			allocateInstanceMethod = unsafeClass.getDeclaredMethod("allocateInstance", Class.class);
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException e) {
			failCause = e;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T instantiate(Class<T> clazz) throws InstantiationException {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | java.lang.InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			//Go trough and ignore this exception
		}
		
		//Instantiation failed, try to force instantiation with sun.misc.Unsafe
		
		if (theUnsafe == null || failCause != null) {
			throw new IllegalStateException("cannot get sun.misc.Unsafe", failCause);
		}
		
		T instance;
		
		try {
			instance = (T) allocateInstanceMethod.invoke(theUnsafe, clazz);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			//Give up, we cannot instantiate this class
			throw new InstantiationException(clazz, "cannot instantiate class: ", e);
		}
		
		return instance;
	}

}
