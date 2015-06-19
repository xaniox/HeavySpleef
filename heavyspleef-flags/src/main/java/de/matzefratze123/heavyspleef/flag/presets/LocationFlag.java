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
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public abstract class LocationFlag extends AbstractFlag<Location> {

	@Override
	public Location parseInput(SpleefPlayer player, String input) throws InputParseException {
		Location playerLocation = player.getBukkitPlayer().getLocation();
		
		double[] coords = new double[3];
		coords[0] = playerLocation.getX();
		coords[1] = playerLocation.getY();
		coords[2] = playerLocation.getZ();
		
		String[] args = input.split(" ");
		
		if (args.length >= 3) {
			for (int i = 0; i < coords.length; i++) {
				// Use ~ for a relative coordinate
				boolean relative = args[i].startsWith("~");
				if (relative) {
					args[i] = args[i].substring(1);
				}

				double result = 0;
				
				if (!args[i].isEmpty()) {
					try {
						result = Double.parseDouble(args[i]);
					} catch (NumberFormatException nfe) {
						throw new InputParseException(args[i], getI18N().getString(Messages.Player.NOT_A_NUMBER));
					}
				}
				
				if (relative) {
					coords[i] = coords[i] + result;
				} else {
					coords[i] = result;
				}
			}
		}
		
		return new Location(playerLocation.getWorld(), coords[0], coords[1], coords[2]);
	}
	
	@Override
	public String getValueAsString() {
		Location location = getValue();
		
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(location.getBlockX());
		builder.append(',');
		builder.append(location.getBlockY());
		builder.append(',');
		builder.append(location.getBlockZ());
		
		if (location.getYaw() != 0f || location.getPitch() != 0f) {
			builder.append(",");
			builder.append(location.getYaw());
			builder.append(",");
			builder.append(location.getPitch());
		}
		
		builder.append(')');
		
		return builder.toString();
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
		
		if (value.getYaw() != 0f) {
			element.addElement("yaw").addText(String.valueOf(value.getYaw()));
		}
		if (value.getPitch() != 0f) {
			element.addElement("pitch").addText(String.valueOf(value.getPitch()));
		}
	}
	
	@Override
	public void unmarshal(Element element) {
		Element worldElement = element.element("world");
		Element xElement = element.element("x");
		Element yElement = element.element("y");
		Element zElement = element.element("z");
		Element yawElement = element.element("yaw");
		Element pitchElement = element.element("pitch");
		
		World world = Bukkit.getWorld(worldElement.getText());
		double x = Double.parseDouble(xElement.getText());
		double y = Double.parseDouble(yElement.getText());
		double z = Double.parseDouble(zElement.getText());
		float yaw = 0f;
		float pitch = 0f;
		
		if (yawElement != null) {
			yaw = Float.parseFloat(yawElement.getText());
		}
		if (pitchElement != null) {
			pitch = Float.parseFloat(pitchElement.getText());
		}
		
		Location location = new Location(world, x, y, z, yaw, pitch);
		setValue(location);
	}
	
}
