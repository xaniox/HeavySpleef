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

import java.text.DecimalFormat;

import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public abstract class DoubleFlag extends AbstractFlag<Double> {
	
	private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
	
	@Override
	public Double parseInput(SpleefPlayer player, String input) throws InputParseException {
		double result;
		
		try {
			result = Double.parseDouble(input);
		} catch (NumberFormatException nfe) {
			throw new InputParseException(input);
		}
		
		return Double.valueOf(result);
	}
	
	@Override
	public String getValueAsString() {
		return FORMAT.format(getValue().doubleValue());
	}
	
	@Override
	public void marshal(Element element) {
		element.addText(String.valueOf(getValue()));
	}
	
	@Override
	public void unmarshal(Element element) {
		double value = Double.parseDouble(element.getText());
		setValue(value);
	}
	
}
