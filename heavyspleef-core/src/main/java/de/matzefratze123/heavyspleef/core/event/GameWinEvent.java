package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class GameWinEvent extends GameEvent {

	private SpleefPlayer[] winners;
	
	public GameWinEvent(Game game, SpleefPlayer[] winners) {
		super(game);
		
		this.winners = winners;
	}
	
	public SpleefPlayer[] getWinners() {
		return winners;
	}

}
