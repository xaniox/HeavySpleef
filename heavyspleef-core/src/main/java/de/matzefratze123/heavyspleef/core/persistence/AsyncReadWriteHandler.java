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
import java.util.UUID;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Statistic;

public interface AsyncReadWriteHandler {
	
	public ListenableFuture<?> saveGames(Iterable<Game> iterable, FutureCallback<Void> callback);
	
	public ListenableFuture<?> saveGame(Game game, FutureCallback<Void> callback);
	
	public ListenableFuture<Game> getGame(String name, FutureCallback<Game> callback);
	
	public ListenableFuture<List<Game>> getGames(FutureCallback<List<Game>> callback);
	
	public ListenableFuture<?> deleteGame(Game game, FutureCallback<Void> callback);
	
	public ListenableFuture<?> saveStatistics(Iterable<Statistic> statistics, FutureCallback<Void> callback);
	
	public ListenableFuture<?> saveStatistic(Statistic statistic, FutureCallback<Void> callback);
	
	public ListenableFuture<Statistic> getStatistic(String player, FutureCallback<Statistic> callback);
	
	public ListenableFuture<Statistic> getStatistic(UUID uuid, FutureCallback<Statistic> callback);
	
	public ListenableFuture<List<Statistic>> getTopStatistics(int limit, FutureCallback<List<Statistic>> callback);
	
}
