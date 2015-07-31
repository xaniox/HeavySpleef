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
import java.util.Map;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class GameCountdownEvent extends GameEvent implements Cancellable {
	
	private boolean cancelled;
	private List<Location> spawnLocations;
	private Map<SpleefPlayer, Location> spawnLocationMap;
	private int countdownLength;
	private boolean countdownEnabled;
	private String errorBroadcast;
	
	public GameCountdownEvent(Game game) {		
		super(game);
		
		countdownEnabled = true;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public List<Location> getSpawnLocations() {
		return spawnLocations;
	}

	public void setSpawnLocations(List<Location> spawnLocations) {
		this.spawnLocations = spawnLocations;
	}

	public Map<SpleefPlayer, Location> getSpawnLocationMap() {
		return spawnLocationMap;
	}

	public void setSpawnLocationMap(Map<SpleefPlayer, Location> spawnLocationMap) {
		this.spawnLocationMap = spawnLocationMap;
	}

	public int getCountdownLength() {
		return countdownLength;
	}

	public void setCountdownLength(int countdownLength) {
		this.countdownLength = countdownLength;
	}

	public boolean isCountdownEnabled() {
		return countdownEnabled;
	}

	public void setCountdownEnabled(boolean countdownEnabled) {
		this.countdownEnabled = countdownEnabled;
	}

	public String getErrorBroadcast() {
		return errorBroadcast;
	}

	public void setErrorBroadcast(String errorBroadcast) {
		this.errorBroadcast = errorBroadcast;
	}

}
