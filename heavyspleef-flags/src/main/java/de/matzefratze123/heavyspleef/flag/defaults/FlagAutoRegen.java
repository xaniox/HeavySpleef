package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

public class FlagAutoRegen extends IntegerFlag {

	private static final long TICKS_MULTIPLIER = 20L;
	private final BukkitScheduler scheduler;
	private BukkitTask task;
	
	public FlagAutoRegen() {
		this.scheduler = Bukkit.getScheduler();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a floor-regeneration interval");
	}
	
	@GameListener
	public void onGameStart(GameStartEvent event) {
		FloorRegenRunnable runnable = new FloorRegenRunnable(event.getGame());
		final long intervalTicks = getValue() * TICKS_MULTIPLIER;
		
		scheduler.runTaskTimer(getHeavySpleef().getPlugin(), runnable, intervalTicks, intervalTicks);
	}
	
	@GameListener
	public void onGameEnd(GameEndEvent event) {
		if (task != null) {
			//Cancel the task as this game ends
			task.cancel();
		}
	}
	
	private class FloorRegenRunnable implements Runnable {

		private Game game;
		
		public FloorRegenRunnable(Game game) {
			this.game = game;
		}
		
		@Override
		public void run() {
			for (Floor floor : game.getFloors()) {
				floor.regenerate();
			}
		}
		
	}

}
