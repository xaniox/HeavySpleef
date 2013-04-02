/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package me.matzefratze123.heavyspleef.api.event;

import me.matzefratze123.heavyspleef.api.GameData;
import me.matzefratze123.heavyspleef.core.LoseCause;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class SpleefLoseEvent extends SpleefEvent {

	public static HandlerList handlers = new HandlerList();
	
	private Player loser = null;
	private LoseCause cause = null;
	
	public SpleefLoseEvent(GameData game, Player player, LoseCause cause) {
		super(game);
		this.loser = player;
		this.cause = cause;
	}
	
	/**
	 * The cause of the lose
	 * 
	 * @see me.matzefratze123.heavyspleef.core.LoseCause
	 * @return A type of the enum LoseCause
	 */
	public LoseCause getCause() {
		return this.cause;
	}
	
	/**
	 * Gets the player that lost
	 */
	public Player getLoser() {
		return this.loser;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public HandlerList getHandlerList() {
		return handlers;
	}

	

}
