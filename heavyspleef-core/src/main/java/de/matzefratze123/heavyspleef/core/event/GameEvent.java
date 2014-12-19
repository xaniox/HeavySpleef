package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;

public class GameEvent {

	private Game game;
	
	public GameEvent(Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}

}
