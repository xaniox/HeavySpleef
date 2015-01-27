package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameListener.Priority;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

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
	
	@GameListener(priority = Priority.HIGHEST)
	public void onGameStart(GameStartEvent event) {
		TimeoutRunnable runnable = new TimeoutRunnable(event.getGame());
		
		task = scheduler.runTaskTimer(getHeavySpleef().getPlugin(), runnable, 0L, 1L * TICKS_MULTIPLIER);
	}
	
	@GameListener
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
			//game.broadcast(null); //TODO: Add message
			//game.stop();
			if (secondsLeft % 30 == 0 || secondsLeft <= 10) {
				String message = getTimeString();
				game.broadcast(message);
			}
			
			if (secondsLeft <= 0) {
				game.broadcast(null); //TODO: add message
				game.stop();
				task.cancel();
			}
			
			secondsLeft--;
		}
		
		private String getTimeString() {
			int minutes = secondsLeft / 60;
			int seconds = secondsLeft % 60;
			
			String message = getHeavySpleef().getVarMessage(null) //TODO: add message
				.setVariable("minutes", String.valueOf(minutes))
				.setVariable("seconds", String.valueOf(seconds))
				.toString();
			
			return message;
		}
		
	}

}
