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
package de.xaniox.heavyspleef.core.event;

import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;

import java.util.List;

public class PlayerWinGameEvent extends GameEndEvent {

	public static final double NO_EXPECTATION = -1;
	
	private SpleefPlayer[] winners;
	private List<SpleefPlayer> losePlaces;
	private double ratingExpectation = -1D;
	
	public PlayerWinGameEvent(Game game, SpleefPlayer[] winners, List<SpleefPlayer> losePlaces) {
		super(game);
		
		this.winners = winners;
		this.losePlaces = losePlaces;
	}

	public SpleefPlayer[] getWinners() {
		return winners;
	}

	public List<SpleefPlayer> getLosePlaces() {
		return losePlaces;
	}

    public double getRatingExpectation() {
        return ratingExpectation;
    }

    public void setRatingExpectation(double ratingExpectation) {
        this.ratingExpectation = ratingExpectation;
    }

    /**
     * Deprecated due to a typo<br>
     * Use @{link #getRatingExpectation}.
     */
    @Deprecated
	public double getRatingExpectiation() {
		return ratingExpectation;
	}

    /**
     * Deprecated due to a typo<br>
     * Use @{link #setRatingExpectation}.
     */
    @Deprecated
	public void setRatingExpectiation(double ratingExpectiation) {
		this.ratingExpectation = ratingExpectiation;
	}

}