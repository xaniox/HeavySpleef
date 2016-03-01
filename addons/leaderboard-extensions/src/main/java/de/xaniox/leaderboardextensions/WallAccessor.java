/*
 * This file is part of addons.
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
package de.xaniox.leaderboardextensions;

import de.xaniox.heavyspleef.persistence.xml.XMLAccessor;
import org.dom4j.Element;

public class WallAccessor extends XMLAccessor<ExtensionLeaderboardWall> {

	@Override
	public Class<ExtensionLeaderboardWall> getObjectClass() {
		return ExtensionLeaderboardWall.class;
	}

	@Override
	public void write(ExtensionLeaderboardWall object, Element element) {
		object.marshal(element);
	}

	@Override
	public ExtensionLeaderboardWall fetch(Element element) {
		ExtensionLeaderboardWall wall = new ExtensionLeaderboardWall();
		wall.unmarshal(element);
		return wall;
	}

}