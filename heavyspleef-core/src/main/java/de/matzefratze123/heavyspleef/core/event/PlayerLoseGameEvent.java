package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerLoseGameEvent extends PlayerGameEvent {

	public PlayerLoseGameEvent(Game game, SpleefPlayer player) {
		super(game, player);
	}

}
