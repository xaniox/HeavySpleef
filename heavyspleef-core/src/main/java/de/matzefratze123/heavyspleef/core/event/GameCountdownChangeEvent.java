package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.CountdownTask;
import de.matzefratze123.heavyspleef.core.Game;

public class GameCountdownChangeEvent extends GameEvent {

	private CountdownTask countdown;
	
	public GameCountdownChangeEvent(Game game, CountdownTask countdown) {
		super(game);
		
		this.countdown = countdown;
	}
	
	public CountdownTask getCountdown() {
		return countdown;
	}
	
}
