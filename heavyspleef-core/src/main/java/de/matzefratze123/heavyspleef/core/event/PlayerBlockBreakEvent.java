package de.matzefratze123.heavyspleef.core.event;

import org.bukkit.block.Block;

import de.matzefratze123.heavyspleef.core.Game;

public class PlayerBlockBreakEvent extends GameEvent implements Cancellable {

	private boolean cancel;
	private Block block;
	
	public PlayerBlockBreakEvent(Game game, Block block) {
		super(game);
		
		this.block = block;
	}
	
	public Block getBlock() {
		return block;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

}
