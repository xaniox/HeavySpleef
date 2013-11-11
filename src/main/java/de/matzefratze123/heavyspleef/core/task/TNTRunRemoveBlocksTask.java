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
package de.matzefratze123.heavyspleef.core.task;

import org.bukkit.Material;
import org.bukkit.block.Block;

import de.matzefratze123.heavyspleef.core.queue.ArrayQueue;
import de.matzefratze123.heavyspleef.core.queue.Queue;

public class TNTRunRemoveBlocksTask implements Runnable {
	
	private final Queue<Block> queuedBlocks;
	
	protected TNTRunRemoveBlocksTask() {
		queuedBlocks = new ArrayQueue<Block>();
	}
	
	public void queue(Block block) {
		queuedBlocks.add(block);
	}
	
	public boolean isQueued(Block block) {
		return queuedBlocks.contains(block);
	}

	@Override
	public void run() {
		while (!queuedBlocks.isEmpty()) {
			Block block = queuedBlocks.remove();
			
			block.setType(Material.AIR);
		}
	}

}
