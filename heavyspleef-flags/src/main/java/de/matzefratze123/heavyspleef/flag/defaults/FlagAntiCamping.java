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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.FlagSection;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagInit;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.game.GameState;
import de.matzefratze123.heavyspleef.core.game.QuitCause;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "anticamping")
public class FlagAntiCamping extends BaseFlag {

	private static final long ONE_SECOND_INTERVAL = 20L;
	private static AntiCampingTask task;
	private static BukkitTask bukkitTask;
	
	@FlagInit
	public static void initTask(HeavySpleef heavySpleef) {
		BukkitScheduler scheduler = Bukkit.getScheduler();
		
		task = new AntiCampingTask(heavySpleef);
		bukkitTask = scheduler.runTaskTimer(heavySpleef.getPlugin(), task, ONE_SECOND_INTERVAL, ONE_SECOND_INTERVAL);
	}
	
	public static void stopTask(HeavySpleef heavySpleef) {
		if (bukkitTask == null) {
			return;
		}
		
		bukkitTask.cancel();
		bukkitTask = null;
		task = null;
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the anticamping feature for a Spleef game");
	}
	
	@Subscribe
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		if (task == null) {
			return;
		}
		
		Game game = event.getGame();
		if (game.getGameState() != GameState.INGAME) {
			return;
		}
		
		SpleefPlayer player = event.getPlayer();
		task.resetPlayerData(player);
	}
	
	private static class AntiCampingTask implements Runnable {
	
		private static final Comparator<Floor> COMPARATOR = new FloorComparator();
		private final HeavySpleef heavySpleef;
		private final I18N i18n = I18NManager.getGlobal();
		private GameManager gameManager;
		private DefaultConfig config;
		private Map<SpleefPlayer, Location> recentLocations = Maps.newHashMap();
		private Map<SpleefPlayer, Integer> secondsCamping = Maps.newHashMap();
		
		public AntiCampingTask(HeavySpleef heavySpleef) {
			this.heavySpleef = heavySpleef;
			this.gameManager = heavySpleef.getGameManager();
			
			config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
		}
		
		@Override
		public void run() {
			final FlagSection flagSection = config.getFlagSection();
			final int anticampingWarn = flagSection.getAnticampingWarn();
			final int anticampingTeleport = flagSection.getAnticampingTeleport();
			final boolean warn = flagSection.isAnticampingDoWarn();
			
			for (Game game : gameManager.getGames()) {
				if (game.getGameState() != GameState.INGAME) {
					continue;
				}
				
				if (!game.isFlagPresent(FlagAntiCamping.class)) {
					continue;
				}
						
				ImmutableList<SpleefPlayer> ingame = ImmutableList.copyOf(game.getPlayers());
				for (SpleefPlayer player : ingame) {
					if (recentLocations.containsKey(player)) {
						Location recent = recentLocations.get(player);
						Location now = player.getBukkitPlayer().getLocation();
						
						double dx = Math.abs(now.getX() - recent.getX());
						double dz = Math.abs(now.getZ() - recent.getZ());
						
						if ((dx < 1D && dz < 1D) || player.getBukkitPlayer().isSneaking()) {
							int seconds = secondsCamping.containsKey(player) ? secondsCamping.get(player) + 1 : 1;
							
							if (seconds == anticampingWarn && warn) {
								player.sendMessage(i18n.getString(Messages.Player.ANTICAMPING_WARN));
							}
							
							if (seconds == anticampingTeleport) {
								if (teleport(player, game)) {
									player.sendMessage(i18n.getString(Messages.Player.ANTICAMPING_TELEPORT));
									secondsCamping.remove(player);
								}
							} else {
								secondsCamping.put(player, seconds);
							}
						} else {
							secondsCamping.remove(player);
						}
					}
					
					recentLocations.put(player, player.getBukkitPlayer().getLocation());
				}
			}
		}
		
		private boolean teleport(SpleefPlayer player, Game game) {
			Location location = player.getBukkitPlayer().getLocation();
			double y = location.getY();
			
			Collection<Floor> floorsCollection = game.getFloors();
			List<Floor> floors = Lists.newArrayList(floorsCollection);
			
			Floor nearestFloor = null;

			// Calculate the nearest floor
			for (Floor floor : floors) {
				Region region = floor.getRegion();
				int minrY = region.getMinimumPoint().getBlockY();
				int maxrY = region.getMaximumPoint().getBlockY();
				double dyMinR = Math.abs(y - minrY);
				double dyMaxR = Math.abs(y - maxrY);
				
				if (nearestFloor == null) {
					nearestFloor = floor;
					continue;
				}

				Region nearestFloorRegion = nearestFloor.getRegion();
				int minnrY = nearestFloorRegion.getMinimumPoint().getBlockY();
				int maxnrY = nearestFloorRegion.getMaximumPoint().getBlockY();
				double dyMinNr = Math.abs(y - minnrY);
				double dyMaxNr = Math.abs(y - maxnrY);
				
				if ((dyMinR < dyMinNr && dyMinR < dyMaxNr) || (dyMaxR < dyMinNr && dyMaxR < dyMaxNr)) {
					nearestFloor = floor;
				}
			}

			if (nearestFloor == null) {
				return false;
			}

			Collections.sort(floors, COMPARATOR);
			
			for (int i = 0; i < floors.size(); i++) {
				Floor floor = floors.get(i);
				
				// Check if the player is at the last floor
				if (nearestFloor == floor) {
					if (i == 0) {
						game.requestLose(player, QuitCause.LOSE);
					} else {
						Region region = floors.get(i - 1).getRegion();
						int maxY = region.getMaximumPoint().getBlockY();
						
						location.setY(maxY + 1.25);
						player.getBukkitPlayer().teleport(location);
					}
					
					return true;
				}
			}
			
			return false;
		}
		
		private void resetPlayerData(SpleefPlayer player) {
			recentLocations.remove(player);
			secondsCamping.remove(player);
		}
		
		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			handlePlayerLeave(event);
		}
		
		@EventHandler
		public void onPlayerKick(PlayerKickEvent event) {
			handlePlayerLeave(event);
		}
		
		private void handlePlayerLeave(PlayerEvent event) {
			SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
			resetPlayerData(player);
		}
		
		private static class FloorComparator implements Comparator<Floor> {

			@Override
			public int compare(Floor o1, Floor o2) {
				Region region1 = o1.getRegion();
				Region region2 = o2.getRegion();
				
				double avgY1 = region1.getMaximumPoint().subtract(region1.getMinimumPoint()).getBlockY() / 2D;
				double avgY2 = region2.getMaximumPoint().subtract(region2.getMinimumPoint()).getBlockY() / 2D;
				
				return Double.valueOf(avgY1).compareTo(avgY2);
			}
			
		}
		
	}

}
