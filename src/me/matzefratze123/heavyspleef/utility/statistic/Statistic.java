package me.matzefratze123.heavyspleef.utility.statistic;

/**
 * This class stores information about spleef statistics of a player
 * 
 * @author matzefratze123
 */
public class Statistic {

	/**
	 * The owner of the statistic
	 */
	private String name;
	
	/**
	 * How often the player lost
	 */
	private int loses;
	
	/**
	 * How often the player won
	 */
	private int wins;
	
	/**
	 * How often the player has knocked out another one
	 */
	private int knockouts;
	
	/**
	 * How many games the player played
	 */
	private int gamesPlayed;
	
	/**
	 * Creates a empty statistic with the given name
	 */
	public Statistic(String name) {
		this.name = name;
		this.loses = 0;
		this.wins = 0;
		this.knockouts = 0;
		this.gamesPlayed = 0;
	}
	
	/**
	 * Creates a empty statistic based on the parameters
	 */
	public Statistic(String name, int loses, int wins, int knockouts, int gamesPlayed) {
		this.name = name;
		this.loses = loses;
		this.wins = wins;
		this.knockouts = knockouts;
		this.gamesPlayed = gamesPlayed;
	}
	
	/**
	 * Get's the wins of the player
	 * @return The wins
	 */
	public int getWins() {
		return this.wins;
	}
	
	/**
	 * Get's the loses of the player
	 * @return The loses
	 */
	public int getLoses() {
		return this.loses;
	}
	
	/**
	 * Get's the knockouts of the player
	 * @return The knockouts
	 */
	public int getKnockouts() {
		return this.knockouts;
	}
	
	/**
	 * Get's the count of played games
	 * @return The played games
	 */
	public int getGamesPlayed() {
		return this.gamesPlayed;
	}
	
	/**
	 * Get's the owner of this statistic
	 * @return Name of the owner
	 */
	public String getName() {
		return this.name;
	}
}
