package me.matzefratze123.heavyspleef.api.event;

import me.matzefratze123.heavyspleef.api.GameData;
import me.matzefratze123.heavyspleef.core.LoseCause;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpleefLoseEvent extends Event {

	public static HandlerList handlers = new HandlerList();
	
	private Player loser = null;
	private GameData gameData = null;
	private LoseCause cause = null;
	
	public SpleefLoseEvent(GameData game, Player player, LoseCause cause) {
		this.loser = player;
		this.gameData = game;
		this.cause = cause;
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
	 * The cause of the lose
	 * 
	 * @see me.matzefratze123.heavyspleef.core.LoseCause
	 * @return A type of the enum LoseCause
	 */
	public LoseCause getCause() {
		return this.cause;
	}
	
	/**
	 * Gets the player that lost
	 */
	public Player getLoser() {
		return this.loser;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public HandlerList getHandlerList() {
		return handlers;
	}

	

}
