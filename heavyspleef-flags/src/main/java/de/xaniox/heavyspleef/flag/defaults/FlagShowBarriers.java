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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.iterator.RegionIterator;
import de.xaniox.heavyspleef.core.MinecraftVersion;
import de.xaniox.heavyspleef.core.SimpleBasicTask;
import de.xaniox.heavyspleef.core.event.GameEndEvent;
import de.xaniox.heavyspleef.core.event.GameStartEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.hook.HookManager;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.hook.ProtocolLibHook;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@Flag(name = "showbarriers", requiresVersion = MinecraftVersion.V1_8_ID, depend = HookReference.PROTOCOLLIB)
public class FlagShowBarriers extends IntegerFlag {
	
	@Inject
	private Game game;
	private ShowBarriersTask task;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Shows barrier particles every x seconds");
		description.add("This allows players to see the floor for a short time if you use barriers as a floor material");
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		if (task != null && task.isRunning()) {
			task.cancel();
		} else if (task == null) {
			task = new ShowBarriersTask();
		}
		
		task.start();
	}
	
	@Subscribe
	public void onGameEnd(GameEndEvent event) {
		if (task != null && task.isRunning()) {
			task.cancel();
		}
	}
	
	private class ShowBarriersTask extends SimpleBasicTask {
		
		private static final long SPAWN_TIME_TICKS = 40L;
		
		private final ProtocolManager manager;
		private long currentTick;
		private boolean calculated;
		private List<Vector> spawningBarriers;
		private int currentIndex;
		private int processPerInterval;
		
		public ShowBarriersTask() {
			super(getHeavySpleef().getPlugin(), TaskType.SYNC_REPEATING_TASK, 10L, 10L);
			
			this.spawningBarriers = Lists.newArrayList();
			
			HookManager hookManager = getHeavySpleef().getHookManager();
			ProtocolLibHook hook = (ProtocolLibHook) hookManager.getHook(HookReference.PROTOCOLLIB);
			manager = hook.getProtocolManager();
		}
		
		@Override
		public void cancel() {
			super.cancel();
			
			//Reset everything
			currentTick = 0;
			calculated = false;
			spawningBarriers.clear();
			currentIndex = 0;
			processPerInterval = 0;
		}

		@Override
		public void run() {
			final long valueTicks = getValue() * 20L;
			
			if (currentTick >= valueTicks) {
				if (!calculated) {
					calculateSpawnLocations();
					processPerInterval = (int) ((spawningBarriers.size() * getTaskArgument(1)) / SPAWN_TIME_TICKS);
					calculated = true;
				}
				
				for (int i = currentIndex; i < currentIndex + processPerInterval && i < spawningBarriers.size(); i++) {
					Vector vector = spawningBarriers.get(i);
					
					spawnBarrier(vector);
				}
				
				currentIndex += processPerInterval;
				if (currentIndex >= spawningBarriers.size()) {
					currentTick = 0;
					currentIndex = 0;
					calculated = false;
				}
			}
			
			currentTick += getTaskArgument(1);
		}

		private void calculateSpawnLocations() {
			spawningBarriers.clear();
			
			for (Floor floor : game.getFloors()) {
				Region region = floor.getRegion();
				RegionIterator iterator = new RegionIterator(region);
				
				while (iterator.hasNext()) {
					BlockVector vector = iterator.next();
					Location location = BukkitUtil.toLocation(game.getWorld(), vector);
					Block block = location.getBlock();
					if (block.getType() != Material.BARRIER) {
						continue;
					}
					
					spawningBarriers.add(vector.add(0.5, 0.5, 0.5));
				}
			}
			
			Collections.shuffle(spawningBarriers);
		}
		
		private void spawnBarrier(Vector vector) {
			PacketContainer packet = manager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
			packet.getParticles().write(0, Particle.BARRIER); //Particle itself
			packet.getFloat().write(0, (float)vector.getX()); //x
			packet.getFloat().write(1, (float)vector.getY()); //y
			packet.getFloat().write(2, (float)vector.getZ()); //z
			packet.getIntegers().write(0, 1); //Number of particles
			
			try {
				for (SpleefPlayer player : game.getPlayers()) {
					Player bukkitPlayer = player.getBukkitPlayer();
					manager.sendServerPacket(bukkitPlayer, packet);
				}
			} catch (InvocationTargetException e) {
				getHeavySpleef().getLogger().log(Level.SEVERE, "Exception occured while sending barrier particle packet", e);
			}
		}
		
	}
	
}