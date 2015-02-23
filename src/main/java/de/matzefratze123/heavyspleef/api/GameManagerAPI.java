/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.api;

import java.util.ArrayList;
import java.util.List;

import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;

public class GameManagerAPI implements IGameManager {

	private static GameManagerAPI	instance;

	static {
		if (instance == null) {
			instance = new GameManagerAPI();
		}
	}

	private GameManagerAPI() {
	}

	public static GameManagerAPI getInstance() {
		return instance;
	}

	@Override
	public void addGame(IGame game) {
		GameManager.addGame((Game) game);
	}

	@Override
	public void deleteGame(String name) {
		GameManager.deleteGame(name);
	}

	@Override
	public boolean hasGame(String name) {
		return GameManager.hasGame(name);
	}

	@Override
	public IGame getGame(String name) {
		return GameManager.getGame(name);
	}

	@Override
	public List<IGame> getGames() {
		List<IGame> games = new ArrayList<IGame>();

		for (Game game : GameManager.getGames()) {
			games.add(game);
		}

		return games;
	}

}
