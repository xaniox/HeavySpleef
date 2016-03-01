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
package de.xaniox.bungeemode;

import de.xaniox.heavyspleef.core.event.GameEndEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.event.Subscribe.Priority;
import de.xaniox.heavyspleef.core.flag.BukkitListener;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.defaults.FlagSpectate;
import de.xaniox.heavyspleef.flag.presets.LocationFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

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
			
			player.teleport(teleportTo);
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