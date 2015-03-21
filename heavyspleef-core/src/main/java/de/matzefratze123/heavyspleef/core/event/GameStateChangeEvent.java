package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;

public class GameStateChangeEvent extends GameEvent {

	private GameState oldState;
	
	public GameStateChangeEvent(Game game, GameState oldState) {
		super(game);
	}
	
	public GameState getOldState() {
		return oldState;
	}
	
	public GameState getNewState() {
		return getGame().getGameState();
	}

}
