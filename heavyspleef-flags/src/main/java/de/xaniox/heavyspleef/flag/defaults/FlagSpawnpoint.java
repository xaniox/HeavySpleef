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
package de.xaniox.heavyspleef.flag.defaults;

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.event.GameCountdownEvent;
import de.xaniox.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.flag.presets.LocationFlag;
import org.bukkit.Location;

import java.util.List;

@Flag(name = "spawnpoint")
public class FlagSpawnpoint extends LocationFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the spawnpoint for players when a game starts to countdown");
	}
	
	@Subscribe
	public void onGameCountdown(GameCountdownEvent event) {
        Location location = getValue();
		if (location == null) {
			return;
		}
		
		event.setSpawnLocations(Lists.newArrayList(location));
	}

    @Subscribe
    public void onPlayerPreJoinGame(PlayerPreJoinGameEvent event) {
        Location location = getValue();
        if (location == null) {
            return;
        }

        event.setGameTeleportationLocation(location);
    }

}