package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerLeaveQueueEvent extends PlayerGameEvent {

	public PlayerLeaveQueueEvent(Game game, SpleefPlayer player) {
		super(game, player);
	}

}
