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
package me.matzefratze123.heavyspleef.core;

import java.util.ArrayList;
import java.util.List;

import me.matzefratze123.heavyspleef.core.region.RegionBase;
import me.matzefratze123.heavyspleef.database.Parser;
import me.matzefratze123.heavyspleef.utility.ArrayHelper;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Represents a scoreboard for a game
 * This is still buggy and shouldn't be used...
 * 
 * @author matzefratze123
 */
public class ScoreBoard {

	private Location firstCorner;
	private Location secondCorner;
	
	private int numberID = 35;
	private byte numberData = 14;
	
	private int otherID = 35;
	private byte otherData = 15;
	
	private Location firstNumberPoint;
	private Location secondNumberPoint;
	
	private BlockFace face;
	private Game game;
	private int id = -1;
	
	public ScoreBoard(Location loc, int id, Game game, BlockFace face) {
		this.face = face;
		this.game = game;
		this.id = id;
		
		Block xOrZ = loc.getBlock().getRelative(face, 19);
		Block xOrZAndY = xOrZ.getRelative(BlockFace.DOWN, 6);
		
		this.firstCorner = Parser.roundLocation(loc);
		this.secondCorner = Parser.roundLocation(xOrZAndY.getLocation());
		
		calculateNumberPoints();
		
		System.out.println(Parser.convertLocationtoString(firstCorner));
		System.out.println(Parser.convertLocationtoString(secondCorner));
	}
	
	public ScoreBoard(String fromString, Game game) {
		String[] parts = fromString.split(";");
		if (parts.length < 7)
			return;
		
		try {
			int id = Integer.parseInt(parts[0]);
			Location firstCorner = Parser.convertStringtoLocation(parts[1]);
			BlockFace face = BlockFace.valueOf(parts[2].toUpperCase());
			int numberID = Integer.parseInt(parts[3]);
			byte numberData = Byte.parseByte(parts[4]);
			int otherID = Integer.parseInt(parts[5]);
			byte otherData = Byte.parseByte(parts[6]);
			
			this.id = id;
			this.firstCorner = firstCorner;
			this.face = face;
			this.numberData = numberData;
			this.numberID = numberID;
			this.otherID = otherID;
			this.otherData = otherData;
			this.game = game;
			
			Block xOrZ = firstCorner.getBlock().getRelative(face, 19);
			Block xOrZAndY = xOrZ.getRelative(BlockFace.DOWN, 6);
			
			this.secondCorner = xOrZAndY.getLocation();
			
			calculateNumberPoints();
		} catch (NumberFormatException e) {}
	}
	
	private void calculateNumberPoints() {
		Block b1_1 = firstCorner.getBlock().getRelative(BlockFace.DOWN, 1);
		Block b1_2 = b1_1.getRelative(face, 1);
		
		this.firstNumberPoint = b1_2.getLocation();
		this.secondNumberPoint = b1_2.getRelative(face, 10).getLocation();
	}
	
	public int getNumberId() {
		return this.numberID;
	}
	
	public byte getNumberData() {
		return this.numberData;
	}
	
	public int getOtherId() {
		return this.otherID;
	}
	
	public byte getOtherData() {
		return this.otherData;
	}
	
	public void draw() {
		int[] wins = game.getWins();
		NumberData data = new NumberData();
		
		if (wins.length < 2) {
			int[] newWins = new int[2];
			newWins[0] = 0;
			newWins[1] = 0;
			
			wins = newWins;
		}
		
		int minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
		int maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
		
		int minY = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
		int maxY = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
		
		int minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		int maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					Block b = firstCorner.getWorld().getBlockAt(x, y, z);
					
					b.setTypeId(otherID);
					b.setData(otherData);
				}
			}
		}
		
		Location[] loc1 = data.getLocations(firstNumberPoint, wins[0]);
		Location[] loc2 = data.getLocations(secondNumberPoint, wins[1]);
		
		ArrayList<Location> allLocation = ArrayHelper.mergeArrays(loc1, loc2);
		
		Location firstColon_1 = firstCorner.getBlock().getRelative(BlockFace.DOWN, 2).getLocation();
		Location firstColon_2 = firstColon_1.getBlock().getRelative(face, 9).getLocation();
		
		Location secondColon_2 = firstColon_2.getBlock().getRelative(BlockFace.DOWN, 2).getLocation();
		allLocation.add(secondColon_2);
		allLocation.add(firstColon_2);
		
		for (Location loc : allLocation) {
			loc.getBlock().setTypeId(numberID);
			loc.getBlock().setData(numberData);
		}
	}
	
	public void remove() {
		int minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
		int maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
		
		int minY = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
		int maxY = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
		
		int minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		int maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					Block b = firstCorner.getWorld().getBlockAt(x, y, z);
					
					b.setTypeId(0);
				}
			}
		}
	}
	
	public boolean contains(Location l) {
		return RegionBase.contains(firstCorner, secondCorner, l);
	}
	
	@Override
	public String toString() {
		return id + ";" + Parser.convertLocationtoString(firstCorner) + ";" + face.name() + ";" + numberID + ";" + numberData + ";" + otherID + ";" + otherData;
	}
	
	public int getId() {
		return this.id;
	}
	
	/**
	 * Contains methods for numbers and other stuff
	 * 
	 * @author matzefratze123
	 */
	public class NumberData {
		
		public Location[] getLocations(Location loc, int number) {
			List<Location> locations = new ArrayList<Location>();
			
			String s = String.valueOf(number);
			if (s.length() > 2)
				s = s.substring(0, 2);
			char[] numbers = s.toCharArray();
			
			if (numbers.length == 1) {
				Location[] zero = getLocation(0, loc);
				Location[] num = getLocation(Integer.parseInt(String.valueOf(numbers[0])), loc.getBlock().getRelative(face, 4).getLocation());
				
				ArrayList<Location> mergedArray = ArrayHelper.mergeArrays(zero, num);
				locations.addAll(mergedArray);
				
				return locations.toArray(new Location[locations.size()]);
			} else if (numbers.length == 2) {
				Location[] num1 = getLocation(Integer.parseInt(String.valueOf(numbers[0])), loc);
				Location[] num2 = getLocation(Integer.parseInt(String.valueOf(numbers[1])), loc.getBlock().getRelative(face, 4).getLocation());
				
				ArrayList<Location> mergedArray = ArrayHelper.mergeArrays(num1, num2);
				locations.addAll(mergedArray);
				return locations.toArray(new Location[locations.size()]);
			}
			
			//Should not happen
			return null;
		}
		
		public Location[] getLocation(int i, Location loc) {
			switch(i) {
			case 0:
				return zero(loc);
			case 1:
				return one(loc);
			case 2:
				return two(loc);
			case 3:
				return three(loc);
			case 4:
				return four(loc);
			case 5:
				return five(loc);
			case 6:
				return six(loc);
			case 7:
				return seven(loc);
			case 8:
				return eight(loc);
			case 9:
				return nine(loc);
			default:
				return new Location[1];
			}
		}
		
		private Location[] zero(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if (y > 0 && y < 4 && i == 1)
						continue;
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] one(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
				
				list.add(b1.getLocation());
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] two(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if (y == 1 && i < 2)
						continue;
					if (y == 3 && i > 0)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] three(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if ((y == 1 || y == 3) && i < 2)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] four(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if (y < 2 && i == 1)
						continue;
					if (y > 2 && i < 2)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] five(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if (y == 1 && i > 0)
						continue;
					if (y == 3 && i < 2)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] six(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if (y == 1 && i > 0)
						continue;
					if (y == 3 && i == 1)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] seven(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if (y > 0 && i < 2)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] eight(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if ((y == 1 || y == 3) && i == 1)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
		private Location[] nine(Location loc) {
			List<Location> list = new ArrayList<Location>();
			
			for (int y = 0; y <= 4; y++) {
				for (int i = 0; i <= 2; i++) {
					if (y == 1 && i == 1)
						continue;
					if (y == 3 && i < 2)
						continue;
					
					Block b1 = loc.getBlock().getRelative(BlockFace.DOWN, y);
					Block b2 = b1.getRelative(face, i);
					
					list.add(b2.getLocation());
				}
			}
			
			return list.toArray(new Location[list.size()]);
		}
		
	}

}
