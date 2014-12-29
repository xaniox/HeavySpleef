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
package de.matzefratze123.heavyspleef.core;

import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CountdownRunnable extends SimpleBasicTask {

	private HeavySpleef heavySpleef;
	private Game game;
	private int remaining;
	
	public CountdownRunnable(HeavySpleef heavySpleef, int remaining, Game game) {
		super(heavySpleef.getPlugin(), TaskType.SYNC_REPEATING_TASK, 0L, 20L);
		
		this.remaining = remaining;
		this.game = game;
	}
	
	public int getRemaining() {
		return remaining;
	}
	
	@Override
	public void run() {
		if (remaining == 0) {
			game.start();
			cancel();
		} else if (remaining % 10 != 0 || remaining <= 5) {
			game.broadcast(BroadcastTarget.INGAME, heavySpleef.getVarMessage(Messages.Broadcast.GAME_COUNTDOWN_MESSAGE)
					.setVariable("remaining", String.valueOf(remaining))
					.toString());
		}
		
		--remaining;
	}

}
