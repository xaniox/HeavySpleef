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
package de.matzefratze123.heavyspleef.core.game;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.core.stats.Statistic;

public class DefaultRatingCompute implements RatingCompute {
	
	@Override
	public RatingResult compute(Map<String, Statistic> statistics, Game game, SpleefPlayer[] winnersArray) {
		List<SpleefPlayer> dead = game.getDeadPlayers();
		
		Map<String, Double> ratings = Maps.newHashMap();
		Map<String, Double> results = Maps.newHashMap();
		
		for (Entry<String, Statistic> entry : statistics.entrySet()) {
			String name = entry.getKey();
			Statistic statistic = entry.getValue();
			
			double r = statistic.getRating();
			ratings.put(name, r);
		}		
		
		for (Entry<String, Double> entry : ratings.entrySet()) {
			String name = entry.getKey();
			double rating = entry.getValue();
			
			double expectation = e(ratings, name);
			double score = s(getPlace(name, dead), ratings.size());
			
			double newRating = rating + K * (score - expectation);
			results.put(name, newRating);
		}
		
		return new RatingResult(results);
	}
	
	private int getPlace(String name, List<SpleefPlayer> dead) {
		for (int i = 0; i < dead.size(); i++) {
			SpleefPlayer deadPlayer = dead.get(i);
			
			if (deadPlayer.getName().equalsIgnoreCase(name)) {
				return dead.size() - i;
			}
		}
		
		throw new IllegalArgumentException("Dead players list does not contain a player with the name " + name);
	}
	
	private double e(Map<String, Double> ratings, String name) {
		double sumE = 0;
		for (Entry<String, Double> entry : ratings.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(name)) {
				continue;
			}
			
			double dif = ratings.get(entry.getKey()) - ratings.get(name);
			if (dif > D) {
				dif = D;
			} else if (dif < -D) {
				dif = -D;
			}
			
			sumE += 1D / (1 + Math.pow(10, dif / D));
		}
		
		final int size = ratings.size();
		return sumE / ((size * (size - 1)) / 2);
	}
	
	private double s(int place, int numPlayers) {
		return (double) (numPlayers - place) / ((numPlayers * (numPlayers - 1)) / 2);
	}

}
