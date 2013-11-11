/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.core.task;

import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.StopCause;
import de.matzefratze123.heavyspleef.util.I18N;

public class TimeoutTask extends AbstractCountdown {

	public static final String TASK_ID_KEY = "timeoutTask";
	private Game game;

	public TimeoutTask(int start, Game game) {
		super(start);
		this.game = game;
	}

	@Override
	public void onCount() {
		if (getTimeRemaining() <= 120) {
			if (getTimeRemaining() <= 5) {
				String message = I18N._("timeLeftSeconds",
						String.valueOf(getTimeRemaining()));
				game.broadcast(message, ConfigUtil.getBroadcast("timeout"));
				return;
			}

			if (getTimeRemaining() % 30 != 0)
				return;

			int minutes = getTimeRemaining() / 60;
			int seconds = getTimeRemaining() % 60;

			String message = minutes == 0 ? I18N._("timeLeftSeconds",
					String.valueOf(getTimeRemaining())) : I18N._(
					"timeLeftMinutes", String.valueOf(minutes),
					String.valueOf(seconds));

			game.broadcast(message, ConfigUtil.getBroadcast("timeout"));
		}
	}

	@Override
	public void onFinish() {
		game.broadcast(I18N._("timeoutReached"), ConfigUtil.getBroadcast("timeout"));
		game.stop(StopCause.DRAW);
		game.cancelTask(TASK_ID_KEY);
	}

}
