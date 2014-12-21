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
