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
package de.xaniox.heavyspleef.core.game;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.SimpleBasicTask;
import de.xaniox.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.xaniox.heavyspleef.core.event.SpleefListener;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;

import java.util.Map;
import java.util.Set;

public class LoseCheckerTask extends SimpleBasicTask implements SpleefListener {
	
	private static final Set<Material> FLOWING_MATERIALS = Sets.newHashSet(Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
	
	private final GameManager gameManager;
	private Map<SpleefPlayer, Location> recentLocations;
	
	public LoseCheckerTask(HeavySpleef heavySpleef) {
		super(heavySpleef.getPlugin(), TaskType.SYNC_REPEATING_TASK, 0L, 4L);
		
		this.gameManager = heavySpleef.getGameManager();
		this.recentLocations = Maps.newHashMap();
	}

	@Override
	public void run() {
		for (Game game : gameManager.getGames()) {
			if (game.getGameState() != GameState.INGAME) {
				continue;
			}
			
			Set<SpleefPlayer> deathCandidates = null;
			final boolean isLiquidDeathzone = game.getPropertyValue(GameProperty.USE_LIQUID_DEATHZONE);
			
			for (SpleefPlayer player : game.getPlayers()) {
				Location playerLoc = player.getBukkitPlayer().getLocation();
				
				boolean isDeathCandidate = isInsideDeathzone(playerLoc, game, isLiquidDeathzone);
				if (!isDeathCandidate && recentLocations.containsKey(player)) {
					//Try to check every block the player has passed between the recent location and his location now
					Location recent = recentLocations.get(player);
					org.bukkit.util.Vector direction = playerLoc.clone().subtract(recent).toVector();
					int directionLength = (int) direction.length();
					
					if (directionLength > 0) {
						BlockIterator iterator = new BlockIterator(game.getWorld(), recent.toVector(), direction, 0D, directionLength);
						while (iterator.hasNext()) {
							Block passedBlock = iterator.next();
							
							if (isInsideDeathzone(passedBlock.getLocation(), game, isLiquidDeathzone)) {
								isDeathCandidate = true;
								break;
							}
						}
					}
				}
				
				if (isDeathCandidate) {
					//Lazy initialization for performance optimization
					if (deathCandidates == null) {
						deathCandidates = Sets.newHashSet();
					}
					
					deathCandidates.add(player);
				}
				
				recentLocations.put(player, playerLoc);
			}
			
			if (deathCandidates != null) {
				for (SpleefPlayer deathCandidate : deathCandidates) {
					game.requestLose(deathCandidate, QuitCause.LOSE);
				}
			}
		}
	}
	
	private boolean isInsideDeathzone(Location location, Game game, boolean useLiquidDeathzone) {
		boolean result = false;
		if (useLiquidDeathzone && FLOWING_MATERIALS.contains(location.getBlock().getType())) {
			result = true;
		} else {
			Vector vec = BukkitUtil.toVector(location);
			
			for (Region deathzone : game.getDeathzones().values()) {
				if (deathzone.contains(vec)) {
					//Location is in deathzone
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	@Subscribe
	public void onPlayerLeaveGameEvent(PlayerLeaveGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		recentLocations.remove(player);
	}

}