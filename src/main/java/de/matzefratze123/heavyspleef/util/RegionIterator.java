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
package de.matzefratze123.heavyspleef.util;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class RegionIterator implements Iterator<Block>, Iterable<Block> {

	private Location min;
	private Location max;
	
	private int currentX;
	private int currentY;
	private int currentZ;
	
	public RegionIterator(Location first, Location second) {
		this.min = Util.getMin(first, second);
		this.max = Util.getMax(first, second);
		
		currentX = min.getBlockX();
		currentY = min.getBlockY();
		currentZ = min.getBlockZ();
	}
	
	public RegionIterator(Block first, Block second) {
		this(first.getLocation(), second.getLocation());
	}

	@Override
	public Iterator<Block> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return currentZ <= max.getBlockZ() && currentY <= max.getBlockY() && currentX <= max.getBlockX();
	}

	@Override
	public Block next() {
		if (!hasNext()) {
			return null;
		}
		
		Block block = min.getWorld().getBlockAt(currentX, currentY, currentZ);
		
		currentZ++;
		
		if (currentZ > max.getBlockZ()) {
			++currentY;
			
			currentZ = min.getBlockZ();
		}
		
		if (currentY > max.getBlockY()) {
			++currentX;
			
			currentZ = min.getBlockZ();
			currentY = min.getBlockY();
		}
		
		return block;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Operation unsupported");
	}
	
}
