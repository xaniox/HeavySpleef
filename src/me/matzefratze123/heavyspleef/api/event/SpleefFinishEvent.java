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
import me.matzefratze123.heavyspleef.core.StopCause;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpleefFinishEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private GameData gameData;
	private Player winner;
	private StopCause cause;
	
	public SpleefFinishEvent(GameData game, StopCause cause, Player winner) {
		this.gameData = game;
		this.cause = cause;
		this.winner = winner;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public HandlerList getHandlerList() {
		return getHandlers();
	}
	
	/**
	 * Gets the game that is involved in this event
	 * 
	 * @return The gamedata of the game
	 */
	public GameData getGame() {
		return this.gameData;
	}
	
	/**
	 * Gets the stopcause of this game
	 * 
	 * @return A enum constant of the StopCause
	 * @see me.matzefratze123.heavyspleef.core.StopCause
	 */
	public StopCause getCause() {
		return this.cause;
	}
	
	/**
	 * Gets the winner of the spleef game</br></br>
	 * <b>This winner can be null, if the game
	 * has been stopped manually or the game
	 * has ended in a draw!</b>
	 * 
	 * @return The winner, or null
	 */
	public Player getWinner() {
		return winner;
	}

}
