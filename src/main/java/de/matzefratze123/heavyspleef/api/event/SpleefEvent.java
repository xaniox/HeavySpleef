package de.matzefratze123.heavyspleef.api.event;

import org.bukkit.event.Event;

import de.matzefratze123.heavyspleef.api.IGame;

public abstract class SpleefEvent extends Event {

	private IGame data;
	
	protected SpleefEvent(IGame game) {
		this.data = game;
	}
	
	/**
	 * Gets the game that is involved in this event
	 * 
	 * @return The gamedata of the game
	 */
	public IGame getGame() {
		return data;
	}

}
