package de.matzefratze123.heavyspleef.core.event;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerJoinGameEvent extends PlayerGameEvent {

	private Location teleportationLocation;
	private JoinResult joinResult;
	private boolean startGame; 
	
	public PlayerJoinGameEvent(Game game, SpleefPlayer player) {
		super(game, player);
		
		this.joinResult = JoinResult.NOT_SPECIFIED;
	}
	
	public JoinResult getJoinResult() {
		return joinResult;
	}
	
	public void setJoinResult(JoinResult joinResult) {
		this.joinResult = joinResult;
	}

	public void setTeleportationLocation(Location location) {
		this.teleportationLocation = location;
	}
	
	public Location getTeleportationLocation() {
		return teleportationLocation;
	}
	
	public void setStartGame(boolean startGame) {
		this.startGame = startGame;
	}
	
	public boolean isStartGame() {
		return startGame;
	}
	
	public enum JoinResult {
		
		NOT_SPECIFIED,
		ALLOW,
		DENY;
		
	}

}
