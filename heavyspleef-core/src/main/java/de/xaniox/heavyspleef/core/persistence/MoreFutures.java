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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

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