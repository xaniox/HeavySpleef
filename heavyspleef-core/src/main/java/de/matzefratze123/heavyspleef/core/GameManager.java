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
package de.matzefratze123.heavyspleef.core;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class GameManager {

	private AsyncReadWriteHandler databaseHandler;
	private Map<String, Game> games;
	
	public GameManager(AsyncReadWriteHandler databaseHandler) {
		this.databaseHandler = databaseHandler;
		this.games = Maps.newHashMap();
	}
	
	public void addGame(Game game) {
		addGame(game, true);
	}
	
	public void addGame(Game game, boolean save) {
		String name = game.getName();
		Validate.isTrue(!games.containsKey(name));
		
		games.put(game.getName(), game);
		
		if (save) {
			databaseHandler.saveGame(game, null);
		}
	}
	
	public Game deleteGame(String name) {
		Game game = games.remove(name);
		
		databaseHandler.deleteGame(game, null);
		return game;
	}
	
	public boolean hasGame(String name) {
		return games.containsKey(name);
	}
	
	public Game getGame(String name) {
		return games.get(name);
	}
	
	public Game getGame(SpleefPlayer player) {
		for (Game game : games.values()) {
			if (game.getPlayers().contains(player)) {
				return game;
			}
		}
		
		return null;
	}
	
	public Collection<Game> getGames() {
		return games.values();
	}
	
}
