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
package de.matzefratze123.heavyspleef.core.game;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class GameManager {

	private final HeavySpleef heavySpleef;
	private Map<String, Game> games;
	
	public GameManager(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
		this.games = Maps.newHashMap();
	}
	
	public void addGame(Game game) {
		addGame(game, true);
	}
	
	public void addGame(Game game, boolean save) {
		String name = game.getName();
		
		synchronized (games) {
			Validate.isTrue(!games.containsKey(name));
			
			games.put(game.getName(), game);
		}
		
		if (save) {
			heavySpleef.getDatabaseHandler().saveGame(game, null);
		}
	}
	
	public Game deleteGame(String name) {
		Game game;
		
		synchronized (games) {
			game = games.remove(getRealGameName(name));
		}
		
		heavySpleef.getDatabaseHandler().deleteGame(game, null);
		return game;
	}
	
	public void renameGame(final Game game, final String to, FutureCallback<Void> callback) {
		String oldName;
		
		synchronized (games) {
			Validate.isTrue(!hasGame(to), "A game with the name '" + to + "' already exists");
			
			oldName = game.getName();
			game.setName(to);
			
			games.remove(oldName);
			games.put(to, game);
		}
		
		heavySpleef.getDatabaseHandler().renameGame(game, oldName, to, callback);
	}
	
	public boolean hasGame(String name) {
		synchronized (games) {
			return games.containsKey(getRealGameName(name));
		}
	}
	
	public Game getGame(String name) {
		synchronized (games) {
			return games.get(getRealGameName(name));
		}
	}
	
	private String getRealGameName(String name) {
		for (String gameName : games.keySet()) {
			if (gameName.equalsIgnoreCase(name)) {
				return gameName;
			}
		}
		
		return null;
	}
	
	public Game getGame(SpleefPlayer player) {
		synchronized (games) {
			for (Game game : games.values()) {
				if (game.getPlayers().contains(player)) {
					return game;
				}
			}
		}
		
		return null;
	}
	
	public List<Game> getGames() {
		synchronized (games) {
			return ImmutableList.copyOf(games.values());
		}
	}

	public void shutdown() {
		synchronized (games) {
			for (Game game : games.values()) {
				if (!game.getGameState().isGameActive() && game.getGameState() != GameState.LOBBY) {
					return;
				}
				
				game.stop();
			}
		}
	}
	
}
