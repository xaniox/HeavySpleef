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

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.flag.AbstractFlag;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.dom4j.Element;

import java.util.List;

public abstract class BooleanFlag extends AbstractFlag<Boolean> {

	private static final List<String> TRUE_MATCHING_KEYWORDS = Lists.newArrayList("true", "yes", "on", "enable", "allow");
	
	@Override
	public Boolean parseInput(SpleefPlayer player, String input) {
		if (input.isEmpty()) {
			//Empty input is true as the user may forget
			//to actually define wether he wants to enable the flag
			return true;
		}
		
		boolean bool = false;
		for (String keyword : TRUE_MATCHING_KEYWORDS) {
			if (keyword.equals(input)) {
				bool = true;
			}
		}
		
		return Boolean.valueOf(bool);
	}
	
	@Override
	public String getValueAsString() {
		return String.valueOf(getValue());
	}
	
	@Override
	public void marshal(Element element) {
		element.addText(String.valueOf(getValue()));
	}
	
	@Override
	public void unmarshal(Element element) {
		boolean value = Boolean.parseBoolean(element.getText());
		
		setValue(value);
	}
	
}