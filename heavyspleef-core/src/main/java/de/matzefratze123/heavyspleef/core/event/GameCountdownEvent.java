package de.matzefratze123.heavyspleef.core.event;

import java.util.List;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.Game;

public class GameCountdownEvent extends GameEvent {
	
	private List<Location> spawnLocations;
	private int countdownLength;
	private boolean countdownEnabled;
	
	public GameCountdownEvent(Game game) {		
		super(game);
		
		countdownEnabled = true;
	}
	
	public void setSpawnLocations(List<Location> spawnLocations) {
		this.spawnLocations = spawnLocations;
	}
	
	public List<Location> getSpawnLocations() {
		return spawnLocations;
	}
	
	public void setCountdownLength(int countdownLength) {
		this.countdownLength = countdownLength;
	}
	
	public int getCountdownLength() {
		return countdownLength;
	}
	
	public void setCountdownEnabled(boolean countdownEnabled) {
		this.countdownEnabled = countdownEnabled;
	}
	
	public boolean isCountdownEnabled() {
		return countdownEnabled;
	}

}
