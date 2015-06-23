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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game.JoinResult;
import de.matzefratze123.heavyspleef.core.JoinRequester.PvPTimerManager.FailCause;
import de.matzefratze123.heavyspleef.core.JoinRequester.PvPTimerManager.PvPTimerCallback;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.QueueSection;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class JoinRequester {
	
	public static final JoinFutureCallback QUEUE_PLAYER_CALLBACK = new JoinFutureCallback() {

		private final I18N i18n = I18NManager.getGlobal();
		
		@Override
		public void onJoin(SpleefPlayer player, Game game, JoinResult result) {
			DefaultConfig config = game.getHeavySpleef().getConfiguration(ConfigType.DEFAULT_CONFIG);
			QueueSection section = config.getQueueSection();
			
			if (result == JoinResult.TEMPORARY_DENY && section.isUseQueues()) {
				GameManager manager = game.getHeavySpleef().getGameManager();
				
				//Remove the player from all other queues
				for (Game otherGame : manager.getGames()) {
					otherGame.unqueue(player);
				}
				
				//Queue the player
				boolean success = game.queue(player);
				
				if (success) {
					player.sendMessage(i18n.getVarString(Messages.Command.ADDED_TO_QUEUE)
							.setVariable("game", game.getName())
							.toString());
				} else {
					player.sendMessage(i18n.getString(Messages.Command.COULD_NOT_ADD_TO_QUEUE));
				}
			}
		}
		
	};
	
	private final PvPTimerCallback joinCallback = new PvPTimerCallback() {
		
		@Override
		public void onSuccess(SpleefPlayer player) {
			JoinResult result = game.join(player);
			JoinFutureCallback callback = callbacks.get(player);
			
			if (callback != null) {
				callback.onJoin(player, game, result);
			}
		}
		
		@Override
		public void onFail(SpleefPlayer player, FailCause cause) {
			if (cause != FailCause.NEW_REQUEST && cause != FailCause.QUIT) {
				//Send a message
				String key = null;
				
				switch (cause) {
				case MOVE:
					key = Messages.Player.JOIN_CANCELLED_MOVED;
					break;
				case DAMAGE:
					key = Messages.Player.JOIN_CANCELLED_DAMAGE;
					break;
				case DEATH:
					key = Messages.Player.JOIN_CANCELLED_DEATH;
					break;
				default:
					break;
				}
				
				if (key != null) {
					player.sendMessage(i18n.getString(key));
				}
			}
		}
	};
	private final I18N i18n = I18NManager.getGlobal();
	private final PvPTimerManager pvpTimerManager;
	private final Map<SpleefPlayer, JoinFutureCallback> callbacks;
	private @Setter boolean pvpTimerMode;
	private Game game;
	
	protected JoinRequester(Game game, PvPTimerManager pvpTimerManager) {
		this.game = game;
		this.pvpTimerManager = pvpTimerManager;
		this.callbacks = Maps.newHashMap();
	}
	
	public long request(SpleefPlayer player, JoinFutureCallback callback) throws JoinValidationException {
		if (!game.getGameState().isGameEnabled()) {
			throw new JoinValidationException(i18n.getVarString(Messages.Command.GAME_JOIN_IS_DISABLED)
				.setVariable("game", game.getName())
				.toString(), JoinResult.PERMANENT_DENY);
		}
		
		boolean joinOnCountdown = game.getPropertyValue(GameProperty.JOIN_ON_COUNTDOWN);
		if (game.getGameState() == GameState.INGAME || (game.getGameState() == GameState.STARTING && !joinOnCountdown)) {
			throw new JoinValidationException(i18n.getVarString(Messages.Command.GAME_IS_INGAME)
					.setVariable("game", game.getName())
					.toString(), JoinResult.TEMPORARY_DENY);
		}
		
		GameManager manager = game.getHeavySpleef().getGameManager();
		
		if (manager.getGame(player) != null) {
			throw new JoinValidationException(i18n.getString(Messages.Command.ALREADY_PLAYING), JoinResult.PERMANENT_DENY);
		}
		
		callbacks.put(player, callback);
		
		if (pvpTimerMode) {
			pvpTimerManager.startTimer(player, joinCallback);
			return pvpTimerManager.getTicksNeeded() / 20L;
		} else {
			joinCallback.onSuccess(player);
			return 0;
		}
	}
	
	public interface JoinFutureCallback {
		
		public void onJoin(SpleefPlayer player, Game game, JoinResult result);
		
	}
	
	public static class JoinValidationException extends Exception {

		private static final long serialVersionUID = -2124169192955966100L;
		private @Getter JoinResult result;

		public JoinValidationException(String message, JoinResult result) {
			super(message);
			
			this.result = result;
		}
		
	}
	
	public static class PvPTimerManager implements Listener {
		
		private final HeavySpleef heavySpleef;
		private final Map<SpleefPlayer, Holder> checking;
		private final CheckTask checkTask;
		private @Getter @Setter long ticksNeeded;
		
		public PvPTimerManager(HeavySpleef heavySpleef) {
			this.heavySpleef = heavySpleef;
			this.checking = Maps.newHashMap();
			this.checkTask = new CheckTask();
		}
		
		public void startTimer(SpleefPlayer player, PvPTimerCallback callback) {
			Validate.notNull(player, "Player cannot be null");
			Validate.notNull(callback, "Callback cannot be null");
			
			if (checking.containsKey(player)) {
				PvPTimerCallback previousCallback = checking.get(player).callback;
				previousCallback.onFail(player, FailCause.NEW_REQUEST);
			}
			
			if (!checkTask.isRunning()) {
				checkTask.start();
			}
			
			Location location = player.getBukkitPlayer().getLocation();
			
			Holder holder = new Holder();
			holder.callback = callback;
			holder.location = location;
			
			checking.put(player, holder);
		}
		
		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getEntity());
			if (!checking.containsKey(player)) {
				return;
			}
			
			handleFail(player, FailCause.DEATH, null);
		}
		
		@EventHandler
		public void onEntityDamage(EntityDamageEvent event) {
			Entity entity = event.getEntity();
			if (!(entity instanceof Player)) {
				return;
			}
			
			SpleefPlayer player = heavySpleef.getSpleefPlayer(entity);
			if (!checking.containsKey(player)) {
				return;
			}
			
			handleFail(player, FailCause.DAMAGE, null);
		}
		
		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			handleQuit(event);
		}
		
		@EventHandler
		public void onPlayerKick(PlayerKickEvent event) {
			handleQuit(event);
		}
		
		private void handleQuit(PlayerEvent event) {
			SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
			if (!checking.containsKey(player)) {
				return;
			}
			
			handleFail(player, FailCause.QUIT, null);
		}
		
		private void handleFail(SpleefPlayer player, FailCause cause, Iterator<?> iterator) {
			Holder holder = checking.get(player);
			PvPTimerCallback callback = holder.callback;
			callback.onFail(player, cause);
			removePlayer(player, iterator);
		}
		
		private void removePlayer(SpleefPlayer player, Iterator<?> iterator) {
			if (iterator == null) {
				checking.remove(player);
			} else {
				iterator.remove();
			}
			
			if (checking.isEmpty() && checkTask != null) {
				checkTask.cancel();
			}
		}
		
		private class Holder {
			
			private Location location;
			private PvPTimerCallback callback;
			private int currentTicks;
			
		}
		
		private class CheckTask extends SimpleBasicTask {

			public CheckTask() {
				super(heavySpleef.getPlugin(), TaskType.SYNC_REPEATING_TASK, 0L, 20L);
			}

			@Override
			public void run() {
				Iterator<Entry<SpleefPlayer, Holder>> iterator = checking.entrySet().iterator();
				
				while (iterator.hasNext()) {
					Entry<SpleefPlayer, Holder> entry = iterator.next();
					
					SpleefPlayer player = entry.getKey();
					Holder holder = entry.getValue();
					
					Location now = player.getBukkitPlayer().getLocation();
					Location previous = holder.location;
					
					if (now.getBlockX() != previous.getBlockX() || now.getBlockY() != previous.getBlockY() || now.getBlockZ() != previous.getBlockZ()) {
						handleFail(player, FailCause.MOVE, iterator);
					} else {
						holder.currentTicks += getTaskArgument(1);
						
						if (holder.currentTicks >= ticksNeeded) {
							PvPTimerCallback callback = holder.callback;
							callback.onSuccess(player);
							removePlayer(player, iterator);
						}
					}
				}
			}
			
			
		}
		
		public interface PvPTimerCallback {
			
			public void onSuccess(SpleefPlayer player);
			
			public void onFail(SpleefPlayer player, FailCause cause);
			
		}
		
		public enum FailCause {
			
			MOVE,
			DAMAGE,
			DEATH,
			QUIT,
			NEW_REQUEST;
			
		}
		
	}

}