/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core;

import java.util.UUID;

public class Statistic implements Comparable<Statistic> {

	public static final String UUID_ATTRIBUTE = "uuid";
	public static final String RATING_ATTRIBUTE = "rating";
	
	private UUID uniqueIdentifier;
	private int wins;
	private int losses;
	private int knockouts;
	private int gamesPlayed;
	private long timePlayed;
	private double rating;
	
	public Statistic() {}
	
	public Statistic(UUID uuid) {
		this.uniqueIdentifier = uuid;
	}
	
	public UUID getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	
	public int getWins() {
		return wins;
	}
	
	public void setWins(int wins) {
		this.wins = wins;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public void setLosses(int losses) {
		this.losses = losses;
	}
	
	public int getKnockouts() {
		return knockouts;
	}
	
	public void setKnockouts(int knockouts) {
		this.knockouts = knockouts;
	}
	
	public int getGamesPlayed() {
		return gamesPlayed;
	}
	
	public void setGamesPlayed(int gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}
	
	public long getTimePlayed() {
		return timePlayed;
	}
	
	public void setTimePlayed(long timePlayed) {
		this.timePlayed = timePlayed;
	}
	
	public double getRating() {
		return rating;
	}
	
	public void setRating(double rating) {
		this.rating = rating;
	}
	
	@Override
	public int compareTo(Statistic o) {
		return Double.valueOf(rating).compareTo(o.rating);
	}
	
}
