package de.matzefratze123.heavyspleef.core;

public enum GameState {
	
	DISABLED(false, false),
	WAITING(false, true),
	LOBBY(false, true),
	STARTING(true, true),
	INGAME(true, true);
	
	private boolean gameActive;
	private boolean gameEnabled;
	
	private GameState(boolean gameActive, boolean gameEnabled) {
		this.gameActive = gameActive;
		this.gameEnabled = gameEnabled;
	}
	
	public boolean isGameActive() {
		return gameActive;
	}
	
	public boolean isGameEnabled() {
		return gameEnabled;
	}
	
}
