package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerWinGameEvent extends PlayerGameEvent {

	public PlayerWinGameEvent(Game game, SpleefPlayer player) {
		super(game, player);
	}

}
