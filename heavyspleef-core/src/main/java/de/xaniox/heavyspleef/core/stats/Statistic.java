/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core.stats;

import de.xaniox.heavyspleef.core.game.Rateable;
import de.xaniox.heavyspleef.core.script.Variable;
import de.xaniox.heavyspleef.core.script.VariableSuppliable;

import java.util.Set;
import java.util.UUID;

public class Statistic implements Comparable<Statistic>, Rateable, VariableSuppliable {

	private static final double START_RATING = 1000D;
	
	private UUID uniqueIdentifier;
	private String lastName;
	private int wins;
	private int losses;
	private int knockouts;
	private int gamesPlayed;
	private long timePlayed;
	private int blocksBroken;
	private double rating = START_RATING;
	
	public Statistic() {}
	
	public Statistic(UUID uuid) {
		this.uniqueIdentifier = uuid;
	}
	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	public int getBlocksBroken() {
		return blocksBroken;
	}

	public void setBlocksBroken(int blocksBroken) {
		this.blocksBroken = blocksBroken;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public UUID getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	@Override
	public int compareTo(Statistic o) {
		return Double.valueOf(rating).compareTo(o.rating);
	}

	public boolean isEmpty() {
		return wins == 0 && losses == 0 && knockouts == 0 && gamesPlayed == 0 && timePlayed == 0 && blocksBroken == 0 && rating == START_RATING;
	}

	@Override
	public void supply(Set<Variable> vars, Set<String> requested) {
		vars.add(new Variable("last-name", lastName));
		vars.add(new Variable("wins", wins));
		vars.add(new Variable("losses", losses));
		vars.add(new Variable("knockouts", knockouts));
		vars.add(new Variable("games-played", gamesPlayed));
		vars.add(new Variable("time-played", timePlayed));
		vars.add(new Variable("blocks-broken", blocksBroken));
		vars.add(new Variable("rating", (int)rating));
		vars.add(new Variable("rating-exact", rating));
	}
	
}