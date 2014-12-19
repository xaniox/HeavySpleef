package de.matzefratze123.heavyspleef.core;

import org.bukkit.plugin.Plugin;

public class CountdownRunnable extends SimpleBasicTask {

	private int remaining;
	private Runnable startCommand;
	private Runnable countdownCommand;
	
	public CountdownRunnable(Plugin plugin, int remaining, Runnable startCommand, Runnable countdownCommand) {
		super(plugin, TaskType.SYNC_REPEATING_TASK, 0L, 20L);
		
		this.remaining = remaining;
		this.startCommand = startCommand;
		this.countdownCommand = countdownCommand;
	}
	
	public int getRemaining() {
		return remaining;
	}
	
	@Override
	public void run() {
		if (remaining == 0) {
			startCommand.run();
			cancel();
		} else if (remaining % 10 != 0 || remaining <= 5) {
			countdownCommand.run();
		}
		
		--remaining;
	}

}
