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
package de.matzefratze123.heavyspleef.core.task;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.core.flag.ListFlagLocation.SerializeableLocation;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class TaskPlayerTeleport implements Runnable {

	private Game game;
	private List<SimpleBlockData> changedBlocks;

	public TaskPlayerTeleport(Game game) {
		this.game = game;
		this.changedBlocks = new ArrayList<SimpleBlockData>();
	}

	@Override
	public void run() {
		Location defaultSpawnpoint = game.getFlag(FlagType.SPAWNPOINT);
		List<SerializeableLocation> spawnpoints = game.getFlag(FlagType.NEXTSPAWNPOINT);
		
		List<SpleefPlayer> players = game.getIngamePlayers();
		
		for (int i = 0; i < players.size(); i++) {
			SpleefPlayer player = players.get(i);
			Location teleportTo;
			
			Team team = game.getComponents().getTeam(player);
			if (team != null && team.getSpawnpoint() != null) {
				teleportTo = team.getSpawnpoint();
			} else if (spawnpoints != null && i < spawnpoints.size()) {
				Location bukkitLocation = spawnpoints.get(i).getBukkitLocation();
				
				teleportTo = bukkitLocation.clone();
			} else if (defaultSpawnpoint != null) {
				teleportTo = defaultSpawnpoint.clone();
			} else {
				Location randomLocation = game.getRandomLocation();
				
				teleportTo = randomLocation;
			}
			
			if (game.getFlag(FlagType.BOXES) && game.getFlag(FlagType.ONEVSONE)) {
				generateBox(teleportTo);
				
				//Add a half block to prevent box glitches
				teleportTo = new Location(teleportTo.getWorld(), teleportTo.getBlockX(), teleportTo.getBlockY(), teleportTo.getBlockZ());
				teleportTo.add(0.5, 0, 0.5);
			}
			
			//We have to teleport the player after the boxes were build. Reason: Otherwise players can glitch out
			player.getBukkitPlayer().teleport(teleportTo);
		}
	}

	private void generateBox(Location location) {
		if (location == null)
			return;
		
		//List all blockfaces which are relevant to change
		BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH_EAST,
				BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST,
				BlockFace.SOUTH_WEST, BlockFace.SELF };
		
		Location loc = location.clone();

		for (int i = 0; i < 3; i++) {
			for (BlockFace face : faces) {
				//Do not generate blocks where the player stands
				if (i < 2 && face == BlockFace.SELF)
					continue;
				
				Block block = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + i, loc.getBlockZ()).getRelative(face);
				
				if (checkBlockSave(block.getLocation())) {
					changedBlocks.add(new SimpleBlockData(block));
				}
				block.setType(Material.GLASS);
			}
		}
	}
	
	private boolean checkBlockSave(Location location) {
		int x, y, z;
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
		
		for (SimpleBlockData data : changedBlocks) {
			if (data.getX() == x && data.getY() == y && data.getZ() == z) {
				return false;
			}
		}
		
		return true;
	}
	
	public void removeBoxes() {
		for (SimpleBlockData data : changedBlocks) {
			Block block = data.getWorld().getBlockAt(data.getLocation());
			block.setTypeIdAndData(data.getMaterial().getId(), data.getData(), false);
		}
		
		changedBlocks.clear();
	}

}
