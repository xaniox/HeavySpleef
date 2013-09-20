package de.matzefratze123.heavyspleef.core.task;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;

public class PlayerTeleportTask implements Runnable {

	private Game game;
	private List<SimpleBlockData> changedBlocks;

	public PlayerTeleportTask(Game game) {
		this.game = game;
		this.changedBlocks = new ArrayList<SimpleBlockData>();
	}

	@Override
	public void run() {
		
		boolean firstSpawnGenerated = false;

		Location firstSpawnpoint = game.getFlag(FlagType.SPAWNPOINT1);
		Location secondSpawnpoint = game.getFlag(FlagType.SPAWNPOINT2);
		
		for (Player player : game.getPlayers()) {
			if (game.getFlag(FlagType.ONEVSONE)) {
				//Check if first spawn has been generated and spawnpoint is not null
				if (!firstSpawnGenerated && firstSpawnpoint != null) {
					generateBox(firstSpawnpoint);
					firstSpawnGenerated = true;
				} else if (secondSpawnpoint != null) {
					generateBox(secondSpawnpoint);
				}
				
			} else {
				Location spawnPoint = game.getFlag(FlagType.SPAWNPOINT);

				if (spawnPoint != null) {
					// Spawnpoint flag not null, teleport player to the flag destination
					player.teleport(spawnPoint);
				} else {
					// No spawnpoint flag, randomly teleport player into the arena
					player.teleport(game.getRandomLocation());
				}
			}
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
				
				changedBlocks.add(new SimpleBlockData(block));
				block.setType(Material.GLASS);
			}
		}
	}
	
	/**
	 * This method is called on the start() in Game.java
	 */
	public void removeBoxes() {
		for (SimpleBlockData data : changedBlocks) {
			Block block = data.getWorld().getBlockAt(data.getLocation());
			block.setTypeIdAndData(data.getMaterial().getId(), data.getData(), false);
		}
		
		changedBlocks.clear();
	}

}
