/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.flag.defaults;

import de.xaniox.heavyspleef.core.event.GameEndEvent;
import de.xaniox.heavyspleef.core.event.GameStartEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.i18n.ParsedMessage;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.List;

@Flag(name = "timeout")
public class FlagTimeout extends IntegerFlag {

	private static final long TICKS_MULTIPLIER = 20L;
	private final BukkitScheduler scheduler;
	private final DecimalFormat secondFormat = new DecimalFormat("00");
	private final DecimalFormat minuteFormat = new DecimalFormat("0");
	private BukkitTask task;
	
	public FlagTimeout() {
		this.scheduler = Bukkit.getScheduler();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a timeout for a Spleef game to stop");
	}
	
	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 0) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_TIMEOUT));
		}
	}
	
	@Subscribe(priority = Subscribe.Priority.HIGHEST)
	public void onGameStart(GameStartEvent event) {
		TimeoutRunnable runnable = new TimeoutRunnable(event.getGame());
		
		task = scheduler.runTaskTimer(getHeavySpleef().getPlugin(), runnable, 0L, 1L * TICKS_MULTIPLIER);
	}
	
	@Subscribe
	public void onGameEnd(GameEndEvent event) {
		if (task != null) {
			//This game has already ended
			task.cancel();
		}
	}
	
	private class TimeoutRunnable implements Runnable {

		private Game game;
		private int secondsLeft;
		private final int length;
		private String scoreboardTitle;
		
		public TimeoutRunnable(Game game) {
			this.game = game;
			this.length = getValue();
			this.secondsLeft = length;
			
			FlagScoreboard.GetScoreboardDisplayNameEvent event = new FlagScoreboard.GetScoreboardDisplayNameEvent();
			game.getEventBus().callEvent(event);
			this.scoreboardTitle = event.getDisplayName();
		}
		
		@Override
		public void run() {
			float exp = (float) secondsLeft / length;
			
			if (secondsLeft <= 0) {
				game.broadcast(getI18N().getString(Messages.Broadcast.GAME_TIMED_OUT));
				game.stop();
				game.flushQueue();
				task.cancel();
			} else if ((secondsLeft > 60 && secondsLeft % 60 == 0) || (secondsLeft <= 60 && secondsLeft % 30 == 0) || secondsLeft <= 5) {
				String message = getTimeString();
				
				game.broadcast(message);
			}
			
			String scoreboardString = getScoreboardTitle();
			FlagScoreboard.SetScoreboardDisplayNameEvent event = new FlagScoreboard.SetScoreboardDisplayNameEvent();
			event.setDisplayName(scoreboardString);
			game.getEventBus().callEvent(event);
			
			for (SpleefPlayer player : game.getPlayers()) {
				Player bukkitPlayer = player.getBukkitPlayer();
				bukkitPlayer.setExp(exp);
				bukkitPlayer.setLevel(secondsLeft);
			}
			
			secondsLeft--;
		}
		
		private String getTimeString() {
			int minutes = secondsLeft / 60;
			int seconds = secondsLeft % 60;
			
			ParsedMessage parsedMessage = getI18N().getVarString(Messages.Broadcast.GAME_TIMEOUT_COUNTDOWN);
			
			//Indexes: 0 = seconds, 1 = minutes, 2 = hours
			String[] timeUnitStrings = getI18N().getStringArray(Messages.Arrays.TIME_UNIT_ARRAY);
			
			if (minutes == 0) {
				parsedMessage.setVariable("timeout", seconds + " " + timeUnitStrings[0]);
			} else {
				parsedMessage.setVariable("timeout", minuteFormat.format(minutes) + ":" + secondFormat.format(seconds) + " " + timeUnitStrings[1]);
			}
			
			return parsedMessage.toString();
		}
		
		private String getScoreboardTitle() {
			int minutes = secondsLeft / 60;
			int seconds = secondsLeft % 60;
			
			return scoreboardTitle + "     " + ChatColor.GRAY + (minutes > 0 ? minuteFormat.format(minutes) + ":" : "")
					+ secondFormat.format(seconds);
		}
		
	}

}