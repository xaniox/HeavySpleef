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
package de.matzefratze123.heavyspleef.flag.presets;

import org.dom4j.Element;

public abstract class EnumListFlag<T extends Enum<T>> extends ListFlag<T> {

	public abstract Class<T> getEnumType();
	
	@Override
	public void marshalListItem(Element element, T item) {
		element.addText(item.name());
	}

	@Override
	public T unmarshalListItem(Element element) {
		return Enum.valueOf(getEnumType(), element.getText());
	}

}
