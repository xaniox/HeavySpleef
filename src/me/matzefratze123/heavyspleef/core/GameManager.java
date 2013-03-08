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
package me.matzefratze123.heavyspleef.core;

import java.util.HashMap;
import java.util.Map;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GameManager {

	protected static Map<String, Integer> tasks = new HashMap<String, Integer>();
	
	public static Map<String, Integer> antiCamping = new HashMap<String, Integer>();
	public static Map<String, Game> games = new HashMap<String, Game>();
	public static Map<String, String> queues = new HashMap<String, String>();
	
	public static Game getGame(String id) {
		id = id.toLowerCase();
		return games.get(id);
	}
	
	public static Game[] getGames() {
		return games.values().toArray(new Game[games.size()]);
	}
	
	public static Game createCuboidGame(String id, Location firstCorner, Location secondCorner) {
		games.put(id, new GameCuboid(firstCorner, secondCorner, id));
		if (HeavySpleef.instance.getConfig().getBoolean("general.generateArena"))
			getGame(id).generate();
		return getGame(id);
	}
	
	public static Game createCylinderGame(String id, Location center, int radius, int minY, int maxY) {
		games.put(id, new GameCylinder(id, center, radius, minY, maxY));
		if (HeavySpleef.instance.getConfig().getBoolean("general.generateArena"))
			getGame(id).generate();
		return getGame(id);
	}
	
	public static void deleteGame(String id) {
		games.remove(id);
	}
	
	public static boolean hasGame(String id) {
		id = id.toLowerCase();
		return games.containsKey(id);
	}
	
	protected static int getTaskID(String id) {
		return tasks.get(id);
	}
	
	public static boolean isInAnyGame(Player p) {
		for (Game game : getGames()) {
			Player[] players = game.getPlayers();
			for (Player pl : players) {
				if (pl.getName().equalsIgnoreCase(p.getName()))
					return true;
			}
		}
		return false;
	}
	
	public static Game getGameFromPlayer(Player p) {
		for (Game game : getGames()) {
			Player[] players = game.getPlayers();
			for (Player pl : players) {
				if (pl.getName().equalsIgnoreCase(p.getName()))
					return game;
			}
		}
		return null;
	}
	
	public static void addQueue(Player p, String gameName) {
		gameName = gameName.toLowerCase();
		if (queues.containsKey(p.getName()))
			p.sendMessage(Game._("leftQueue", queues.get(p.getName())));
		p.sendMessage(Game._("addedToQueue", gameName));
		queues.put(p.getName(), gameName);
	}
	
	public static void removeFromQueue(Player player) {
		queues.remove(player.getName());
		player.sendMessage(Game._("noLongerInQueue"));
	}
	
	public static boolean isInQueue(Player p) {
		return queues.containsKey(p.getName());
	}
	
	public static Game getQueue(Player p) {
		return getGame(queues.get(p.getName()));
	}
	
	public static void removeAllPlayersFromGameQueue(String gameName) {
		gameName = gameName.toLowerCase();
		for (String player : queues.keySet()) {
			if (queues.get(player).equalsIgnoreCase(gameName))
				queues.remove(player);
		}
	}

	public static boolean isInAnyGameIngame(Player p) {
		for (Game game : getGames()) {
			if (!game.isIngame())
				continue;
			Player[] players = game.getPlayers();
			for (Player pl : players) {
				if (pl.getName().equalsIgnoreCase(p.getName()))
					return true;
			}
		}
		return false;
	}
}
