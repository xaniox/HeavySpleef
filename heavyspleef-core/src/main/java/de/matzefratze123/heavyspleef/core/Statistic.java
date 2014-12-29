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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "statistics")
public class Statistic implements Comparable<Statistic> {

	public static final String UUID_ATTRIBUTE = "uuid";
	public static final String RATING_ATTRIBUTE = "rating";
	
	@Column(name = "uuid")
	private UUID uniqueIdentifier;
	@Column(name = "wins")
	private int wins;
	@Column(name = "losses")
	private int losses;
	@Column(name = "knockouts")
	private int knockouts;
	@Column(name = "games_played")
	private int gamesPlayed;
	@Column(name = "time_played")
	private long timePlayed;
	@Column(name = "rating")
	private int rating;
	
	public Statistic(UUID uuid) {
		this.uniqueIdentifier = uuid;
	}
	
	public UUID getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	
	public int getWins() {
		return wins;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public int getKnockouts() {
		return knockouts;
	}
	
	public int getGamesPlayed() {
		return gamesPlayed;
	}
	
	public long getTimePlayed() {
		return timePlayed;
	}
	
	public int getRating() {
		return rating;
	}
	
	@Override
	public int compareTo(Statistic o) {
		return Integer.valueOf(rating).compareTo(o.rating);
	}
	
}
