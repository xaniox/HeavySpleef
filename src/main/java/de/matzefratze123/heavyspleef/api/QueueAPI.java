/*
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
 * */
 
package de.matzefratze123.heavyspleef.api;


import org.apache.commons.lang.Validate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.QueuesManager;

public class QueueAPI {
	
	/**
	 * Gets the instance
	 */
	public static QueueAPI getInstance() {
		return new QueueAPI();
	}
	
	/**
	 * Gets the gamedata of the queue
	 * from a player
	 * 
	 * @param player The player that is in queue
	 * @return The gamedata
	 */
	public GameData getQueue(Player player) {
		Validate.notNull(player);
		
		Game game = QueuesManager.getQueue(player);
		return new GameData(game);
	}
	
	/**
	 * Checks if a player is in a queue
	 * 
	 * @param player The player to check
	 */
	public boolean hasQueue(Player player) {
		Validate.notNull(player, "Player cannot be null");
		return QueuesManager.hasQueue(player);
	}
	
	/**
	 * Adds a player to a queue
	 * 
	 * @param player The player to add
	 * @param data The gamedata where the player should be in queue
	 * @param teamColor The team of the queue, may be null if this is no team game...
	 */
	public boolean addQueue(Player player, GameData data, ChatColor teamColor) {
		Validate.notNull(player, "Player cannot be null");
		Validate.notNull(data, "GameData cannot be null");
		
		QueuesManager.addToQueue(player, data.getHandle(), teamColor);
		return true;
	}
	
	/**
	 * Removes a player from the queue
	 * 
	 * @param player The player to remove
	 * @return True if the player was removed
	 */
	public boolean removeQueue(Player player) {
		Validate.notNull(player, "Player cannot be null");
		
		if (!hasQueue(player))
			return false;
		
		QueuesManager.removeFromQueue(player);
		return true;
	}
	
	/**
	 * Removes all players from a gamequeue
	 * @param data The gamedata
	 */
	public void removeAllQueues(GameData data) {
		Validate.notNull(data, "GameData cannot be null");
		data.getHandle().removeAllFromQueue();
	}
	
}
