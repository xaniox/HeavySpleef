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

import org.bukkit.entity.Player;
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class IntegerFlag extends AbstractFlag<Integer> {

	@Override
	public Integer parseInput(Player player, String input) throws InputParseException {
		int result;
		
		try {
			result = Integer.parseInt(input);
		} catch (NumberFormatException nfe) {
			throw new InputParseException(input);
		}
		
		return result;
	}
	
	@Override
	public void marshal(Element element) {
		element.addText(String.valueOf(getValue()));
	}
	
	@Override
	public void unmarshal(Element element) {
		int value = Integer.parseInt(element.getText());
		setValue(value);
	}
	
}
