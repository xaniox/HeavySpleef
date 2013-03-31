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
package me.matzefratze123.heavyspleef.api;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;

public class GameAPI {
	
	/**
	 * Gets an instance of the api
	 * @return An instance of the api
	 */
	public static GameAPI getInstance() {
		return new GameAPI();
	}
	
	/**
	 * Creates a new cuboid spleef game 
	 * 
	 * @param name The name of the spleef game
	 * @param corner1 The first corner of the game
	 * @param corner2 The second corner of the game
	 * 
	 * @return True if the game was successfully added
	 */
	public boolean createCuboidGame(String name, Location corner1, Location corner2) {
		if (name == null)
			return false;
		if (hasGame(name))
			return false;
		
		GameManager.createCuboidGame(name, corner1, corner2);
		return true;
	}
	
	/**
	 * Creates a new cylinder spleef game
	 * 
	 * @param name The name of the spleef game
	 * @param center The center of the cylinder arena
	 * @param radius The radius of the cylinder
	 * @param minY The minimum Y
	 * @param maxY The maximum Y
	 * 
	 * @return True if the game was successfully added
	 */
	public boolean createCylinderGame(String name, Location center, int radius, int minY, int maxY) {
		Validate.notNull(name);
		Validate.notNull(center);
		
		if (radius < 0 || minY < 0 || maxY > 256)
			return false;
		if (hasGame(name))
			return false;
		
		GameManager.createCylinderGame(name, center, radius, minY, maxY);
		return true;
	}
	
	/**
	 * Deletes a game with the given name
	 * 
	 * @param name The name of the game to be deleted
	 * @return True if the game was successfully removed
	 */
	public boolean delete(String name) {
		Validate.notNull(name);
		
		if (!hasGame(name))
			return false;
		
		GameManager.deleteGame(name);
		return true;
	}
	
	/**
	 * Deletes a game from the given gamedata
	 * 
	 * @param data The data to delete
	 * @return True if the game was successfully deleted
	 */
	public boolean delete(GameData data) {
		Validate.notNull(data);
		
		if (!hasGame(data.getGame().getName()))
			return false;
		
		GameManager.deleteGame(data.getGame().getName());
		return true;
	}
	
	/**
	 * Gets a game with the specified data
	 * 
	 * @param name The name of the arena
	 * @return A gamedata which can be modified
	 */
	public GameData getGameData(String name) {
		Validate.notNull(name);
		
		if (!hasGame(name))
			return null;
		
		return new GameData(name);
	}
	
	/**
	 * Gets a game from the given player
	 * This returns null if the player
	 * is not in a game
	 * 
	 * @param player The player in the game
	 * @return The gamedata
	 */
	public GameData getGameData(Player player) {
		Validate.notNull(player);
		
		Game game = GameManager.fromPlayer(player);
		return new GameData(game);
	}
	
	/**
	 * Gets all games
	 * 
	 * @return An array which contains all gamedatas
	 */
	public GameData[] getGameDatas() {
		Game[] games = GameManager.getGames();
		GameData[] datas = new GameData[games.length];
		
		for (int i = 0; i < games.length; i++)
			datas[i] = new GameData(games[i]);
		
		return datas;
	}
	
	/**
	 * Checks if a game exists
	 * 
	 * @param name The name to check
	 * @return True if the game exists, otherwise false
	 */
	public boolean hasGame(String name) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null");
		
		return GameManager.hasGame(name);
	}
	
	/**
	 * Gets the queue api
	 * 
	 * @return An instance of the queueapi
	 */
	public QueueAPI getQueueAPI() {
		return QueueAPI.getInstance();
	}
}
