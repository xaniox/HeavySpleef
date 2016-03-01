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
package de.xaniox.heavyspleef.core.flag;

import java.lang.reflect.Field;

/**
 * Defines an injector to inject certain values
 * into fields of a class annotated with {@link Inject}
 * 
 * @author matzefratze123
 * @param <T> The type of object this class injects into
 */
public interface Injector<T> {
	
	/**
	 * Called to inject values into fields of a specific class<br><br>
	 * 
	 * <b>Note:</b> The fields given are already accessible so you 
	 * do not have to call {@link Field#setAccessible(boolean)} again
	 * 
	 * @param instance An instance containing the fields
	 * @param injectableFields The fields to be value-injected
	 * @param cookie An optional object given by the caller
	 * 
	 * @throws IllegalArgumentException When the false argument is given
	 * @throws IllegalAccessException When access to the field fails
	 */
	public void inject(T instance, Field[] injectableFields, Object cookie) throws IllegalArgumentException, IllegalAccessException;

}