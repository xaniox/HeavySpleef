/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	
	private Map<StatisticValue, Integer> scores;
	
	/**
	 * Creates a empty statistic with the given name
	 */
	public StatisticModule(String name) {
		scores = new HashMap<StatisticValue, Integer>();
		
		this.name = name;
	}
	
	/**
	 * Creates a empty statistic based on the parameters
	 */
	public StatisticModule(String name, int loses, int wins, int knockouts, int gamesPlayed) {
		scores = new HashMap<StatisticValue, Integer>();
		
		this.name = name;
		
		scores.put(StatisticValue.LOSE, loses);
		scores.put(StatisticValue.WIN, wins);
		scores.put(StatisticValue.KNOCKOUTS, knockouts);
		scores.put(StatisticValue.GAMES_PLAYED, gamesPlayed);
		//this.elo = elo;
	}
	
	public void putScore(StatisticValue value, int score) {
		if (value == StatisticValue.SCORE) {
			throw new IllegalArgumentException("value cannot be StatisticValue.SCORE");
		}
		
		if (score != 0) {
			scores.put(value, score);
		} else {
			scores.remove(value);
		}
		
		updateScore();
	}
	
	public void addScores(Map<StatisticValue, Integer> scores) {
		for (Entry<StatisticValue, Integer> entry : scores.entrySet()) {
			int score = 0;
			
			if (entry.getValue() != null) {
				score = entry.getValue();
			}
			
			scores.put(entry.getKey(), score);
		}
	}
	
	public int getScore(StatisticValue value) {
		Integer s = scores.get(value);
		int score = 0;
		
		if (s != null) {
			score = s;
		}
		
		return score;
	}
	
	public Map<StatisticValue, Integer> getScores() {
		return scores;
	}
	
	private void updateScore() {
		SettingsSectionLeaderBoard section = HeavySpleef.getSystemConfig().getLeaderboardSection();
		
		int pointsWin = section.getWinPoints();
		int pointsLose = section.getLosePoints();
		int pointsKnockout = section.getKnockoutPoints();
		int pointsGamePlayed = section.getGamePlayedPoints();
		
		int score = (pointsWin * getScore(StatisticValue.WIN)) +
				    (pointsLose * getScore(StatisticValue.LOSE)) + 
				    (pointsKnockout * getScore(StatisticValue.KNOCKOUTS)) + 
				    (pointsGamePlayed * getScore(StatisticValue.GAMES_PLAYED));
		
		scores.put(StatisticValue.SCORE, score);
		
		for (Entry<StatisticValue, Integer> e : scores.entrySet()) {
			System.out.println(e.getKey().name() + ": " + e.getValue());
		}
	}
	
	/**
	 * Gets the KD of the statistic
	 */
	public double getKD() {
		double winsPerGame = 0.0;
		
		if (getScore(StatisticValue.GAMES_PLAYED) > 0) {
			winsPerGame = getScore(StatisticValue.WIN) / (double)getScore(StatisticValue.GAMES_PLAYED);
		}
		
		return winsPerGame;
	}
	
	/**
	 * Get's the owner of this statistic
	 * @return Name of the owner
	 */
	public String getHolder() {
		return this.name;
	}
	
	/**
	 * Add's a win
	 */
	public void addWin() {
		putScore(StatisticValue.WIN, getScore(StatisticValue.WIN) + 1);
	}
	
	/**
	 * Add's a lose
	 */
	public void addLose() {
		putScore(StatisticValue.LOSE, getScore(StatisticValue.LOSE) + 1);
	}
	
	/**
	 * Add's a knockout
	 */
	public void addKnockout() {
		putScore(StatisticValue.KNOCKOUTS, getScore(StatisticValue.KNOCKOUTS) + 1);
	}
	
	/**
	 * Add's a game
	 */
	public void addGame() {
		putScore(StatisticValue.GAMES_PLAYED, getScore(StatisticValue.GAMES_PLAYED) + 1);
	}
	
	public void merge(StatisticModule other) {
		for (Entry<StatisticValue, Integer> entry : other.scores.entrySet()) {
			scores.put(entry.getKey(), (scores.get(entry.getKey()) == null ? 0 : scores.get(entry.getKey())) + entry.getValue());
		}
		
		updateScore();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof StatisticModule)) {
			return false;
		}
		
		StatisticModule module = (StatisticModule) o;
		if (!module.getHolder().equalsIgnoreCase(name)) {
			return false;
		}
		
		return true;
	}

	@Override
	public int compareTo(StatisticModule o) {
		return Integer.valueOf(getScore(StatisticValue.SCORE)).compareTo(o.getScore(StatisticValue.SCORE));
	}
	
	public enum StatisticValue {
		
		WIN("wins"),
		LOSE("loses"),
		KNOCKOUTS("knockouts"),
		GAMES_PLAYED("games"),
		SCORE("score");
		
		private String columnName;
		
		private StatisticValue(String columnName) {
			this.columnName = columnName;
		}
		
		public String getColumnName() {
			return columnName;
		}
		
	}
	
}
