package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerQueueFlushEvent extends PlayerGameEvent {
	
	private FlushResult result;
	
	public PlayerQueueFlushEvent(Game game, SpleefPlayer player) {
		super(game, player);
	}
	
	public FlushResult getResult() {
		return result;
	}
	
	public void setResult(FlushResult result) {
		this.result = result;
	}
	
	public enum FlushResult {
		
		ALLOW,
		DENY;
		
	}

}
