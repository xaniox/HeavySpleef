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

import de.xaniox.heavyspleef.core.flag.AbstractFlag;
import de.xaniox.heavyspleef.core.flag.InputParseException;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.dom4j.Element;

public abstract class BaseFlag extends AbstractFlag<Void> {

	private static final String ENABLED = "Enabled";
	
	@Override
	public void marshal(Element element) {
		//SimpleFlags are not marshalled as they are either set or not
	}

	@Override
	public void unmarshal(Element element) {}

	@Override
	public Void parseInput(SpleefPlayer player, String input) throws InputParseException {
		return null;
	}

	@Override
	public String getValueAsString() {
		return ENABLED;
	}

}