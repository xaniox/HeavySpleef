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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.flag.NullFlag;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;
import de.matzefratze123.heavyspleef.flag.presets.LocationListFlag;

@Flag(name = "multi-spawn", ignoreParseException = true)
public class FlagMultiSpawnpoint extends LocationListFlag {

	public FlagMultiSpawnpoint() {
		List<Location> list = Lists.newArrayList();
		setValue(list);
	}
	
	@Override
	public List<Location> parseInput(Player player, String input) throws InputParseException {
		throw new InputParseException("Use multi-spawn:add to add a spawnpoint and multi-spawn:remove to remove the recent spawnpoint");
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines multiple spawnpoints for players");
	}
	
	@Subscribe
	public void onGameCountdown(GameCountdownEvent event) {
		List<Location> list = getValue();
		event.setSpawnLocations(list);
	}
	
	@Flag(name = "add", parent = FlagMultiSpawnpoint.class)
	public static class FlagAddSpawnpoint extends LocationFlag {
		
		@Override
		public void setValue(Location value) {
			FlagMultiSpawnpoint parent = (FlagMultiSpawnpoint) getParent();
			parent.add(value);
		}

		@Override
		public void getDescription(List<String> description) {
			description.add("Adds a spawnpoint to the list of spawnpoints");
		}
		
	}
	
	@Flag(name = "remove", parent = FlagMultiSpawnpoint.class)
	public static class FlagRemoveSpawnpoint extends NullFlag {
		
		@Override
		public void setValue(Void value) {
			FlagMultiSpawnpoint parent = (FlagMultiSpawnpoint) getParent();
			int lastIndex = parent.size() - 1;
			
			parent.remove(lastIndex);
		}
		
		@Override
		public void getDescription(List<String> description) {
			description.add("Removes the recent spawnpoint");
		}
		
	}
	
}