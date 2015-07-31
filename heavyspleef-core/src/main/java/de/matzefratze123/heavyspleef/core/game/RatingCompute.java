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

import java.util.Map;

import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.core.stats.Statistic;

public interface RatingCompute {
	
	//Leave this as a constant for now
	public static final int K = 30;
	public static final int D = 400;
	
	public RatingResult compute(Map<String, Statistic> statistics, Game game, SpleefPlayer[] winners);
	
	public static class RatingResult {
		
		private Map<String, Double> newRating;
		
		public RatingResult(Map<String, Double> newRating) {
			this.newRating = newRating;
		}
		
		public Map<String, Double> getNewRating() {
			return newRating;
		}
		
	}
	
}
