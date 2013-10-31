package de.matzefratze123.heavyspleef.api.event;


import org.bukkit.event.Event;

import de.matzefratze123.heavyspleef.api.GameData;

public abstract class SpleefEvent extends Event {

	private GameData data;
	
	protected SpleefEvent(GameData data) {
		this.data = data;
	}
	
	/**
	 * Gets the game that is involved in this event
	 * 
	 * @return The gamedata of the game
	 */
	public GameData getGame() {
		return data;
	}

}
