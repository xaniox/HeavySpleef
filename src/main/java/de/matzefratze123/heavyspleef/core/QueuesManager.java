/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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

import static de.matzefratze123.heavyspleef.core.GameManager.getGames;

import org.bukkit.ChatColor;

import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

/**
 * Provides a global queue manager for all games
 * 
 * @author matzefratze123
 */
public class QueuesManager {

	public static boolean hasQueue(SpleefPlayer player) {
		boolean has = false;
		
		for (Game game : getGames()) {
			has = game.getQueue().contains(player);
			if (has) { 
				break;
			}
		}
		
		return has;
	}
	
	public static void removeFromQueue(SpleefPlayer player) {
		for (Game game : getGames()) {
			game.getQueue().removePlayer(player);
		}
	}
	
	public static Game getQueue(SpleefPlayer player) {
		for (Game game : getGames()) {
			if (game.getQueue().contains(player)) {
				return game;
			}
		}
		
		return null;
	}
	
	public static void addToQueue(SpleefPlayer player, Game game, ChatColor color) {
		if (game == null)
			return;
		
		game.getQueue().addPlayer(player);
	}
	
}
