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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class EmptyInstantiator<T> implements Instantiator<T> {

	@Override
	public T newInstance(Class<T> clazz, InstantitatorArgs args) throws InstantiationException {
		try {
			Constructor<T> constr = clazz.getConstructor();
			return constr.newInstance();
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new InstantiationException(e.getMessage());
		}
	}

}
