package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerJoinGameEvent extends PlayerGameEvent {

	private boolean startGame;
	
	public PlayerJoinGameEvent(Game game, SpleefPlayer player) {
		super(game, player);
	}
	
	public boolean getStartGame() {
		return startGame;
	}
	
	public void setStartGame(boolean startGame) {
		this.startGame = startGame;
	}

}
