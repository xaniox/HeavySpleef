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
package de.xaniox.heavyspleef.core;

import com.google.common.collect.Maps;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class PlayerPostActionHandler implements Listener {
	
	private final HeavySpleef heavySpleef;
	private Map<SpleefPlayer, PostDataContainer<?>> awaitingPostAction;
	
	public PlayerPostActionHandler(HeavySpleef heavySpleef) {
		Bukkit.getPluginManager().registerEvents(this, heavySpleef.getPlugin());
		
		this.heavySpleef = heavySpleef;
		this.awaitingPostAction = Maps.newHashMap();
	}
	
	public <T extends Event> void addPostAction(SpleefPlayer player, Class<T> event, PostActionCallback<T> callback) {
		addPostAction(player, event, callback, null);
	}
	
	public <T extends Event> void addPostAction(SpleefPlayer player, Class<T> event, PostActionCallback<T> callback, Object cookie) {
		Validate.notNull(player, "player cannot be null");
		Validate.notNull(event, "event class cannot be null");
		Validate.notNull(callback, "callback cannot be null");
		
		PostDataContainer<T> container = new PostDataContainer<T>(event, callback, cookie);
		awaitingPostAction.put(player, container);
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
		
		if (!awaitingPostAction.containsKey(player)) {
			return;
		}
		
		PostDataContainer<?> container = awaitingPostAction.get(player);
		if (container.event != event.getClass()) {
			return;
		}
		
		awaitingPostAction.remove(player);
		
		PostActionCallback<PlayerInteractEvent> callback = (PostActionCallback<PlayerInteractEvent>) container.callback;
		Object cookie = container.cookie;
		
		callback.onPostAction(event, player, cookie);
	}
	
	private class PostDataContainer<T extends Event> {
		
		private Class<T> event;
		private PostActionCallback<T> callback;
		private Object cookie;
		
		public PostDataContainer(Class<T> event, PostActionCallback<T> callback, Object cookie) {
			this.event = event;
			this.callback = callback;
			this.cookie = cookie;
		}
		
	}
	
	public static interface PostActionCallback<T> {
		
		public void onPostAction(T event, SpleefPlayer player, Object cookie);
		
	}
	
}