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
package de.matzefratze123.heavyspleef.persistence.xml;

import java.util.Set;

import org.dom4j.Element;

import de.matzefratze123.heavyspleef.persistence.DatabaseContext;

public class XMLContext extends DatabaseContext<XMLAccessor<?>> {
	
	@SafeVarargs
	public XMLContext(XMLAccessor<?>... accessors) {
		super(accessors);
	}
	
	public XMLContext(Set<XMLAccessor<?>> accessors) {
		super(accessors);
	}
	
	@SuppressWarnings("unchecked")
	public <T> void write(T object, Element element) {
		Class<T> clazz = (Class<T>) object.getClass();
		XMLAccessor<T> accessor = (XMLAccessor<T>) searchAccessor(clazz);
		
		accessor.write(object, element);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T read(Element element, Class<T> expected) {
		XMLAccessor<T> accessor = (XMLAccessor<T>) searchAccessor(expected);
		
		return accessor.fetch(element);
	}
	
}
