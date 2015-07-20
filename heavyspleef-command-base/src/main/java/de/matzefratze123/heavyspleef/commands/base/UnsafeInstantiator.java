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
package de.matzefratze123.heavyspleef.commands.base;

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
