package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerLeaveGameEvent extends PlayerGameEvent implements Cancellable {

	private boolean cancel;
	
	public PlayerLeaveGameEvent(Game game, SpleefPlayer player) {
		super(game, player);
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
