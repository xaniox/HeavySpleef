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

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class LocationFlag extends AbstractFlag<Location> {

	@Override
	public Location parseInput(Player player, String input) throws InputParseException {
		return player.getLocation();
	}
	
	@Override
	public void marshal(Element element) {
		Element worldElement = element.addElement("world");
		Element xElement = element.addElement("x");
		Element yElement = element.addElement("y");
		Element zElement = element.addElement("z");
		
		Location value = getValue();
		Validate.notNull(value, "getValue() cannot be null when marshalling flag value");
		
		worldElement.addText(value.getWorld().getName());
		xElement.addText(String.valueOf(value.getX()));
		yElement.addText(String.valueOf(value.getY()));
		zElement.addText(String.valueOf(value.getZ()));
	}
	
	@Override
	public void unmarshal(Element element) {
		Element worldElement = element.element("world");
		Element xElement = element.element("x");
		Element yElement = element.element("y");
		Element zElement = element.element("z");
		
		World world = Bukkit.getWorld(worldElement.getText());
		double x = Double.parseDouble(xElement.getText());
		double y = Double.parseDouble(yElement.getText());
		double z = Double.parseDouble(zElement.getText());
		
		Location location = new Location(world, x, y, z);
		setValue(location);
	}
	
}
