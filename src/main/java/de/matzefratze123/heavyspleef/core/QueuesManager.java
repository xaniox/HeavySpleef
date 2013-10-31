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
package de.matzefratze123.heavyspleef.core;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static de.matzefratze123.heavyspleef.core.GameManager.*;

/**
 * Provides a global queue manager for all games
 * 
 * @author matzefratze123
 */
public class QueuesManager {

	public static boolean hasQueue(Player player) {
		boolean has = false;
		
		for (Game game : getGames()) {
			has = game.hasQueue(player);
			if (has) break;
		}
		
		return has;
	}
	
	public static void removeFromQueue(Player player) {
		for (Game game : getGames())
			game.removeFromQueue(player);
	}
	
	public static Game getQueue(Player player) {
		for (Game game : getGames()) {
			if (game.getQueue(player) != null)
				return game;
		}
		
		return null;
	}
	
	public static void addToQueue(Player player, Game game, ChatColor color) {
		if (game == null)
			return;
		
		game.addToQueue(player, color);
	}
	
}
