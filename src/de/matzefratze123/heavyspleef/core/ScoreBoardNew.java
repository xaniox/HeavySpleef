package de.matzefratze123.heavyspleef.core;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import de.matzefratze123.heavyspleef.util.SimpleBlockData;

public class ScoreBoardNew {
	
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
	
	
	
	private class SegmentDisplay {
		
		private SimpleBlockData fontData;
		private SimpleBlockData coverData;
		
		private Location cornerLocation;
		
		private static final int WIDTH = 3;
		private static final int HEIGHT = 5;
		
		private BlockFace direction;
		
		public SegmentDisplay(Location cornerLocation, BlockFace direction) {
			this.direction = direction;
			this.cornerLocation = cornerLocation;
		}
		
		public void setSegment(char segment, boolean state) {
			if (segment < 'a')
				throw new IllegalArgumentException("segment " + segment + " is less than a");
			
			if (segment > 'g')
				throw new IllegalArgumentException("segment " + segment + " is greater than g");
			
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
			Block currentBlock = cornerLocation.getBlock();
			
			for (int i = 0; i < 2; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(coverData.getMaterial());
					currentBlock.setData(coverData.getData());
				}
				
				currentBlock = currentBlock.getRelative(direction);
			}
		}
		
		private void setB(boolean state) {
			Block currentBlock = cornerLocation.getBlock();
			
			//Offset
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(direction);
			}
			
			for (int i = 0; i < 2; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(coverData.getMaterial());
					currentBlock.setData(coverData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setC(boolean state) {
			Block currentBlock = cornerLocation.getBlock();
			
			//Offset
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(direction);
			}
			
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 2; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(coverData.getMaterial());
					currentBlock.setData(coverData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setD(boolean state) {
			Block currentBlock = cornerLocation.getBlock();
			
			//Offset
			for (int i = 0; i < 4; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 2; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(coverData.getMaterial());
					currentBlock.setData(coverData.getData());
				}
				
				currentBlock = currentBlock.getRelative(direction);
			}
		}
		
		private void setE(boolean state) {
			Block currentBlock = cornerLocation.getBlock();
			
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 2; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(coverData.getMaterial());
					currentBlock.setData(coverData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setF(boolean state) {
			Block currentBlock = cornerLocation.getBlock();
			
			for (int i = 0; i < 2; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(coverData.getMaterial());
					currentBlock.setData(coverData.getData());
				}
				
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
		}
		
		private void setG(boolean state) {
			Block currentBlock = cornerLocation.getBlock();
			
			for (int i = 0; i < 2; i++) {
				currentBlock = currentBlock.getRelative(BlockFace.DOWN);
			}
			
			for (int i = 0; i < 2; i++) {
				if (state) {
					currentBlock.setType(fontData.getMaterial());
					currentBlock.setData(fontData.getData());
				} else {
					currentBlock.setType(coverData.getMaterial());
					currentBlock.setData(coverData.getData());
				}
				
				currentBlock = currentBlock.getRelative(direction);
			}
		}
		
	}
	
}
