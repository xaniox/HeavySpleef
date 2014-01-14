/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
package de.matzefratze123.heavyspleef.api.event;


import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import de.matzefratze123.heavyspleef.api.IGame;
import de.matzefratze123.heavyspleef.core.StopCause;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class SpleefFinishEvent extends SpleefEvent {

	private static final HandlerList handlers = new HandlerList();
	private SpleefPlayer winner;
	private StopCause cause;
	
	public SpleefFinishEvent(IGame game, StopCause cause, SpleefPlayer winner) {
		super(game);
		this.cause = cause;
		this.winner = winner;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * Gets the stopcause of this game
	 * 
	 * @return A enum constant of the type StopCause
	 * @see de.matzefratze123.heavyspleef.core.StopCause
	 */
	public StopCause getCause() {
		return this.cause;
	}
	
	/**
	 * Gets the winner of the spleef game
	 * May be null when there is no winner
	 * 
	 * @return The winner as a {@link SpleefPlayer}
	 */
	public SpleefPlayer getSpleefWinner() {
		return winner;
	}
	
	/**
	 * Gets the winner of the spleef game</br></br>
	 * <b>This winner can be null, if the game
	 * has been stopped manually or the game
	 * has ended in a draw!</b>
	 * 
	 * @return The winner, or null
	 * @deprecated Deprecated due to {@link #getSpleefWinner()}
	 */
	@Deprecated
	public Player getWinner() {
		return winner != null ? winner.getBukkitPlayer() : null;
	}

}
