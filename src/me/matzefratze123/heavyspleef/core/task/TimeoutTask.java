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
package me.matzefratze123.heavyspleef.core.task;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.config.ConfigUtil;

public class TimeoutTask extends AbstractCountdown {

	private Game game;

	public TimeoutTask(int start, Game game) {
		super(start);
		this.game = game;
	}

	@Override
	public void onCount() {
		if (getTimeRemaining() <= 120) {
			if (getTimeRemaining() <= 5) {
				String message = Game._("timeLeftSeconds",
						String.valueOf(getTimeRemaining()));
				game.broadcast(message, ConfigUtil.getBroadcast("timeout"));
				return;
			}

			if (getTimeRemaining() % 30 != 0)
				return;

			int minutes = getTimeRemaining() / 60;
			int seconds = getTimeRemaining() % 60;

			String message = minutes == 0 ? Game._("timeLeftSeconds",
					String.valueOf(getTimeRemaining())) : Game._(
					"timeLeftMinutes", String.valueOf(minutes),
					String.valueOf(seconds));

			game.broadcast(message, ConfigUtil.getBroadcast("timeout"));
		}
	}

	@Override
	public void onFinish() {
		game.broadcast(Game._("timeoutReached"), ConfigUtil.getBroadcast("timeout"));
		game.endInDraw();
	}

}
