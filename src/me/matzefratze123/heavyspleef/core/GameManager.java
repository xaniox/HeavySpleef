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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GameManager {

	protected static Map<String, Integer> tasks = new HashMap<String, Integer>();
	
	public static Map<String, Integer> antiCamping = new HashMap<String, Integer>();
	public static Map<String, Game> games = new HashMap<String, Game>();
	public static Map<String, String> queues = new HashMap<String, String>();
	
	public static Game getGame(String id) {
		return games.get(id);
	}
	
	public static Game[] getGames() {
		return games.values().toArray(new Game[games.size()]);
	}
	
	public static Game createGame(String id, Location firstCorner, Location secondCorner, boolean generateArena) {
		games.put(id, new Game(firstCorner, secondCorner, id));
		if (HeavySpleef.instance.getConfig().getBoolean("general.generateArena"))
			if (generateArena)
				createGlasArena(getGame(id));
		return getGame(id);
	}
	
	public static void deleteGame(String id) {
		games.remove(id);
	}
	
	public static boolean hasGame(String id) {
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
		if (queues.containsKey(p.getName()))
			p.sendMessage(Game._("leftQueue", queues.get(p.getName())));
		p.sendMessage(Game._("addedToQueue", gameName));
		queues.put(p.getName(), gameName);
	}
	
	public static boolean isInQueue(Player p) {
		return queues.containsKey(p.getName());
	}
	
	public static Game getQueue(Player p) {
		return getGame(queues.get(p.getName()));
	}
	
	public static void removeAllPlayersFromGameQueue(String gameName) {
		for (String player : queues.keySet()) {
			if (queues.get(player).equalsIgnoreCase(gameName))
				queues.remove(player);
		}
	}
	
	private static void createGlasArena(Game game) {
		
		Location loc1 = game.getFirstCorner();
		Location loc2 = game.getSecondCorner();
		
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
		int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		World world = loc1.getWorld();
		Block currentBlock;
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				currentBlock = world.getBlockAt(x, minY, z);
				if (currentBlock.getType() == Material.AIR || !isSolid(currentBlock))
					currentBlock.setType(Material.OBSIDIAN);
			}
		}
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				currentBlock = world.getBlockAt(x, maxY, z);
				if (currentBlock.getType() == Material.AIR || !isSolid(currentBlock))
					currentBlock.setType(Material.GLOWSTONE);
			}
		}
		
		for (int y = minY; y <= maxY; y++) {
			for (int z = minZ; z <= maxZ; z++) {
				currentBlock = world.getBlockAt(minX, y, z);
				if (currentBlock.getType() == Material.AIR || !isSolid(currentBlock))
					currentBlock.setTypeId(20);
			}
		}
		
		for (int y = minY; y <= maxY; y++) {
			for (int z = minZ; z <= maxZ; z++) {
				currentBlock = world.getBlockAt(maxX, y, z);
				if (currentBlock.getType() == Material.AIR || !isSolid(currentBlock))
					currentBlock.setTypeId(20);
			}
		}
		
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				currentBlock = world.getBlockAt(x, y, minZ);
				if (currentBlock.getType() == Material.AIR || !isSolid(currentBlock))
					currentBlock.setTypeId(20);
			}
		}
		
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				currentBlock = world.getBlockAt(x, y, maxZ);
				if (currentBlock.getType() == Material.AIR || !isSolid(currentBlock))
					currentBlock.setTypeId(20);
				
			}
		}
		
		emptyInnerArena(game);
	}
	
	private static void emptyInnerArena(Game game) {
		Location loc1 = game.getFirstInnerCorner();
		Location loc2 = game.getSecondInnerCorner();
		
		World world = loc1.getWorld();
		
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
		int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					world.getBlockAt(x, y, z).setType(Material.AIR);
				}
			}
		}
	}
	
	public static boolean isSolid(Block block) {
		int[] solidIDs = new int[] {1,2,3,4,5,7,12,13,14,15,16,17,18,19,
											29,33,35,41,42,43,44,45,46,477,48,49,
											52,53,54,56,57,58,61,6267,68,69,73,74,
											79,80,82,84,86,87,88,89,91,95,97,98,
											103,108,109,110,112,113,114,116,118,
											120,121,123,124,125,128,129,130,133,
											134,135,136,137,138,152,153,155,158};
		for (int s : solidIDs)
			if (s == block.getTypeId())
				return true;
		return false;
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
