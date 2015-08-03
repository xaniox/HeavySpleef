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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.Inject;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSpectate;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "teleport-all")
@BukkitListener
public class FlagTeleportAll extends LocationFlag {

	@Inject
	private BungeemodeAddon addon;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Teleport all players on the server to a specific location at the end of the game, clearing their inventory");
		description.add("This leaves the spectator mode if necessary");
		description.add("Intended for use with the bungee-mode");
	}
	
	@Subscribe(priority = Priority.HIGHEST)
	public void onGameEnd(GameEndEvent event) {
		Location teleportTo = getValue();
		Game game = event.getGame();
		
		FlagSpectate spectate = game.isFlagPresent(FlagSpectate.class) ? game.getFlag(FlagSpectate.class) : null;
		
		for (Player bukkitPlayer : Bukkit.getOnlinePlayers()) {
			SpleefPlayer player = getHeavySpleef().getSpleefPlayer(bukkitPlayer);
			
			if (spectate != null && spectate.isSpectating(player)) {
				addon.getSendBackExceptions().add(player);
				spectate.leave(player);
			}
			
			player.getBukkitPlayer().teleport(teleportTo);
			player.getBukkitPlayer().getInventory().clear();
			player.getBukkitPlayer().updateInventory();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		BungeemodeListener listener = addon.getListener();
		if (listener != null && listener.isRestarting()) {
			event.getPlayer().teleport(getValue());
		}
	}

}
