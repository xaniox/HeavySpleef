package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;

public class GameRenameEvent extends GameEvent {

	private String newName;
	
	public GameRenameEvent(Game game, String newName) {
		super(game);
		
		this.newName = newName;
	}
	
	public String getNewName() {
		return newName;
	}

}