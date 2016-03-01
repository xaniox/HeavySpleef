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
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.regions.Region;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;

public class RegionVisualizer {
	
	private static final long DEFAULT_DELAY = 0L;
	private static final long DEFAULT_INTERVAL = 15L;
	
	private final JavaPlugin plugin;
	private final BukkitScheduler scheduler = Bukkit.getScheduler();
	private final Map<SpleefPlayer, BukkitTask> tasks = Maps.newHashMap();
	
	private long interval;
	
	public RegionVisualizer(JavaPlugin plugin) {
		this.plugin = plugin;
		this.interval = DEFAULT_INTERVAL;
	}
	
	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	public void visualize(Region region, SpleefPlayer player, World world) {
		if (tasks.containsKey(player)) {
			// There is already another visualization task running for this player
			// Cancel that task
			BukkitTask task = tasks.get(player);
			task.cancel();
		}
		
		VisualizationAnimation animationRunnable = new VisualizationAnimation(region, player, world);
		BukkitTask task = scheduler.runTaskTimer(plugin, animationRunnable, DEFAULT_DELAY, interval);
		
		tasks.put(player, task);
	}
	
	private class VisualizationAnimation implements Runnable {
		
		private static final byte LIME_WOOL_DATA = 5;
		private static final byte RED_WOOL_DATA = 14;
		private static final int REPETITIONS = 10;
		
		private int currentRepetitions;
		private SpleefPlayer player;
		private Region region;
		private World world;
		
		public VisualizationAnimation(Region region, SpleefPlayer player, World world) {
			this.player = player;
			this.region = region;
			this.world = world;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			boolean finish = !player.isOnline();
			
			if (player.isOnline()) {
				Player bukkitPlayer = player.getBukkitPlayer();
				
				// Stores the current wool data
				byte data;
				
				if (currentRepetitions % 2 == 0) {
					data = LIME_WOOL_DATA;
				} else {
					data = RED_WOOL_DATA;
				}
				
				finish = currentRepetitions > REPETITIONS;
				Iterator<BlockVector> iterator = region.iterator();
				
				while (iterator.hasNext()) {
					BlockVector vec = iterator.next();
					
					int x = vec.getBlockX();
					int y = vec.getBlockY();
					int z = vec.getBlockZ();
					
					Location location = new Location(world, x, y, z);
					
					if (!finish) {
						bukkitPlayer.sendBlockChange(location, Material.WOOL, data);
					} else {
						Block block = world.getBlockAt(location);
						
						Material material = block.getType();
						data = block.getData();
						
						bukkitPlayer.sendBlockChange(location, material, data);
					}
				}
			}
			
			if (finish) {
				BukkitTask task = tasks.get(player);
				task.cancel();
				
				tasks.remove(player);
			}
			
			++currentRepetitions;
		}
	}
}