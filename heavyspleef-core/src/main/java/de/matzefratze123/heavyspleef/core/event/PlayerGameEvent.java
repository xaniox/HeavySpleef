package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerGameEvent extends GameEvent {

	private SpleefPlayer player;
	
	public PlayerGameEvent(Game game, SpleefPlayer player) {
		super(game);
		
		this.player = player;
	}
	
	public SpleefPlayer getPlayer() {
		return player;
	}

}
