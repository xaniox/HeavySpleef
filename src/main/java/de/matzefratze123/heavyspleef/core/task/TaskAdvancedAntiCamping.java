/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.region.FloorCuboid;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.ArrayHelper;
import de.matzefratze123.heavyspleef.util.RegionIterator;

public class TaskAdvancedAntiCamping implements Runnable {

	private static final long PERIOD = 5 * 20L;
	private static final int PLATFORM_LIMIT = 45;
	private static final BlockFace[] CHECKING_FACES = {BlockFace.NORTH, BlockFace.SOUTH,
													   BlockFace.WEST, BlockFace.EAST};
	
	private static final BlockFaceIntegerPair[][][] CHECKIN_REGIONS_IN_FRONT = {
		{{new BlockFaceIntegerPair(UnattachedBlockFace.FORWARDS, 2), new BlockFaceIntegerPair(UnattachedBlockFace.RIGHT, 3)},
		 {new BlockFaceIntegerPair(UnattachedBlockFace.FORWARDS, 1), new BlockFaceIntegerPair(UnattachedBlockFace.LEFT, 3)}},
		{{new BlockFaceIntegerPair(UnattachedBlockFace.FORWARDS, 3), new BlockFaceIntegerPair(UnattachedBlockFace.RIGHT, 2)},
		 {new BlockFaceIntegerPair(UnattachedBlockFace.FORWARDS, 3), new BlockFaceIntegerPair(UnattachedBlockFace.LEFT, 2)}},
	};
	
	private int tid = -1;
	
	public TaskAdvancedAntiCamping() {}
	
	public void start() {
		if (tid != -1) {
			throw new IllegalStateException("task is already running");
		}
		
		tid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 0L, PERIOD);
	}
	
	public void stop() {
		if (tid == -1) {
			throw new IllegalStateException("task is not running");
		}
		
		Bukkit.getScheduler().cancelTask(tid);
		tid = -1;
	}
	
	public void restart() {
		if (isRunning()) {
			stop();
		}
	
		start();
	}
	
	public boolean isRunning() {
		return tid != -1;
	}

	@Override
	public void run() {
		for (Game game : GameManager.getGames()) {
			if (game.getGameState() != GameState.INGAME) {
				continue;
			}
			
			for (SpleefPlayer player : game.getIngamePlayers()) {
				checkPlayer(player);
			}
		}
	}
	
	private void checkPlayer(SpleefPlayer player) {
		//Floor calculation
		IFloor standingOn = null;
		Location playerLocation = player.getBukkitPlayer().getLocation();
		
		for (IFloor floor : player.getGame().getComponents().getFloors()) {
			if (floor.getY() > playerLocation.getBlockY()) {
				continue;
			}
			
			if (standingOn == null || playerLocation.getBlockY() - floor.getY() <  playerLocation.getBlockY() - standingOn.getY()) {
				standingOn = floor;
			}
		}
				
		if (standingOn == null) {
			return;
		}
		
		if (!(standingOn instanceof FloorCuboid)) {
			return;
		}
		
		int amountOfBlocks = getAmountOfBlocks((FloorCuboid) standingOn);
		
		//Don't check the player if the platform where he is standing on is to large
		if (amountOfBlocks > PLATFORM_LIMIT) {
			return;
		}
		
		Block block = standingOn.getWorld().getBlockAt(playerLocation.getBlockX(), standingOn.getY(), playerLocation.getBlockZ());
		List<Block> connected = new ArrayList<Block>();
		connected.add(block);
		
		indicateConnectedBlocks(block, connected, player);
		
		if (amountOfBlocks == connected.size()) {
			return;
		}
		
		if (checkCamping(connected, player)) {
			TaskAntiCamping.teleportDown(player);
		}
	}
	
	private void indicateConnectedBlocks(Block block, List<Block> list, SpleefPlayer player) {		 
		for (BlockFace face : CHECKING_FACES) {
			final Block relative = block.getRelative(face);
			
			if (list.contains(relative) || !player.getGame().canSpleef(player, relative.getLocation())) {
				continue;
			}
			
			if (relative.getType() == Material.AIR) {
				continue;
			}
			
			list.add(relative);
			
			indicateConnectedBlocks(relative, list, player);
		}
	}
	
	private boolean checkCamping(List<Block> checked, SpleefPlayer player) {
		boolean result = true;
		boolean hasAirAsRelative = false;
		
		final int size = checked.size();
		
		list_loop:
		for (int i = 0; i < size; i++) {
			Block block = checked.get(i);
			
			for (BlockFace face : CHECKING_FACES) {
				final Block relative = block.getRelative(face);
				
				if (!player.getGame().canSpleef(player, relative.getLocation())) {
					continue;
				}
				
				if (relative.getType() == Material.AIR) {
					hasAirAsRelative = true;
					
					//Check area
					for (BlockFaceIntegerPair[][] region : CHECKIN_REGIONS_IN_FRONT) {
						BlockFaceIntegerPair[] pathToFirst = region[0];
						BlockFaceIntegerPair[] pathToSecond = region[1];
						
						Block first = getRelative(pathToFirst, block, face);
						Block second = getRelative(pathToSecond, block, face);
						
						RegionIterator iterator = new RegionIterator(first, second);
						
						for (Block regionBlock : iterator) {
							if (!player.getGame().canSpleef(player, regionBlock.getLocation()) || checked.contains(regionBlock)) {
								continue;
							}
							
							if (regionBlock.getType() != Material.AIR) {
								result = false;
								break list_loop;
							}
						}
					}
				}
			}
		}
		
		return result && hasAirAsRelative;
	}
	
	private int getAmountOfBlocks(FloorCuboid floor) {
		int amount = 0;
		
		RegionIterator iterator = ((FloorCuboid)floor).getRegionIterator();
		
		for (Block block : iterator) {
			if (block.getType() == Material.AIR) {
				continue;
			}
			
			amount++;
		}
		
		return amount;
	}
	
	private boolean[] checkRelative(Block block, List<Block> checked, SpleefPlayer player) {
		boolean result = true;
		boolean hasAirAsRelative = false;
		
		face_loop: 
		for (BlockFace face : CHECKING_FACES) {
			final Block relative = block.getRelative(face);
			
			if (checked.contains(relative) || !player.getGame().canSpleef(player, relative.getLocation())) {
				continue;
			}
			
			if (relative.getType() == Material.AIR) {
				hasAirAsRelative = true;
				
				if (!result) {
					break;
				}
				
				//Check area
				for (BlockFaceIntegerPair[][] region : CHECKIN_REGIONS_IN_FRONT) {
					BlockFaceIntegerPair[] pathToFirst = region[0];
					BlockFaceIntegerPair[] pathToSecond = region[1];
					
					Block first = getRelative(pathToFirst, block, face);
					Block second = getRelative(pathToSecond, block, face);
					
					RegionIterator iterator = new RegionIterator(first, second);
					
					for (Block regionBlock : iterator) {
						if (!player.getGame().canSpleef(player, regionBlock.getLocation())) {
							continue;
						}
						
						if (regionBlock.getType() != Material.AIR) {
							result = false;
							break face_loop;
						}
					}
				}
			} else {
				//Check other blockfaces
				checked.add(relative);
				
				boolean[] subResult = checkRelative(relative, checked, player);
				result &= subResult[0];
				hasAirAsRelative |= subResult[1];
			}
		}
		
		return new boolean[] {result, hasAirAsRelative};
	}
	
	private Block getRelative(BlockFaceIntegerPair[] path, Block start, BlockFace direction) {
		for (int i = 0; i < path.length; i++) {
			BlockFaceIntegerPair pathFragment = path[i];
			
			for (int j = 0; j < pathFragment.i; j++) {
				start = start.getRelative(pathFragment.blockFace.getBukkitBlockFace(direction));
			}
		}
		
		return start;
	}
	
	public static enum UnattachedBlockFace {
		
		RIGHT(BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH),
		LEFT(BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH),
		BACK(BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST),
		FORWARDS(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);
		
		private static final BlockFace[] ALLOWED_BLOCKFACES = CHECKING_FACES;
		
		private BlockFace[] RELATIVE_BLOCKFACES;
		
		private UnattachedBlockFace(BlockFace... relatives) {
			RELATIVE_BLOCKFACES = relatives;
		}
		
		public BlockFace getBukkitBlockFace(BlockFace base) {
			Validate.isTrue(ArrayHelper.contains(ALLOWED_BLOCKFACES, base), "Illegal blockface " + base.name());
			
			int index = ArrayHelper.getIndex(ALLOWED_BLOCKFACES, base);
			
			return RELATIVE_BLOCKFACES[index];
		}
		
	}
	
	private static class BlockFaceIntegerPair {
		
		private UnattachedBlockFace blockFace;
		private int i;
		
		private BlockFaceIntegerPair(UnattachedBlockFace face, int i) {
			this.blockFace = face;
			this.i = i;
		}
		
	}

}
