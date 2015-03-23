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
package de.matzefratze123.heavyspleef.core.config;

import org.bukkit.configuration.Configuration;

public abstract class ThrowingConfigurationObject<E extends Exception> extends ConfigurationObject {

	public ThrowingConfigurationObject(Configuration config) {
		super(config);
	}
	
	public ThrowingConfigurationObject(Configuration config, Object[] args) {
		super(config, args);
	}
	
	@Override
	public void inflate(Configuration config, Object... args) {
		try {
			inflateUnsafe(config, args);
		} catch (Exception e) {
			Class<? extends E> clazz = getExceptionClass();
			if (clazz.isInstance(e)) {
				throw new UnsafeException(e);
			} else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			
			//This part shouldn't be executed
		}
	}

	public abstract void inflateUnsafe(Configuration config, Object[] args) throws E;
	
	protected abstract Class<? extends E> getExceptionClass();
	
	public static class UnsafeException extends RuntimeException {

		private static final long serialVersionUID = -7780543786839198797L;
		
		public UnsafeException(Throwable cause) {
			super(cause);
		}
		
	}
	
}
