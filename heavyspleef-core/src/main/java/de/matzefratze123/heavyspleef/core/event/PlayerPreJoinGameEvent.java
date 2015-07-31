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
import de.matzefratze123.heavyspleef.core.game.Game.JoinResult;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerPreJoinGameEvent extends PlayerGameEvent {

	private String message;
	private Location teleportationLocation;
	private JoinResult joinResult;
	
	public PlayerPreJoinGameEvent(Game game, SpleefPlayer player) {
		super(game, player);
		
		this.joinResult = JoinResult.ALLOW;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Location getTeleportationLocation() {
		return teleportationLocation;
	}

	public void setTeleportationLocation(Location teleportationLocation) {
		this.teleportationLocation = teleportationLocation;
	}

	public JoinResult getJoinResult() {
		return joinResult;
	}

	public void setJoinResult(JoinResult joinResult) {
		this.joinResult = joinResult;
	}

}
