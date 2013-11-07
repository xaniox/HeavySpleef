/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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

import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Permissions;

/**
 * Represents a portal in a world which points to the spleef hub
 * 
 * @author matzefratze123
 *
 */
public class HUBPortal extends RegionCuboid {

	public HUBPortal(int id, Location firstCorner, Location secondCorner) {
		super(id, firstCorner, secondCorner);
	}
	
	public HUBPortal(Location firstCorner, Location secondCorner) {
		this(-1, firstCorner, secondCorner);
	}
	
	public boolean isIllegalId() {
		return id < 0;
	}
	
	public void setId(int id) {
		super.id = id;
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
		player.sendMessage(LanguageHandler._("welcomeToHUB"));
		
		//Effect
		int minX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());
		int maxX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
		
		int minY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());
		int maxY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
		
		int minZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		int maxZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block currentBlock = firstPoint.getWorld().getBlockAt(x, y, z);
					
					if (SimpleBlockData.isSolid(currentBlock.getTypeId()))
						continue;
					for (int i = 0; i < 5; i++)
						currentBlock.getWorld().playEffect(currentBlock.getLocation(), Effect.SMOKE, 4);
				}
			}
		}
	}
	
}
