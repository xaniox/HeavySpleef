package de.matzefratze123.heavyspleef.persistence;

import java.util.List;
import java.util.TreeSet;
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
	public void saveGames(Iterable<Game> iterable, FutureCallback<Void> callback) {
		invokeVoidDelegate(callback, () -> delegate.saveGames(iterable));
	}
	
	@Override
	public void saveGame(Game game, FutureCallback<Void> callback) {
		invokeVoidDelegate(callback, () -> delegate.saveGame(game));
	}
	
	@Override
	public void getGame(String name, FutureCallback<Game> callback) {
		invokeDelegate(callback, () -> delegate.getGame(name));
	}
	
	@Override
	public void getGames(FutureCallback<List<Game>> callback) {
		invokeDelegate(callback, delegate::getGames);
	}
	
	@Override
	public void deleteGame(Game game, FutureCallback<Void> callback) {
		invokeVoidDelegate(callback, () -> delegate.deleteGame(game));
	}
	
	@Override
	public void saveStatistics(Iterable<Statistic> statistics, FutureCallback<Void> callback) {
		invokeVoidDelegate(callback, () -> delegate.saveStatistics(statistics));
	}
	
	@Override
	public void saveStatistic(Statistic statistic, FutureCallback<Void> callback) {
		invokeVoidDelegate(callback, () -> delegate.saveStatistic(statistic));
	}
	
	@Override
	public void getStatistic(String player, FutureCallback<Statistic> callback) {
		invokeDelegate(callback, () -> delegate.getStatistic(player));
	}
	
	@Override
	public void getTopStatistics(int limit, FutureCallback<TreeSet<Statistic>> callback) {
		invokeDelegate(callback, () -> delegate.getTopStatistics(limit));
	}
	
	private <T> void invokeDelegate(FutureCallback<T> callback, Callable<T> callable) {
		if (isServerThread() || forceAsync) {
			ListenableFuture<T> future = executorService.submit(callable);
			
			if (callback != null) {
				MoreFutures.addBukkitSyncCallback(plugin, future, callback);
			}
		} else {
			//This seems not to be the server thread so just execute it
			Exception cause = null;
			T result = null;
			
			try {
				result = callable.call();
			} catch (Exception e) {
				cause = e;
			}
			
			if (callback != null) {
				if (cause != null) {
					callback.onFailure(cause);
				} else {
					callback.onSuccess(result);
				}
			}
		}
	}
	
	private void invokeVoidDelegate(FutureCallback<Void> callback, VoidCallable callable) {
		invokeDelegate(callback, callable);
	}
	
	private static boolean isServerThread() {
		return Bukkit.isPrimaryThread();
	}
	
	@FunctionalInterface
	public interface VoidCallable extends Callable<Void> {
		
		@Override
		public default Void call() throws Exception {
			voidCall();
			return (Void) null;
		}
		
		public void voidCall();
		
	}
	
}
