package de.matzefratze123.heavyspleef.core.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class TNTRunTask implements Runnable {

	private static boolean started = false;
	private static final long REPETITON = 4L;
	
	private TNTRunRemoveBlocksTask belongingTask;
	
	public void start() {
		if (started) {
			throw new IllegalStateException("Task already running!");
		}
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 0L, REPETITON);
		belongingTask = new TNTRunRemoveBlocksTask();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), belongingTask, 0L, REPETITON * 2);
	}

	@Override
	public void run() {
		for (Game game : GameManager.getGames()) {
			if (game.getGameState() != GameState.INGAME) {
				continue;
			}
			if (!game.getFlag(FlagType.TNTRUN)) {
				return;
			}
			
			for (SpleefPlayer player : game.getIngamePlayers()) {
				Location playerLoc = player.getBukkitPlayer().getLocation();
				Block underPlayer = playerLoc.getBlock().getRelative(BlockFace.DOWN);
				
				double px, pz, bx, bz;
				
				px = playerLoc.getX();
				pz = playerLoc.getZ();
				
				bx = underPlayer.getX();
				bz = underPlayer.getZ();
				
				if (underPlayer.getType() == Material.AIR) {
					BlockFace[] possibleFaces = new BlockFace[3];
					
					if (px > bx + 0.5) {
						if (pz > bz + 0.5) {
							//SOUTH, EAST
							possibleFaces[0] = BlockFace.SOUTH;
							possibleFaces[1] = BlockFace.EAST;
							possibleFaces[2] = BlockFace.SOUTH_EAST;
						} else {
							//NORTH, EAST
							possibleFaces[0] = BlockFace.NORTH;
							possibleFaces[1] = BlockFace.EAST;
							possibleFaces[2] = BlockFace.NORTH_EAST;
						}
					} else {
						if (pz > bz + 0.5) {
							//SOUTH, WEST
							possibleFaces[0] = BlockFace.SOUTH;
							possibleFaces[1] = BlockFace.WEST;
							possibleFaces[2] = BlockFace.SOUTH_WEST;
						} else {
							//NORTH, WEST
							possibleFaces[0] = BlockFace.NORTH;
							possibleFaces[1] = BlockFace.WEST;
							possibleFaces[2] = BlockFace.NORTH_WEST;
						}
					}
					
					for (BlockFace face : possibleFaces) {
						Block block = underPlayer.getRelative(face);
						if (block.getType() == Material.AIR) {
							continue;
						}
						
						underPlayer = block;
					}
				}
				
				if (!game.canSpleef(player, underPlayer.getLocation())) {
					continue;
				}
				
				belongingTask.queue(underPlayer);
				/*
				while (true) {
					int currentY = current == null ? playerLoc.getBlockY() - 1 : current.getLocation().getBlockY() - 1;
					if (currentY < 0) {
						break;
					}
					
					if (current == null) {
						current = playerLoc.getBlock().getRelative(BlockFace.DOWN);
					} else {
						current = current.getRelative(BlockFace.DOWN);
					}
					
					if (current.getType() == Material.AIR) {
						break;
					} else {
w						if (game.canSpleef(player, current.getLocation())) {
							belongingTask.queue(current);
							player.addBrokenBlock(current);
						}
					}
				}*/
			}
		}
	}

}
