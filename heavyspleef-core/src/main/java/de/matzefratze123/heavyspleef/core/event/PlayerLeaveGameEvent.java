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
package de.matzefratze123.heavyspleef.core.event;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.QuitCause;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerLeaveGameEvent extends PlayerGameEvent implements Cancellable {

	private boolean cancelled;
	private Location teleportationLocation;
	private boolean sendMessages = true;
	private String playerMessage;
	private String broadcastMessage;
	private QuitCause cause;
	private SpleefPlayer killer;
	
	public PlayerLeaveGameEvent(Game game, SpleefPlayer player, SpleefPlayer killer, QuitCause cause) {
		super(game, player);
		
		this.killer = killer;
		this.cause = cause;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Location getTeleportationLocation() {
		return teleportationLocation;
	}

	public void setTeleportationLocation(Location teleportationLocation) {
		this.teleportationLocation = teleportationLocation;
	}

	public boolean isSendMessages() {
		return sendMessages;
	}

	public void setSendMessages(boolean sendMessages) {
		this.sendMessages = sendMessages;
	}

	public String getPlayerMessage() {
		return playerMessage;
	}

	public void setPlayerMessage(String playerMessage) {
		this.playerMessage = playerMessage;
	}

	public String getBroadcastMessage() {
		return broadcastMessage;
	}

	public void setBroadcastMessage(String broadcastMessage) {
		this.broadcastMessage = broadcastMessage;
	}

	public QuitCause getCause() {
		return cause;
	}

	public SpleefPlayer getKiller() {
		return killer;
	}
	
}
