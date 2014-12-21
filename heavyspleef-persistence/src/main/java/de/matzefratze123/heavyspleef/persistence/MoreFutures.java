package de.matzefratze123.heavyspleef.persistence;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class MoreFutures {
	
	/**
	 * Add a callback to a {@link ListenableFuture}
	 * to be run on the bukkit main thread
	 * 
	 * @param plugin The plugin registering the callback
	 * @param future The {@link ListenableFuture} to add this callback
	 * @param callback The callback to be called
	 */
	public static <T> void addBukkitSyncCallback(final Plugin plugin, ListenableFuture<T> future, final FutureCallback<T> callback) {
		Futures.addCallback(future, new FutureCallback<T>() {
			@Override
			public void onFailure(final Throwable cause) {
				Bukkit.getScheduler().runTask(plugin, new Runnable() {
					
					@Override
					public void run() {
						callback.onFailure(cause);
					}
				});
			}
			@Override
			public void onSuccess(final T result) {
				Bukkit.getScheduler().runTask(plugin, new Runnable() {
					
					@Override
					public void run() {
						callback.onSuccess(result);
					}
				});
			}
		});
	}
	
	/**
	 * Add a unsafe callback to a {@link ListenableFuture}
	 * to be run on the bukkit main thread
	 * This method does not check the generic types
	 * of the future and the callback.<br><br>
	 * This may throw a {@link ClassCastException} if
	 * those types are not equal
	 * 
	 * @param plugin
	 * @param future
	 * @param callback
	 */
	@SuppressWarnings("unchecked")
	public static <T> void addUnsafeBukkitSyncCallback(final Plugin plugin, ListenableFuture<?> future, final FutureCallback<?> callback) {
		addBukkitSyncCallback(plugin, (ListenableFuture<T>) future, (FutureCallback<T>) callback);
	}
	
}