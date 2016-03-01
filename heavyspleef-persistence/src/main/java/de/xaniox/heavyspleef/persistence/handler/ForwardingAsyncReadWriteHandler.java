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
package de.xaniox.heavyspleef.persistence.handler;

import com.google.common.util.concurrent.*;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.xaniox.heavyspleef.core.persistence.MoreFutures;
import de.xaniox.heavyspleef.core.persistence.OperationBatch;
import de.xaniox.heavyspleef.core.persistence.ReadWriteHandler;
import de.xaniox.heavyspleef.core.stats.Statistic;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;

public class ForwardingAsyncReadWriteHandler implements AsyncReadWriteHandler {
	
	private ReadWriteHandler delegate;
	private ListeningExecutorService executorService;
	private Plugin plugin;
	private boolean forceAsync;
	
	public ForwardingAsyncReadWriteHandler(ReadWriteHandler delegate, final Plugin plugin, boolean forceAsync) {
		this.delegate = delegate;
		this.forceAsync = forceAsync;
		this.plugin = plugin;
		
		final UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				plugin.getLogger().log(Level.SEVERE, "Uncaught exception in database thread:", e);
			}
		};
		
		final ThreadFactory threadFactory = new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setUncaughtExceptionHandler(exceptionHandler);
				thread.setName("Persistence-Thread");
				
				return thread;
			}
		}; 
		
		ExecutorService plainService = Executors.newFixedThreadPool(1, threadFactory);
		this.executorService = MoreExecutors.listeningDecorator(plainService);
	}
	
	@Override
	public ListenableFuture<?> saveGames(final Iterable<Game> iterable, FutureCallback<Void> callback) {
		return runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveGames(iterable);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<?> saveGame(final Game game, FutureCallback<Void> callback) {
		return runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveGame(game);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<Game> getGame(final String name, FutureCallback<Game> callback) {
		return runCallableThreadDynamic(new Callable<Game>() {

			@Override
			public Game call() throws Exception {
				return delegate.getGame(name);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<List<Game>> getGames(FutureCallback<List<Game>> callback) {
		return runCallableThreadDynamic(new Callable<List<Game>>() {

			@Override
			public List<Game> call() throws Exception {
				return delegate.getGames();
			}
		}, callback);
	}
	
	@Override
	public ListenableFuture<?> renameGame(final Game game, final String from, final String to, FutureCallback<Void> callback) {
		return runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.renameGame(game, from, to);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<?> deleteGame(final Game game, FutureCallback<Void> callback) {
		return runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.deleteGame(game);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<?> saveStatistics(final Iterable<Statistic> statistics, FutureCallback<Void> callback) {
		return runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveStatistics(statistics);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<?> saveStatistic(final Statistic statistic, FutureCallback<Void> callback) {
		return runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveStatistic(statistic);
			}
		}, callback);
	}
	
	@Override
	public ListenableFuture<Statistic> getStatistic(final String player, FutureCallback<Statistic> callback) {
		return runCallableThreadDynamic(new Callable<Statistic>() {

			@Override
			public Statistic call() throws Exception {
				return delegate.getStatistic(player);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<Statistic> getStatistic(final UUID uuid, FutureCallback<Statistic> callback) {
		return runCallableThreadDynamic(new Callable<Statistic>() {

			@Override
			public Statistic call() throws Exception {
				return delegate.getStatistic(uuid);
			}
		}, callback);
	}
	
	@Override
	public ListenableFuture<Map<String, Statistic>> getStatistics(final String[] players, FutureCallback<Map<String, Statistic>> callback) {
		return runCallableThreadDynamic(new Callable<Map<String, Statistic>>() {

			@Override
			public Map<String, Statistic> call() throws Exception {
				return delegate.getStatistics(players);
			}
		}, callback);
	}
	
	@Override
	public ListenableFuture<Integer> getStatisticRank(final String player, FutureCallback<Integer> callback) {
		return runCallableThreadDynamic(new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return delegate.getStatisticRank(player);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<Integer> getStatisticRank(final UUID uuid, FutureCallback<Integer> callback) {
		return runCallableThreadDynamic(new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return delegate.getStatisticRank(uuid);
			}
		}, callback);
	}

	@Override
	public ListenableFuture<Map<String, Statistic>> getTopStatistics(final int offset, final int limit, FutureCallback<Map<String, Statistic>> callback) {
		return runCallableThreadDynamic(new Callable<Map<String, Statistic>>() {

			@Override
			public Map<String, Statistic> call() throws Exception {
				return delegate.getTopStatistics(offset, limit);
			}
		}, callback);
	}
	
	@Override
	public ListenableFuture<OperationBatch.BatchResult> executeBatch(OperationBatch batch, FutureCallback<OperationBatch.BatchResult> callback) {
		batch.setHandler(delegate);
		return runCallableThreadDynamic(batch, callback);
	}
	
	@Override
	public void clearCache() {
		delegate.clearCache();
	}
	
	@Override
	public ListenableFuture<?> forceCacheSave(FutureCallback<Void> callback) {
		return runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.forceCacheSave();
			}
		}, callback);
	}
	
	@Override
	public void release() {
		delegate.release();
	}
	
	@Override
	public void shutdownGracefully() {
		final long timeout = 5000;
		
		//Request the service to shutdown
		executorService.shutdown();
		
		try {
			//Wait for running tasks to complete
			executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("InterruptedException thrown while waiting for tasks to be completed: ", e);
		}
		
		delegate.shutdownGracefully();
	}
	
	public <R> ListenableFuture<R> runCallableThreadDynamic(Callable<R> callable, FutureCallback<R> callback) {
		ListenableFuture<R> future = null;
		
		if (callback == null) {
			callback = new FutureCallback<R>() {

				@Override
				public void onSuccess(R result) {}

				@Override
				public void onFailure(Throwable t) {
					plugin.getLogger().log(Level.SEVERE, "Unexpected exception in database thread:", t);
				}
			};
		}
		
		if (isServerThread() || forceAsync) {
			future = executorService.submit(callable);
			
			if (plugin.isEnabled()) {
				//MoreFutures invokes the Bukkit Scheduler and since tasks cannot be
				//registered while the plugin is disabled we can only add a synchronous
				//callback when the plugin is enabled
				MoreFutures.addBukkitSyncCallback(plugin, future, callback);
			} else {
				//Just add a default callback as the plugin is disabled
				Futures.addCallback(future, callback);
			}
		} else {
			Throwable throwableThrown = null;
			R result = null;
			
			try {
				result = callable.call();
			} catch (Exception e) {
				throwableThrown = e;
			}
			
			if (callback != null) {
				if (throwableThrown != null) {
					callback.onFailure(throwableThrown);
				} else {
					callback.onSuccess(result);
				}
			}
		}
		
		return future;
	}
	
	private static boolean isServerThread() {
		return Bukkit.isPrimaryThread();
	}
	
	public abstract class VoidCallable implements Callable<Void> {
		
		@Override
		public Void call() throws Exception {
			voidCall();
			return null;
		}
		
		public abstract void voidCall() throws Exception;
		
	}
	
}