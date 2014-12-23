package de.matzefratze123.heavyspleef.core.event;

import org.bukkit.block.Block;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerBlockBreakEvent extends PlayerGameEvent implements Cancellable {

	private boolean cancel;
	private Block block;
	
	public PlayerBlockBreakEvent(Game game, SpleefPlayer player, Block block) {
		super(game, player);
		
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
