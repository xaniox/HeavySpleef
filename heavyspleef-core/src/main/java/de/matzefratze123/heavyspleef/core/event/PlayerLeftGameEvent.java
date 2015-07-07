package de.matzefratze123.heavyspleef.core.event;

import lombok.Getter;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.QuitCause;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

@Getter
public class PlayerLeftGameEvent extends PlayerGameEvent {

	private QuitCause cause;
	private SpleefPlayer killer;
	
	public PlayerLeftGameEvent(Game game, SpleefPlayer player, SpleefPlayer killer, QuitCause cause) {
		super(game, player);
		
		this.killer = killer;
		this.cause = cause;
	}
	
}
