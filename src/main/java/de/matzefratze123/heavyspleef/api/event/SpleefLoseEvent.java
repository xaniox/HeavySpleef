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
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class SpleefLoseEvent extends SpleefEvent {

	public static final HandlerList	handlers	= new HandlerList();

	private SpleefPlayer			killer		= null;
	private Player					loser		= null;
	private LoseCause				cause		= null;

	public SpleefLoseEvent(IGame game, Player player, SpleefPlayer killer, LoseCause cause) {
		super(game);
		this.killer = killer;
		this.loser = player;
		this.cause = cause;
	}

	/**
	 * The cause of the lose
	 * 
	 * @see de.matzefratze123.heavyspleef.core.LoseCause
	 * @return A type of the enum LoseCause
	 */
	public LoseCause getCause() {
		return cause;
	}

	/**
	 * Gets the player that lost
	 */
	public Player getLoser() {
		return loser;
	}

	/**
	 * Gets the player who killed the loser May be null if there was no killer
	 * detected
	 */
	public SpleefPlayer getKiller() {
		return killer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
