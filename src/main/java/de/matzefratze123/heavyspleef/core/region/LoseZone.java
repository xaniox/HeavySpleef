/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.core.region;


import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;

import de.matzefratze123.heavyspleef.database.DatabaseSerializeable;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.util.LocationHelper;

public class LoseZone extends RegionCuboid implements DatabaseSerializeable {
	
	public LoseZone(Location loc1, Location loc2, int id) {
		super(id, loc1, loc2);
	}

	public String asInfo() {
		return "ID: " + getId() + ", " + LocationHelper.toFriendlyString(firstPoint) + "; " + LocationHelper.toFriendlyString(secondPoint);
	}

	@Override
	public ConfigurationSection serialize() {
		MemorySection section = new MemoryConfiguration();
		
		section.set("id", id);
		section.set("first", Parser.convertLocationtoString(firstPoint));
		section.set("second", Parser.convertLocationtoString(secondPoint));
		
		return section;
	}
	
	public static LoseZone deserialize(ConfigurationSection section) {
		int id = section.getInt("id");
		Location first = Parser.convertStringtoLocation(section.getString("first"));
		Location second = Parser.convertStringtoLocation(section.getString("second"));
		
		return new LoseZone(first, second, id);
	}
	
}
