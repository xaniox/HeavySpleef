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
package de.matzefratze123.heavyspleef.persistence;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Statistic;

public interface ReadWriteHandler {
	
	public void saveGames(Iterable<Game> iterable);
	
	public void saveGame(Game game);
	
	public Game getGame(String name);
	
	public List<Game> getGames();
	
	public void deleteGame(Game game);
	
	public void saveStatistics(Iterable<Statistic> iterable);
	
	public void saveStatistic(Statistic statistic);
	
	public Statistic getStatistic(UUID uuid);
	
	public Statistic getStatistic(String playerName);

	public TreeSet<Statistic> getTopStatistics(int limit);
	
}
