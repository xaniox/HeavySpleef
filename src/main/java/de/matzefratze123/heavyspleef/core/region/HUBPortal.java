/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
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
package de.matzefratze123.heavyspleef.core.region;


import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;

/**
 * Represents a portal in a world which points to the spleef hub
 * 
 * @author matzefratze123
 *
 */
public class HUBPortal extends RegionBase {

	private Location firstCorner;
	private Location secondCorner;
	
	public HUBPortal(int id, Location firstCorner, Location secondCorner) {
		super(id);
		
		this.firstCorner = firstCorner;
		this.secondCorner = secondCorner;
	}
	
	public HUBPortal(Location firstCorner, Location secondCorner) {
		this(-1, firstCorner, secondCorner);
	}

	@Override
	public boolean contains(Location location) {
		return RegionBase.contains(firstCorner, secondCorner, location);
	}
	
	public Location getFirstCorner() {
		return this.firstCorner;
	}
	
	public Location getSecondCorner() {
		return this.secondCorner;
	}
	
	public void onMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		
		if (!contains(player.getLocation()))
			return;
		if (!player.hasPermission(Permissions.USE_PORTAL.getPerm())) 
			return;
		
		travel(player);
	}
	
	private void travel(Player player) {
		if (GameManager.getSpleefHub() == null)
			return;
		
		player.teleport(GameManager.getSpleefHub());
		player.sendMessage(Game._("welcomeToHUB"));
		
		//Effect
		int minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
		int maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
		
		int minY = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
		int maxY = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
		
		int minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		int maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block currentBlock = firstCorner.getWorld().getBlockAt(x, y, z);
					
					if (SimpleBlockData.isSolid(currentBlock.getTypeId()))
						continue;
					for (int i = 0; i < 5; i++)
						currentBlock.getWorld().playEffect(currentBlock.getLocation(), Effect.SMOKE, 4);
				}
			}
		}
	}
	
}
