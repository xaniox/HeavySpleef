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

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class LoseCheckerTask extends SimpleBasicTask {
	
	private static final Set<Material> FLOWING_MATERIALS = Sets.newHashSet(Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
	
	private final GameManager gameManager;
	
	public LoseCheckerTask(Plugin plugin, GameManager gameManager) {
		super(plugin, TaskType.SYNC_REPEATING_TASK, 0L, 10L);
		
		this.gameManager = gameManager;
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
				
				boolean isDeathCandidate = false;
				if (isLiquidDeathzone && FLOWING_MATERIALS.contains(playerLoc.getBlock().getType())) {
					isDeathCandidate = true;
				} else {
					Vector playerPos = BukkitUtil.toVector(playerLoc);
					
					for (CuboidRegion deathzone : game.getDeathzones()) {
						if (deathzone.contains(playerPos)) {
							//Player is in deathzone, so take him out
							isDeathCandidate = true;
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
			}
			
			if (deathCandidates != null) {
				for (SpleefPlayer deathCandidate : deathCandidates) {
					game.requestLose(deathCandidate);
				}
			}
		}
	}

}
