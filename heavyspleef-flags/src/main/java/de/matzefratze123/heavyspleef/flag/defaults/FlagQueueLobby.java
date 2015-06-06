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
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.PlayerEnterQueueEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveQueueEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.Inject;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "queuelobby")
@BukkitListener
public class FlagQueueLobby extends LocationFlag {

	@Inject
	private Game game;
	private Map<SpleefPlayer, Location> previousLocations;
	
	public FlagQueueLobby() {
		this.previousLocations = Maps.newHashMap();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Teleports queued players into a lobby where they cannot teleport until they left the queue");
	}
	
	@Subscribe
	public void onQueueEnter(PlayerEnterQueueEvent event) {
		Location teleportPoint = getValue();
		
		SpleefPlayer player = event.getPlayer();
		Player bukkitPlayer = player.getBukkitPlayer();
		
		Location now = bukkitPlayer.getLocation();
		previousLocations.put(player, now);
		bukkitPlayer.teleport(teleportPoint);
	}
	
	@Subscribe
	public void onQueueLeave(PlayerLeaveQueueEvent event) {
		SpleefPlayer player = event.getPlayer();
		Location previous = previousLocations.get(player);
		
		if (previous != null) {
			player.getBukkitPlayer().teleport(previous);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		
		if (!game.isQueued(player)) {
			return;
		}
		
		event.setCancelled(true);
		player.sendMessage(getI18N().getString(Messages.Player.CANNOT_TELEPORT_IN_QUEUE));
	}

}
