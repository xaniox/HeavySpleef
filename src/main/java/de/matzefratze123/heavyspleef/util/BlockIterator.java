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
import java.util.List;

import org.bukkit.block.Block;

import com.google.common.collect.ImmutableList;

public class BlockIterator implements Iterator<Block>, Iterable<Block> {

	public static final int	FORWARD		= 0;
	public static final int	BACKWARD	= 1;

	private int				currentIndex;
	private int				direction;

	private List<Block>		blockList;

	public BlockIterator(Block[] blocks) {
		direction = FORWARD;

		this.blockList = ImmutableList.copyOf(blocks);
	}

	public BlockIterator(List<Block> blocks) {
		direction = FORWARD;

		this.blockList = ImmutableList.copyOf(blocks);
	}

	public void setDirection(int direction) {
		this.direction = direction;

		if (direction == FORWARD) {
			currentIndex = 0;
		} else if (direction == BACKWARD) {
			currentIndex = blockList.size() - 1;
		}
	}

	public int getDirection() {
		return direction;
	}

	@Override
	public boolean hasNext() {
		return direction == FORWARD ? currentIndex + 1 <= blockList.size() : (direction == BACKWARD ? currentIndex >= 0 : false);
	}

	@Override
	public Block next() {
		Block block = blockList.get(currentIndex);

		if (direction == FORWARD) {
			currentIndex++;
		} else if (direction == BACKWARD) {
			currentIndex--;
		}

		return block;
	}

	@Override
	public void remove() {
		blockList.remove(currentIndex);
	}

	@Override
	public Iterator<Block> iterator() {
		return this;
	}

}
