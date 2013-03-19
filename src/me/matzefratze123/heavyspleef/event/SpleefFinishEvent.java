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
package me.matzefratze123.heavyspleef.event;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.StopCause;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpleefFinishEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Game game;
	private StopCause cause;
	
	public SpleefFinishEvent(Game game, StopCause cause) {
		this.game = game;
		this.cause = cause;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public HandlerList getHandlerList() {
		return getHandlers();
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public StopCause getCause() {
		return this.cause;
	}

}
