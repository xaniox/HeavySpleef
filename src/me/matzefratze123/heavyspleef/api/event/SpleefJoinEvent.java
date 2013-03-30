package me.matzefratze123.heavyspleef.api.event;

import me.matzefratze123.heavyspleef.api.GameData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpleefJoinEvent extends Event {

	public static HandlerList handlers = new HandlerList();
	private GameData gameData = null;
	private Player player = null;
	
	public SpleefJoinEvent(GameData game, Player player) {
		this.player = player;
		this.gameData = game;
	}
	
	/**
	 * Gets the game that is involved in this event
	 * 
	 * @return The gamedata of the game
	 */
	public GameData getGame() {
		return this.gameData;
	}
	
	/**
	 * Gets the player that joined the game
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public HandlerList getHandlerList() {
		return handlers;
	}

	

}
