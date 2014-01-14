/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.task.Task;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

/**
 * Provides an manager for pvp timers
 * 
 * @author matzefratze123
 */
public class PvPTimerManager implements Listener {
	
	private Plugin plugin;
	private Map<SpleefPlayer, Integer> pvpTimerTasks;
	private MotionCheckTask motionCheck;
	
	private static PvPTimerManager instance;
	
	/**
	 * Gets the global instance of the timer manager
	 */
	public static PvPTimerManager getInstance() {
		if (instance == null) {
			instance = new PvPTimerManager(HeavySpleef.getInstance());
		}
		
		return instance;
	}
	
	/**
	 * Creates a new PvPTimerManager
	 * 
	 * @param plugin The plugin for event handling
	 */
	public PvPTimerManager(Plugin plugin) {
		this.plugin = plugin;
		this.pvpTimerTasks = new HashMap<SpleefPlayer, Integer>();
		this.motionCheck = new MotionCheckTask();
		this.motionCheck.start();
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Adds a player to the timer and runs the Runnable
	 * when the player didn't move, didn't quit and didn't die.
	 * 
	 * @param player The player to add
	 * @param target The runnable to run when the task hasn't been destroyed for the player
	 * @param ticksUntilEnd The amount of ticks until the target will be executed and the player will be removed from the timer
	 */
	public void add(SpleefPlayer player, Runnable target, long ticksUntilEnd) {
		if (pvpTimerTasks.containsKey(player)) {
			cancel(player);
		}
		
		RunnableExecutorInjector injector = new RunnableExecutorInjector(target, player);
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.getInstance(), injector, ticksUntilEnd);
		
		pvpTimerTasks.put(player, taskId);
		motionCheck.setLastLocation(player, player.getBukkitPlayer().getLocation());
	}
	
	/**
	 * Adds a player to the timer and runs the Runnable
	 * when the player didn't move, didn't quit and didn't die.
	 * </br></br>
	 * This method uses the default tick amount from the config until the target is going to be executed 
	 * 
	 * @param player The player to add
	 * @param target The runnable to run when the task hasn't been destroyed for the player
	 */
	public void add(SpleefPlayer player, Runnable target) {
		long pvptimer = HeavySpleef.getSystemConfig().getGeneralSection().getPvPTimer() * 20L;
		
		add(player, target, pvptimer);
	}
	
	/**
	 * Cancels the task for a player
	 * 
	 * @param player The player to remove
	 * @param announce If we should send the player a fail message
	 */
	public void cancel(SpleefPlayer player, boolean announce) {
		if (!pvpTimerTasks.containsKey(player)) {
			return;
		}
		
		Bukkit.getScheduler().cancelTask(pvpTimerTasks.get(player));
		pvpTimerTasks.remove(player);
		
		if (announce) {
			player.sendMessage(I18N._("pvpTimerCancelled"));
		}
	}
	
	/**
	 * Cancels the task for a player.
	 * This calls {@link #cancel(SpleefPlayer, boolean)} with the announce value of true
	 * 
	 * @param player
	 */
	public void cancel(SpleefPlayer player) {
		cancel(player, true);
	}
	
	/**
	 * Determines if the given player is currently on the timer
	 * 
	 * @param player The player to check
	 * @return True if the player is on the timer, false otherwise
	 */
	public boolean contains(SpleefPlayer player) {
		return pvpTimerTasks.containsKey(player);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(e.getEntity());
		
		if (!contains(player))
			return;
		
		cancel(player);
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		handleQuit(e);
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent e) {
		handleQuit(e);
	}
	
	private void handleQuit(PlayerEvent e) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(e.getPlayer());
		
		if (!contains(player))
			return;
		
		cancel(player);
	}
	
	private class MotionCheckTask implements Runnable, Task {
		
		private int pid = -1;
		private Map<SpleefPlayer, Location> lastLocations;
		
		public MotionCheckTask() {
			lastLocations = new HashMap<SpleefPlayer, Location>();
		}
		
		@Override
		public int start() {
			if (pid != -1) {
				throw new IllegalStateException("Task already registered!");
			}
			
			pid = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
			return pid;
		}

		@Override
		public void cancel() {
			if (pid == -1) {
				return;
			}
			
			Bukkit.getScheduler().cancelTask(pid);
			pid = -1;
		}

		@Override
		public boolean isAlive() {
			return pid != -1;
		}

		@Override
		public void run() {
			for (SpleefPlayer player : pvpTimerTasks.keySet()) {
				if (!lastLocations.containsKey(player)) {
					lastLocations.put(player, player.getBukkitPlayer().getLocation());
					continue;
				}
				
				Location last = lastLocations.get(player);
				Location current = player.getBukkitPlayer().getLocation();
				
				if (!locationEquals(last, current)) {
					PvPTimerManager.this.cancel(player);
				} else {
					lastLocations.put(player, current);
				}
			}
		}
		
		public void setLastLocation(SpleefPlayer player, Location last) {
			lastLocations.put(player, last);
		}
		
		private boolean locationEquals(Location loc1, Location loc2) {
			if (loc1.getWorld() != loc2.getWorld()) {
				return false;
			}
			if (loc1.getBlockX() != loc2.getBlockX()) {
				return false;
			}
			if (loc1.getBlockY() != loc2.getBlockY()) {
				return false;
			}
			if (loc1.getBlockZ() != loc2.getBlockZ()) {
				return false;
			}
			
			return true;
		}
		
	}
	
	private class RunnableExecutorInjector implements Runnable {
		
		private Runnable parent;
		private SpleefPlayer player;
		
		public RunnableExecutorInjector(Runnable parent, SpleefPlayer player) {
			this.parent = parent;
			this.player = player;
		}

		@Override
		public void run() {
			cancel(player, false);
			
			parent.run();
		}
		
	}
	
}
