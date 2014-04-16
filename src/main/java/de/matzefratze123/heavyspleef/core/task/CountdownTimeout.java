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
package de.matzefratze123.heavyspleef.core.task;

import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.StopCause;
import de.matzefratze123.heavyspleef.util.I18N;

public class CountdownTimeout extends Countdown implements CountdownListener {

	public static final String	TASK_ID_KEY	= "timeoutTask";

	private Game				game;

	public CountdownTimeout(Game game, int ticks) {
		super(ticks);

		this.game = game;
		addCountdownListener(this);
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onCancel() {
	}

	@Override
	public void onFinish() {
		game.broadcast(I18N._("timeoutReached"), ConfigUtil.getBroadcast(MessageType.TIMEOUT));
		game.stop(StopCause.DRAW);
		cancel();
	}

	@Override
	public void onTick() {
		if (getTicksLeft() <= 120) {
			if (getTicksLeft() <= 5) {
				String message = I18N._("timeLeftSeconds", String.valueOf(getTicksLeft()));
				game.broadcast(message, ConfigUtil.getBroadcast(MessageType.TIMEOUT));
				return;
			}

			if (getTicksLeft() % 30 != 0)
				return;

			int minutes = getTicksLeft() / 60;
			int seconds = getTicksLeft() % 60;

			String message = minutes == 0 ? I18N._("timeLeftSeconds", String.valueOf(getTicksLeft())) : I18N._("timeLeftMinutes", String.valueOf(minutes), String.valueOf(seconds));

			game.broadcast(message, ConfigUtil.getBroadcast(MessageType.TIMEOUT));
		}
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onUnpause() {
	}

}
