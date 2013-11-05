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

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.api.IGame;
import de.matzefratze123.heavyspleef.core.region.HUBPortal;

public class GameManager {
	
	//Main list which contains all games
	public static List<Game> games = new ArrayList<Game>();
	
	public static Location spleefHub = null;
	public static List<HUBPortal> portals = new ArrayList<HUBPortal>();
	
	public static void addGame(Game game) {
		if (hasGame(game.getName())) {
			throw new IllegalStateException("Game " + game.getName() + " already exist!");
		}
		
		games.add(game);
	}

	public static void deleteGame(String name) {
		IGame game = getGame(name);
		
		if (game == null) {
			return;
		}
		
		games.remove(game);
		HeavySpleef.getInstance().getGameDatabase().db.set(name, null);
	}

	public static boolean hasGame(String name) {
		for (Game game : games) {
			if (game.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		return false;
	}

	public static Game getGame(String name) {
		for (Game game : games) {
			if (game.getName().equalsIgnoreCase(name)) {
				return game;
			}
		}
		
		return null;
	}
	
	public static List<Game> getGames() {
		return games;
	}
	
	/* Spleef HUB Start */
	public static void setSpleefHub(Location location) {
		spleefHub = location;
		if (location == null)
			HeavySpleef.getInstance().getGameDatabase().globalDb.set("hub", null);
		
	}
	
	public static Location getSpleefHub() {
		return spleefHub;
	}
	
	public static void addPortal(HUBPortal portal) {
		if (portal.isIllegalId()) {
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
