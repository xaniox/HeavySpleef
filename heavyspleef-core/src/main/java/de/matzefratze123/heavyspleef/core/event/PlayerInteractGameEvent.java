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

import lombok.Getter;

import org.bukkit.block.Block;
import org.bukkit.event.block.Action;

import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class PlayerInteractGameEvent extends PlayerGameEvent implements Cancellable {

	private boolean cancel;
	private @Getter Block block;
	private @Getter Action action;
	
	public PlayerInteractGameEvent(Game game, SpleefPlayer player, Block block, Action action) {
		super(game, player);
		
		this.block = block;
		this.action = action;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

}
