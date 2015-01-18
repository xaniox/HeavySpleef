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
package de.matzefratze123.heavyspleef.persistence;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class DatabaseContext<T extends ObjectDatabaseAccessor<?, ?>> {
	
	private Set<T> accessors;
	
	@SafeVarargs
	public DatabaseContext(T... accessors) {
		this.accessors = ImmutableSet.copyOf(accessors);
	}
	
	public DatabaseContext(Set<T> accessors) {
		this.accessors = ImmutableSet.copyOf(accessors);
	}
	
	protected <E> T searchAccessor(Class<E> objectClass) {
		for (T accessor : accessors) {
			if (accessor.getObjectClass().isAssignableFrom(objectClass)) {
				return accessor;
			}
		}
		
		throw new NoSuchAccessorException("The requested accessor for class \"" + objectClass.getClass().getCanonicalName() + "\" is not available");
	}
	
	protected Set<T> getAccessors() {
		return accessors;
	}
	
}
