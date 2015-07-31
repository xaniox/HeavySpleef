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
package de.matzefratze123.heavyspleef.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Unregister {
	
	public static class Unregisterer {
		
		private Unregisterer() {
			throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
		}
		
		public static void runUnregisterMethods(Class<?> clazz, HeavySpleef heavySpleef, boolean validateStatic, boolean parents) {
			Class<?> currentClass = clazz;
			
			do {
				Method[] methods = currentClass.getDeclaredMethods();
					
				for (Method method : methods) {
					if (!method.isAnnotationPresent(Unregister.class)) {
						continue;
					}
					
					if ((method.getModifiers() & Modifier.STATIC) == 0) {
						if (validateStatic) {
							throw new IllegalArgumentException("Method " + method.getName() + " of class " + clazz.getCanonicalName() + " is not static");
						} else {
							continue;
						}
					}
					
					boolean accessible = method.isAccessible();
					
					try {
						if (!accessible) {
							method.setAccessible(true);
						}
						
						Class<?>[] parameters = method.getParameterTypes();
						Object[] args = null;
						
						if (parameters.length > 0 && parameters[0] == HeavySpleef.class) {
							args = new Object[] { heavySpleef };
						}
						
						method.invoke(null, args);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new IllegalArgumentException("Cannot invoke @Unregister method", e);
					} finally {
						method.setAccessible(accessible);
					}
				}
				currentClass = currentClass.getSuperclass();
			} while (parents && currentClass != null && currentClass != Object.class);
		}
		
	}
	
}
