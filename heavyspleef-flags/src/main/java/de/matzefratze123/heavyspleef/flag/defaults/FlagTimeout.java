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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameEventHandler;
import de.matzefratze123.heavyspleef.core.event.GameEventHandler.Priority;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "timeout")
public class FlagTimeout extends IntegerFlag {

	private static final long TICKS_MULTIPLIER = 20L;
	private final BukkitScheduler scheduler;
	private BukkitTask task;
	
	public FlagTimeout() {
		this.scheduler = Bukkit.getScheduler();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a timeout for a Spleef game to stop");
	}
	
	@GameEventHandler(priority = Priority.HIGHEST)
	public void onGameStart(GameStartEvent event) {
		TimeoutRunnable runnable = new TimeoutRunnable(event.getGame());
		
		task = scheduler.runTaskTimer(getHeavySpleef().getPlugin(), runnable, 0L, 1L * TICKS_MULTIPLIER);
	}
	
	@GameEventHandler
	public void onGameEnd(GameEndEvent event) {
		if (task != null) {
			//This game has already ended
			task.cancel();
		}
	}
	
	private class TimeoutRunnable implements Runnable {

		private Game game;
		private int secondsLeft;
		
		public TimeoutRunnable(Game game) {
			this.game = game;
			this.secondsLeft = getValue();
		}
		
		@Override
		public void run() {
			if (secondsLeft <= 0) {
				game.broadcast(getI18N().getString(Messages.Broadcast.GAME_TIMED_OUT));
				game.stop();
				task.cancel();
			} else if (secondsLeft % 30 == 0 || secondsLeft <= 10) {
				String message = getTimeString();
				game.broadcast(message);
			}
			
			secondsLeft--;
		}
		
		private String getTimeString() {
			int minutes = secondsLeft / 60;
			int seconds = secondsLeft % 60;
			
			String message = I18N.getInstance().getVarString(Messages.Broadcast.GAME_TIMEOUT_COUNTDOWN)
				.setVariable("minutes", String.valueOf(minutes))
				.setVariable("seconds", String.valueOf(seconds))
				.toString();
			
			return message;
		}
		
	}

}
