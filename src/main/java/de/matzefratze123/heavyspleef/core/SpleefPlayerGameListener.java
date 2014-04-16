package de.matzefratze123.heavyspleef.core;

import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public interface SpleefPlayerGameListener {

	public void playerJoin(SpleefPlayer player);

	public void playerLeave(SpleefPlayer player);

	public void playerKnockout(SpleefPlayer player);

	public void playerWin(SpleefPlayer player);

	public enum EventType {

		PLAYER_JOIN, PLAYER_LEAVE, PLAYER_KNOCKOUT, PLAYER_WIN;

	}

}
