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
package de.matzefratze123.heavyspleef.core.persistence;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Statistic;

public interface AsyncReadWriteHandler {
	
	public void saveGames(Iterable<Game> iterable, FutureCallback<Void> callback);
	
	public void saveGame(Game game, FutureCallback<Void> callback);
	
	public void getGame(String name, FutureCallback<Game> callback);
	
	public void getGames(FutureCallback<List<Game>> callback);
	
	public void deleteGame(Game game, FutureCallback<Void> callback);
	
	public void saveStatistics(Iterable<Statistic> statistics, FutureCallback<Void> callback);
	
	public void saveStatistic(Statistic statistic, FutureCallback<Void> callback);
	
	public void getStatistic(String player, FutureCallback<Statistic> callback);
	
	public void getStatistic(UUID uuid, FutureCallback<Statistic> callback);
	
	public void getTopStatistics(int limit, FutureCallback<TreeSet<Statistic>> callback);
	
}
