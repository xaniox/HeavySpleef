package me.matzefratze123.heavyspleef.api.event;

import me.matzefratze123.heavyspleef.api.GameData;

import org.bukkit.event.Event;

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
