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
package de.xaniox.heavyspleef.flag.defaults;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.SimpleBasicTask;
import de.xaniox.heavyspleef.core.event.GameStateChangeEvent;
import de.xaniox.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.FlagInit;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Flag(name = "freeze")
public class FlagFreeze extends BaseFlag {
	
	private static final int POTION_AMPLIFIER = 128;
	private static final float DEFAULT_WALK_SPEED = 0.2f;
	private static MovementCheckTask task;
	
	@FlagInit
	public static void initMovementCheckTask(HeavySpleef heavySpleef) {
		//Instantiate the task class but do not start the task
		task = new MovementCheckTask(heavySpleef);
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Freezes players on the game's countdown");
	}
	
	@Subscribe
	public void onGameStateChange(GameStateChangeEvent event) {
		GameState oldState = event.getOldState();
		GameState newState = event.getNewState();
		Game game = event.getGame();
		
		if (newState == GameState.STARTING) {
			//Freeze all players while the countdown is active
			if (!task.isRunning()) {
				task.start();
			}
			
			for (SpleefPlayer player : game.getPlayers()) {
				freezePlayer(player);
			}
		} else if (oldState == GameState.STARTING) {
			//Unfreeze all players
			for (SpleefPlayer player : game.getPlayers()) {
				unfreezePlayer(player);
			}
			
			checkTaskNeed();
		}
	}
	
	@Subscribe
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		unfreezePlayer(event.getPlayer());
		
		checkTaskNeed();
	}
	
	private void checkTaskNeed() {
		if (!task.hasPlayersLeft() && task.isRunning()) {
			task.cancel();
		}
	}
	
	private void freezePlayer(SpleefPlayer player) {
		task.addFrozenPlayer(player);
		
		Player bukkitPlayer = player.getBukkitPlayer();
		bukkitPlayer.setWalkSpeed(0F);
		
		PotionEffect noJumpEffect = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, POTION_AMPLIFIER, true);
		bukkitPlayer.addPotionEffect(noJumpEffect, true);
	}
	
	private void unfreezePlayer(SpleefPlayer player) {
		task.removeFrozenPlayer(player);
		
		Player bukkitPlayer = player.getBukkitPlayer();
		bukkitPlayer.setWalkSpeed(DEFAULT_WALK_SPEED);
		bukkitPlayer.removePotionEffect(PotionEffectType.JUMP);
	}
	
	private static class MovementCheckTask extends SimpleBasicTask {

		private Map<SpleefPlayer, Location> freezeLocations;
		private List<WeakReference<SpleefPlayer>> frozenPlayers;
		
		public MovementCheckTask(HeavySpleef heavySpleef) {
			super(heavySpleef.getPlugin(), TaskType.SYNC_REPEATING_TASK, 0L, 8L);
			
			this.freezeLocations = Maps.newHashMap();
			this.frozenPlayers = Lists.newArrayList();
		}
		
		public void addFrozenPlayer(SpleefPlayer player) {
			WeakReference<SpleefPlayer> ref = new WeakReference<SpleefPlayer>(player);
			frozenPlayers.add(ref);
			freezeLocations.put(player, player.getBukkitPlayer().getLocation());
		}
		
		public void removeFrozenPlayer(SpleefPlayer player) {
			Iterator<WeakReference<SpleefPlayer>> iterator = frozenPlayers.iterator();
			while (iterator.hasNext()) {
				WeakReference<SpleefPlayer> ref = iterator.next();
				if (ref.get() != null && ref.get() == player) {
					iterator.remove();
				}
			}
			
			freezeLocations.remove(player);
		}
		
		public boolean hasPlayersLeft() {
			return !frozenPlayers.isEmpty();
		}

		@Override
		public void run() {
			Iterator<WeakReference<SpleefPlayer>> iterator = frozenPlayers.iterator();
			while (iterator.hasNext()) {
				WeakReference<SpleefPlayer> ref = iterator.next();
				SpleefPlayer player = ref.get();
				
				if (player == null) {
					iterator.remove();
				}
				
				Location now = player.getBukkitPlayer().getLocation();
				Location freezeLoc = freezeLocations.get(player);
				
				if (now.getX() != freezeLoc.getX() || now.getY() != freezeLoc.getY() || now.getZ() != freezeLoc.getZ()) {
					Location tpLocation = freezeLoc.clone();
					tpLocation.setYaw(now.getYaw());
					tpLocation.setPitch(now.getPitch());
					
					player.teleport(tpLocation);
				}
			}
		}
		
	}

}