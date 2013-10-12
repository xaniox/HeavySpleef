package de.matzefratze123.heavyspleef.core;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.util.SimpleBlockData;

public class ScoreBoardNew {
	
	private class SegmentDisplay {
		
		private SimpleBlockData fontData;
		private SimpleBlockData coverData;
		
		private Location cornerLocation;
		
		private static final int WIDTH = 3;
		private static final int HEIGHT = 5;
		
		private int direction;
		
		public SegmentDisplay(Location cornerLocation, int direction) {
			this.direction = direction;
			this.cornerLocation = cornerLocation;
		}
		
		public void setSegment(char segment, boolean state) {
			if (segment < 'a')
				throw new IllegalArgumentException("segment " + segment + " is less than a");
			
			if (segment > 'g')
				throw new IllegalArgumentException("segment " + segment + " is greater than g");
			
			SimpleBlockData data;
			
			if (state)
				data = fontData;
			else
				data = coverData;
			
			switch(segment) {
			
			case 'a':
				
				
				break;
			case 'b':
				break;
			case 'c': 
				break;
			case 'd':
				break;
			case 'e':
				break;
			case 'f':
				break;
			case 'g':
				break;
			default:
				break;
			
			}
		}
		
	}
	
}
