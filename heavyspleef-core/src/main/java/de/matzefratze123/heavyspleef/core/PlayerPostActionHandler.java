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
package de.matzefratze123.heavyspleef.core;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerPostActionHandler implements Listener {
	
	private final HeavySpleef heavySpleef;
	private Map<SpleefPlayer, PostDataContainer> awaitingPostAction;
	
	public PlayerPostActionHandler(HeavySpleef heavySpleef) {
		Bukkit.getPluginManager().registerEvents(this, heavySpleef.getPlugin());
		
		this.heavySpleef = heavySpleef;
		this.awaitingPostAction = Maps.newHashMap();
	}
	
	public void addPostAction(SpleefPlayer player, PostActionType type, PostActionCallback callback) {
		addPostAction(player, type, callback, null);
	}
	
	public void addPostAction(SpleefPlayer player, PostActionType type, PostActionCallback callback, Object cookie) {
		Validate.notNull(player, "player cannot be null");
		Validate.notNull(type, "type cannot be null");
		Validate.notNull(callback, "callback cannot be null");
		
		PostDataContainer container = new PostDataContainer(type, callback, cookie);
		awaitingPostAction.put(player, container);
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
		
		if (!awaitingPostAction.containsKey(player)) {
			return;
		}
		
		PostDataContainer container = awaitingPostAction.get(player);
		if (container.type != PostActionType.PLAYER_INTERACT) {
			return;
		}
		
		awaitingPostAction.remove(player);
		
		PostActionCallback callback = container.callback;
		Object cookie = container.cookie;
		
		callback.onPostAction(player, cookie);
	}
	
	private class PostDataContainer {
		
		private PostActionType type;
		private PostActionCallback callback;
		private Object cookie;
		
		public PostDataContainer(PostActionType type, PostActionCallback callback, Object cookie) {
			this.type = type;
			this.callback = callback;
			this.cookie = cookie;
		}
		
	}
	
	public enum PostActionType {
		
		PLAYER_INTERACT;
		
	}
	
	public static interface PostActionCallback {
		
		public void onPostAction(SpleefPlayer player, Object cookie);
		
	}
	
}
