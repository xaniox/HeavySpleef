/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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
package me.matzefratze123.heavyspleef.api.event;

import me.matzefratze123.heavyspleef.api.GameData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpleefJoinEvent extends Event {

	public static HandlerList handlers = new HandlerList();
	private GameData gameData = null;
	private Player player = null;
	
	public SpleefJoinEvent(GameData game, Player player) {
		this.player = player;
		this.gameData = game;
	}
	
	/**
	 * Gets the game that is involved in this event
	 * 
	 * @return The gamedata of the game
	 */
	public GameData getGame() {
		return this.gameData;
	}
	
	/**
	 * Gets the player that joined the game
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public HandlerList getHandlerList() {
		return handlers;
	}

	

}
