package de.matzefratze123.heavyspleef.core.task;

import org.bukkit.Effect;
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
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4);
		}
	}

}
