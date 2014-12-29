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

import java.util.List;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.Game;

public class GameCountdownEvent extends GameEvent {
	
	private List<Location> spawnLocations;
	private int countdownLength;
	private boolean countdownEnabled;
	
	public GameCountdownEvent(Game game) {		
		super(game);
		
		countdownEnabled = true;
	}
	
	public void setSpawnLocations(List<Location> spawnLocations) {
		this.spawnLocations = spawnLocations;
	}
	
	public List<Location> getSpawnLocations() {
		return spawnLocations;
	}
	
	public void setCountdownLength(int countdownLength) {
		this.countdownLength = countdownLength;
	}
	
	public int getCountdownLength() {
		return countdownLength;
	}
	
	public void setCountdownEnabled(boolean countdownEnabled) {
		this.countdownEnabled = countdownEnabled;
	}
	
	public boolean isCountdownEnabled() {
		return countdownEnabled;
	}

}
