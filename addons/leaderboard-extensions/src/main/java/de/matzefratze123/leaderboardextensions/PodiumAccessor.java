/*
 * This file is part of addons.
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
package de.matzefratze123.leaderboardextensions;

import org.dom4j.Element;

import de.matzefratze123.heavyspleef.persistence.xml.XMLAccessor;

public class PodiumAccessor extends XMLAccessor<ExtensionLeaderboardPodium> {

	@Override
	public Class<ExtensionLeaderboardPodium> getObjectClass() {
		return ExtensionLeaderboardPodium.class;
	}

	@Override
	public void write(ExtensionLeaderboardPodium object, Element element) {
		object.marshal(element);
	}

	@Override
	public ExtensionLeaderboardPodium fetch(Element element) {
		ExtensionLeaderboardPodium podium = new ExtensionLeaderboardPodium();
		podium.unmarshal(element);
		return podium;
	}
	
}
