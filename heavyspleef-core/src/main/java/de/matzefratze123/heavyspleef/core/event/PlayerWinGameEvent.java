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
package de.matzefratze123.heavyspleef.core.event;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerWinGameEvent extends GameEndEvent {

	public static final double NO_EXPECTATION = -1;
	
	private SpleefPlayer[] winners;
	private double ratingExpectiation = -1D;
	
	public PlayerWinGameEvent(Game game, SpleefPlayer[] winners) {
		super(game);
		
		this.winners = winners;
	}
	
	public SpleefPlayer[] getWinners() {
		return winners;
	}

	public double getRatingExpectations() {
		return ratingExpectiation;
	}
	
	public void setRatingExpectiation(double ratingExpectiation) {
		this.ratingExpectiation = ratingExpectiation;
	}

}
