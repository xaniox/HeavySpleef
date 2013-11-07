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
package de.matzefratze123.heavyspleef.core;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.database.DatabaseSerializeable;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Logger;

public class ScoreBoard extends RegionCuboid implements DatabaseSerializeable {
	
	/*
	 * Defines the segments of a character
	 */
	private static Map<Character, String> charCodes;
	
	/*
	 * Static initialisator. Define character segments 
	 */
	static {
		if (charCodes == null) {
			charCodes = new HashMap<Character, String>();
			
			charCodes.put('0', "abcdef");
			charCodes.put('1', "bc");
			charCodes.put('2', "abged");
			charCodes.put('3', "abgcd");
			charCodes.put('4', "bcgf");
			charCodes.put('5', "afgcd");
			charCodes.put('6', "afedcg");
			charCodes.put('7', "abc");
			charCodes.put('8', "abcdefg");
			charCodes.put('9', "abcdfg");
		}
	}
	
	private SegmentDisplay[] displays;
	private Location[] colon;
	
	private BlockFace direction;
	
	/**
	 * Creates a new scoreboard
	 * 
	 * @param firstPointLocation The upper-left firstPoint of this scoreboard
	 * @param direction The orentation blockface of this scoreboard
	 */
	public ScoreBoard(int id, Location firstPointLocation, BlockFace direction) {
		super(id, firstPointLocation, null);
		
		this.direction = direction;
		
		calculateColons();
		calculateDisplays();
		
		secondPoint = move(firstPoint, direction, 18, BlockFace.DOWN, 6);
	}
	
	protected void setId(int id) {
		super.id = id;
	}
	
	private void calculateColons() {
		colon = new Location[2];
		
		Location firstPoint = this.firstPoint.clone();
		
		colon[0] = move(firstPoint, direction, 9, BlockFace.DOWN, 2);
		colon[1] = move(firstPoint, direction, 9, BlockFace.DOWN, 4);
	}
	
	private void calculateDisplays() {
		//             Display
		//   -->   -->         -->   -->
		//   [0]   [1]         [2]   [3]
		//   ___   ___         ___   ___  
		//  / _ \ / _ \   _   / _ \ / _ \ 
		// | | | | | | | (_) | | | | | | |
		// | |_| | |_| |  _  | |_| | |_| |
		//  \___/ \___/  (_)  \___/ \___/ 
		//
		
		
		displays = new SegmentDisplay[4];
		
		Location firstPoint = this.firstPoint.clone();
		
		firstPoint = move(firstPoint, BlockFace.DOWN, 1, direction, 1);
		displays[0] = new SegmentDisplay(firstPoint, direction);
		
		firstPoint = move(firstPoint, direction, 4);
		displays[1] = new SegmentDisplay(firstPoint, direction);
		
		firstPoint = move(firstPoint, direction, 6);
		displays[2] = new SegmentDisplay(firstPoint, direction);
		
		firstPoint = move(firstPoint, direction, 4);
		displays[3] = new SegmentDisplay(firstPoint, direction);
	}
	
	/**
	 * Generates the scoreboard
	 * 
	 * @param characters An array with the length of 4; filled with characters 0 - 9
	 */
	public void generate(char... characters) {
		if (characters.length < 4) {
			throw new IllegalArgumentException("characters length less than 4");
		}
		
		generateBlankBoard();
		generateColons();
		
		for (int i = 0; i < 4; i++) {
			char character = characters[i];
			
			String segmentCode = charCodes.get(character);
			
			for (char segment : SegmentDisplay.SEGMENTS) {
				String segmentAsString = String.valueOf(segment);
				
				if (segmentCode.contains(segmentAsString)) {
					displays[i].setSegment(segment, true);
				}
			}
		}
	}
	
	private void generateColons() {
		for (Location colonPart : colon) {
			colonPart.getBlock().setType(SegmentDisplay.fontData.getMaterial());
			colonPart.getBlock().setData(SegmentDisplay.fontData.getData());
		}
	}
	
	private void generateBlankBoard(SimpleBlockData data) {
		int minX, maxX, minY, maxY, minZ, maxZ;
		
		minX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());
		maxX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
		
		minY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());
		maxY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
		
		minZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		maxZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		
		Block current;
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					current = firstPoint.getWorld().getBlockAt(x, y, z);
					
					current.setType(data.getMaterial());
					current.setData(data.getData());
				}
			}
		}
	}
	
	private void generateBlankBoard() {
		generateBlankBoard(SegmentDisplay.baseData);
	}
	
	public void remove() {
		generateBlankBoard(new SimpleBlockData(0, (byte)0));
	}
	
	@Override
	public boolean contains(Location location) {
		int x, y, z;
		int minX, maxX, minY, maxY, minZ, maxZ;
		
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
		
		minX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());
		maxX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
		
		minY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());
		maxY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
		
		minZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		maxZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		
		return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ; 
	}
	
	/**
	 * Moves a location
	 * 
	 * @param base Base location
	 * @param directions Directions; even indexes has to be a blockface, uneven indexes a Integer
	 * @return The moved location
	 */
	private static Location move(Location base, Object... directions) {
		for (int i = 0; i + 1 < directions.length; i += 2) {
			BlockFace face = (BlockFace) directions[i];
			Integer length = (Integer) directions[i + 1];
			
			for (int a = 0; a < length; a++) {
				base = base.getBlock().getRelative(face).getLocation();
			}
		}
		
		return base;
	}
	
	@Override
	public ConfigurationSection serialize() {
		MemorySection section = new MemoryConfiguration();
		
		section.set("id", id);
		section.set("first", Parser.convertLocationtoString(firstPoint));
		section.set("second", Parser.convertLocationtoString(secondPoint));
		section.set("direction", direction.name());
		
		return section;
	}
	
	public static ScoreBoard deserialize(ConfigurationSection section) {
		int id = section.getInt("id");
		Location first = Parser.convertStringtoLocation(section.getString("first"));
		BlockFace direction = BlockFace.valueOf(section.getString("direction"));
		
		ScoreBoard board = new ScoreBoard(id, first, direction);
		
		return board;
	}
	
	public static class SegmentDisplay {
		
		private static final char[] SEGMENTS = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g'};
		
		private static SimpleBlockData fontData = new SimpleBlockData(Material.WOOL, (byte)14);
		private static SimpleBlockData baseData = new SimpleBlockData(Material.WOOL, (byte)15);
		private static boolean dataInitialized = false;
		
		private Location firstPointLocation;
		
		private BlockFace direction;
		
		static {
			if (!dataInitialized) {
				dataInitialized = true;
				
				try {
					String[] fontParts = HeavySpleef.getSystemConfig().getString("scoreboards.fontID").split(":");
					String[] baseParts = HeavySpleef.getSystemConfig().getString("scoreboards.baseID").split(":");
					
					byte data = 0;
					
					if (fontParts.length > 1) {
						data = Byte.parseByte(fontParts[1]);
					}
					
					fontData = new SimpleBlockData(Integer.parseInt(fontParts[0]), data);
					
					data = 0;
					
					if (baseParts.length > 1) {
						data = Byte.parseByte(baseParts[1]);
					}
					
					baseData = new SimpleBlockData(Integer.parseInt(baseParts[0]), data);
				} catch (Exception e) {
					Logger.warning("Could not read scoreboard id and data. Changing to default!");
				}
			}
		}
		
		public SegmentDisplay(Location firstPointLocation, BlockFace direction) {
			this.direction = direction;
			this.firstPointLocation = firstPointLocation;
			
			
		}
		
		public void setSegment(char segment, boolean state) {
			if (segment < 'a') {
				throw new IllegalArgumentException("segment " + segment + " is less than a");
			}
			
			if (segment > 'g') {
				throw new IllegalArgumentException("segment " + segment + " is greater than g");
			}
			
			switch(segment) {
			
			case 'a':
				setA(state);
				break;
			case 'b':
				setB(state);
				break;
			case 'c': 
				setC(state);
				break;
			case 'd':
				setD(state);
				break;
			case 'e':
				setE(state);
				break;
			case 'f':
				setF(state);
				break;
			case 'g':
				setG(state);
				break;
			default:
				break;
			
			}
		}
		
		private void setA(boolean state) {
			Block currentBlock = firstPointLocation.getBlock();
			
			for (int i = 0; i < 3; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(baseData.getMaterial());
					currentBlock.setData(baseData.getData());
				}
				
				currentBlock = currentBlock.getRelative(direction);
			}
		}
		
		private void setB(boolean state) {
			Block currentBlock = firstPointLocation.getBlock();
			
			//Offset
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(direction);
			}
			
			for (int i = 0; i < 3; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(baseData.getMaterial());
					currentBlock.setData(baseData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setC(boolean state) {
			Block currentBlock = firstPointLocation.getBlock();
			
			//Offset
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(direction);
			}
			
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 3; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(baseData.getMaterial());
					currentBlock.setData(baseData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setD(boolean state) {
			Block currentBlock = firstPointLocation.getBlock();
			
			//Offset
			for (int i = 0; i < 4; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 3; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(baseData.getMaterial());
					currentBlock.setData(baseData.getData());
				}
				
				currentBlock = currentBlock.getRelative(direction);
			}
		}
		
		private void setE(boolean state) {
			Block currentBlock = firstPointLocation.getBlock();
			
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 3; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(baseData.getMaterial());
					currentBlock.setData(baseData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setF(boolean state) {
			Block currentBlock = firstPointLocation.getBlock();
			
			for (int i = 0; i < 3; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(baseData.getMaterial());
					currentBlock.setData(baseData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setG(boolean state) {
			Block currentBlock = firstPointLocation.getBlock();
			
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 3; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(baseData.getMaterial());
					currentBlock.setData(baseData.getData());
				}
				
				currentBlock = currentBlock.getRelative(direction);
			}
		}
		
	}

}
