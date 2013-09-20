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

import java.util.ArrayList;
import java.util.List;


import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.region.HUBPortal;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;

public class GameManager {
	
	//Main core list that contains ALL games!
	public static List<Game> games = new ArrayList<Game>();
	public static List<String> renamedGames = new ArrayList<String>();
	
	public static Location spleefHub = null;
	public static List<HUBPortal> portals = new ArrayList<HUBPortal>();
	
	public static Game getGame(String id) {
		id = id.toLowerCase();
		for (Game game : getGames()) {
			if (id.equalsIgnoreCase(game.getName()))
				return game;
		}
		
		return null;
	}
	
	public static Game[] getGames() {
		return games.toArray(new Game[games.size()]);
	}
	
	public static String[] getGamesAsString() {
		String[] array = new String[getGames().length];
		
		for (int i = 0; i < games.size(); i++) {
			array[i] = games.get(i).getName();
		}
		
		return array;
	}
	
	public static Game createCuboidGame(String id, Location firstCorner, Location secondCorner) {
		games.add(new GameCuboid(firstCorner, secondCorner, id));
		return getGame(id);
	}
	
	public static Game createCylinderGame(String id, Location center, int radiusEastWest, int radiusNorthSouth, int minY, int maxY) {
		if (!HeavySpleef.hooks.getService(WorldEditHook.class).hasHook())
			return null;
		games.add(new GameCylinder(id, center, radiusEastWest, radiusNorthSouth, minY, maxY));
		return getGame(id);
	}
	
	public static Game createCylinderGame(String id, Location center, int radius, int minY, int maxY) {
		return createCylinderGame(id, center, radius, radius, minY, maxY);
	}
	
	public static void deleteGame(String id) {
		id = id.toLowerCase();
		games.remove(getGame(id));
		HeavySpleef.instance.database.db.set(id, null);
		HeavySpleef.instance.database.saveConfig();
	}
	
	public static boolean hasGame(String id) {
		id = id.toLowerCase();
		boolean has = false;
		
		for (Game game : getGames()) {
			if (game.getName().equalsIgnoreCase(id))
				has = true;
		}
		
		return has;
	}
	
	public static boolean isActive(Player p) {
		for (Game game : getGames()) {
			Player[] players = game.getPlayers();
			for (Player pl : players) {
				if (pl.getName().equalsIgnoreCase(p.getName()))
					return true;
			}
		}
		return false;
	}
	
	public static Game fromPlayer(Player p) {
		for (Game game : getGames()) {
			Player[] players = game.getPlayers();
			for (Player pl : players) {
				if (pl.getName().equalsIgnoreCase(p.getName()))
					return game;
			}
		}
		return null;
	}

	public static boolean isActiveIngame(Player p) {
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
	
	/* Spleef HUB Start */
	public static void setSpleefHub(Location location) {
		spleefHub = location;
		if (location == null)
			HeavySpleef.instance.database.globalDb.set("hub", null);
		
	}
	
	public static Location getSpleefHub() {
		return spleefHub;
	}
	
	public static void addPortal(HUBPortal portal) {
		if (portal.isInterruptedId()) {
			int newlyId = getValidId();
			portal.setId(newlyId);
		}
		
		portals.add(portal);
	}
	
	public static void removePortal(HUBPortal portal) {
		portals.remove(portal);
	}
	
	public static HUBPortal getPortal(int id) {
		for (HUBPortal portal : portals) {
			if (portal.getId() == id)
				return portal;
		}
		
		return null;
	}
	
	public static List<HUBPortal> getPortals() {
		return GameManager.portals;
	}
	
	public static boolean hasPortal(int id) {
		for (HUBPortal portal : portals) {
			if (portal.getId() == id)
				return true;
		}
		
		return false;
	}
	
	private static int getValidId() {
		int id = 0;
		
		while(hasPortal(id))
			id++;
		
		return id;
	}
	
	/* Spleef HUB End */
	
}
