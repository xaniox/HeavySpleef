/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core.persistence;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.stats.Statistic;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AsyncReadWriteHandler {
	
	public ListenableFuture<?> saveGames(Iterable<Game> iterable, FutureCallback<Void> callback);
	
	public ListenableFuture<?> saveGame(Game game, FutureCallback<Void> callback);
	
	public ListenableFuture<Game> getGame(String name, FutureCallback<Game> callback);
	
	public ListenableFuture<List<Game>> getGames(FutureCallback<List<Game>> callback);
	
	public ListenableFuture<?> renameGame(Game game, String from, String to, FutureCallback<Void> callback);
	
	public ListenableFuture<?> deleteGame(Game game, FutureCallback<Void> callback);
	
	public ListenableFuture<?> saveStatistics(Iterable<Statistic> statistics, FutureCallback<Void> callback);
	
	public ListenableFuture<?> saveStatistic(Statistic statistic, FutureCallback<Void> callback);
	
	public ListenableFuture<Statistic> getStatistic(String player, FutureCallback<Statistic> callback);
	
	public ListenableFuture<Statistic> getStatistic(UUID uuid, FutureCallback<Statistic> callback);
	
	public ListenableFuture<Integer> getStatisticRank(String player, FutureCallback<Integer> callback);
	
	public ListenableFuture<Integer> getStatisticRank(UUID uuid, FutureCallback<Integer> callback);
	
	public ListenableFuture<Map<String, Statistic>> getTopStatistics(int offset, int limit, FutureCallback<Map<String, Statistic>> callback);

	public ListenableFuture<Map<String, Statistic>> getStatistics(String[] players, FutureCallback<Map<String, Statistic>> callback);
	
	public ListenableFuture<OperationBatch.BatchResult> executeBatch(OperationBatch batch, FutureCallback<OperationBatch.BatchResult> callback);
	
	public void clearCache();

	public ListenableFuture<?> forceCacheSave(FutureCallback<Void> callback);

	public void release();

	public void shutdownGracefully();
	
}