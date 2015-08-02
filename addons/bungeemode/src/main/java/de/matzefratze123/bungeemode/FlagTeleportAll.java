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
package de.matzefratze123.bungeemode;

import java.util.List;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.Inject;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSpectate;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "teleport-all")
public class FlagTeleportAll extends LocationFlag {

	@Inject
	private BungeemodeAddon addon;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Teleport all players that participated in the game to a specific location at the end of it");
		description.add("This leaves the spectator mode if necessary");
		description.add("Intended for use with the bungee-mode");
	}
	
	@Subscribe(priority = Priority.HIGHEST)
	public void onGameEnd(GameEndEvent event) {
		Location teleportTo = getValue();
		Game game = event.getGame();
		
		FlagSpectate spectate = game.isFlagPresent(FlagSpectate.class) ? game.getFlag(FlagSpectate.class) : null;
		
		for (SpleefPlayer player : game.getDeadPlayers()) {
			if (!player.isOnline()) {
				continue;
			}
			
			if (spectate != null && spectate.isSpectating(player)) {
				addon.getSendBackExceptions().add(player);
				spectate.leave(player);
			}
			
			player.getBukkitPlayer().teleport(teleportTo);
		}
		
		//Also teleport all other spectators which were not involved in the game
		for (SpleefPlayer spectator : spectate.getSpectators()) {
			addon.getSendBackExceptions().add(spectator);
			spectate.leave(spectator);
			spectator.getBukkitPlayer().teleport(teleportTo);
		}
	}

}
