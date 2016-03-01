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
package de.xaniox.heavyspleef.flag.presets;

import de.xaniox.heavyspleef.core.flag.InputParseException;
import de.xaniox.heavyspleef.flag.presets.DelimiterBasedListParser.Delimiters;
import org.dom4j.Element;

public abstract class EnumListFlag<T extends Enum<T>> extends ListFlag<T> {

	private final ListInputParser<T> parser = new DelimiterBasedListParser<>(Delimiters.SPACE_DELIMITER, new ArgumentParser<T>() {

		@Override
		public T parseArgument(String argument) throws InputParseException {
			try {
				return valueOf(argument);
			} catch (Exception e) {
				throw new InputParseException(argument, e);
			}
		}
		
		private T valueOf(String argument) {
			//Upper-case transform the argument
			argument = argument.toUpperCase();
			
			return Enum.valueOf(getEnumType(), argument);
		}
		
	});
	
	public abstract Class<T> getEnumType();
	
	@Override
	public String getListItemAsString(T item) {
		return item.name();
	}
	
	@Override
	public void marshalListItem(Element element, T item) {
		element.addText(item.name());
	}

	@Override
	public T unmarshalListItem(Element element) {
		return Enum.valueOf(getEnumType(), element.getText());
	}
	
	@Override
	public ListInputParser<T> createParser() {
		return parser;
	}

}