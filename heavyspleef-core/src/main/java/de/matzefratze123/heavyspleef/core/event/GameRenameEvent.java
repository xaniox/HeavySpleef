package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;

public class GameRenameEvent extends GameEvent {

	private String old;
	
	public GameRenameEvent(Game game, String old) {
		super(game);
		
		this.old = old;
	}
	
	public String getOld() {
		return old;
	}

}