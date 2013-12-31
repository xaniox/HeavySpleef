/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.stats;

import org.bukkit.Bukkit;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionLeaderBoard;


/**
 * This class stores information about spleef statistics of a player
 * 
 * @author matzefratze123
 */
public class StatisticModule implements Comparable<StatisticModule> {
	
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
	 * The elo of this player
	 */
	private int elo;
	
	/**
	 * Creates a empty statistic with the given name
	 */
	public StatisticModule(String name) {
		this.name = name;
		this.loses = 0;
		this.wins = 0;
		this.knockouts = 0;
		this.gamesPlayed = 0;
		this.elo = 1000;
	}
	
	/**
	 * Creates a empty statistic based on the parameters
	 */
	public StatisticModule(String name, int loses, int wins, int knockouts, int gamesPlayed) {
		this.name = name;
		this.loses = loses;
		this.wins = wins;
		this.knockouts = knockouts;
		this.gamesPlayed = gamesPlayed;
		//this.elo = elo;
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
	 * Get's the score of the player </br>
	 * <b>formula: 1000 + wins * games - (loses * games / 3)<b>
	 */
	public int getScore() {
		SettingsSectionLeaderBoard section = HeavySpleef.getSystemConfig().getLeaderboardSection();
		
		int pointsWin = section.getWinPoints();
		int pointsLose = section.getLosePoints();
		int pointsKnockout = section.getKnockoutPoints();
		int pointsGamePlayed = section.getGamePlayedPoints();
		
		return (pointsWin * getWins()) + (pointsLose * getLoses()) + (pointsKnockout * getKnockouts()) + (pointsGamePlayed * getGamesPlayed());
	}
	
	/**
	 * Get's the elo of this player
	 * 
	 * @return The elo
	 */
	public int getElo() {
		return this.elo;
	}
	
	/**
	 * Gets the KD of the statistic
	 */
	public double getKD() {
		double winsPerGame = 0.0;
		if (getGamesPlayed() > 0)
			winsPerGame = (double)getWins() / (double)getGamesPlayed();
		
		return round(winsPerGame);
	}
	
	/**
	 * Get's the owner of this statistic
	 * @return Name of the owner
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Add's a win
	 */
	public void addWin() {
		wins++;
	}
	
	/**
	 * Add's a lose
	 */
	public void addLose() {
		loses++;
	}
	
	/**
	 * Add's a knockout
	 */
	public void addKnockout() {
		knockouts++;
	}
	
	/**
	 * Add's a game
	 */
	public void addGame() {
		gamesPlayed++;
	}
	
	/**
	 * Sets the elo of this player
	 */
	public void setElo(int elo) {
		this.elo = elo;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof StatisticModule)) {
			return false;
		}
		
		StatisticModule module = (StatisticModule) o;
		if (!module.getName().equalsIgnoreCase(name)) {
			return false;
		}
		
		return true;
	}

	@Override
	public int compareTo(StatisticModule o) {
		if (o.getScore() > getScore())
			return 1;
		if (o.getScore() < getScore())
			return -1;
		if (o.getScore() == getScore())
			return 0;
		
		return 0;
	}
	
	private double round(double d) {
		d *= 10000;
		d = Math.round(d);
		return d / 10000.0D;
	}
	
	public static void pushAsync() {
		Bukkit.getScheduler().runTaskAsynchronously(HeavySpleef.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				HeavySpleef.getInstance().getStatisticDatabase().saveAccounts();
			}
		});
	}
	
}
