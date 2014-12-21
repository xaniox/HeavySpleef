package de.matzefratze123.heavyspleef.persistence;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Statistic;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;

public class ForwardingAsyncReadWriteHandler implements AsyncReadWriteHandler {

	private ReadWriteHandler delegate;
	private ListeningExecutorService executorService;
	private Plugin plugin;
	private boolean forceAsync;
	
	public ForwardingAsyncReadWriteHandler(ReadWriteHandler delegate, Plugin plugin, boolean forceAsync) {
		this.delegate = delegate;
		this.forceAsync = forceAsync;
		this.plugin = plugin;
		
		ExecutorService plainService = Executors.newCachedThreadPool();
		this.executorService = MoreExecutors.listeningDecorator(plainService);
	}
	
	@Override
	public void saveGames(final Iterable<Game> iterable, FutureCallback<Void> callback) {
		runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveGames(iterable);
			}
		}, callback);
	}

	@Override
	public void saveGame(final Game game, FutureCallback<Void> callback) {
		runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveGame(game);
			}
		}, callback);
	}

	@Override
	public void getGame(final String name, FutureCallback<Game> callback) {
		runCallableThreadDynamic(new Callable<Game>() {

			@Override
			public Game call() throws Exception {
				return delegate.getGame(name);
			}
		}, callback);
	}

	@Override
	public void getGames(FutureCallback<List<Game>> callback) {
		runCallableThreadDynamic(new Callable<List<Game>>() {

			@Override
			public List<Game> call() throws Exception {
				return delegate.getGames();
			}
		}, callback);
	}

	@Override
	public void deleteGame(final Game game, FutureCallback<Void> callback) {
		runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.deleteGame(game);
			}
		}, callback);
	}

	@Override
	public void saveStatistics(final Iterable<Statistic> statistics, FutureCallback<Void> callback) {
		runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveStatistics(statistics);
			}
		}, callback);
	}

	@Override
	public void saveStatistic(final Statistic statistic, FutureCallback<Void> callback) {
		runCallableThreadDynamic(new VoidCallable() {
			
			@Override
			public void voidCall() throws Exception {
				delegate.saveStatistic(statistic);
			}
		}, callback);
	}

	@Override
	public void getStatistic(final UUID uuid, FutureCallback<Statistic> callback) {
		runCallableThreadDynamic(new Callable<Statistic>() {

			@Override
			public Statistic call() throws Exception {
				return delegate.getStatistic(uuid);
			}
		}, callback);
	}

	@Override
	public void getTopStatistics(final int limit, FutureCallback<TreeSet<Statistic>> callback) {
		runCallableThreadDynamic(new Callable<TreeSet<Statistic>>() {

			@Override
			public TreeSet<Statistic> call() throws Exception {
				return delegate.getTopStatistics(limit);
			}
		}, callback);
	}
	
	public <R> void runCallableThreadDynamic(Callable<R> callable, FutureCallback<R> callback) {
		if (isServerThread() || forceAsync) {
			ListenableFuture<R> future = executorService.submit(callable);
			
			if (callback != null) {
				MoreFutures.addBukkitSyncCallback(plugin, future, callback);
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
	}
	
	private static boolean isServerThread() {
		return Bukkit.isPrimaryThread();
	}
	
	public abstract class VoidCallable implements Callable<Void> {
		
		@Override
		public Void call() throws Exception {
			voidCall();
			return (Void) null;
		}
		
		public abstract void voidCall() throws Exception;
		
	}
	
}
