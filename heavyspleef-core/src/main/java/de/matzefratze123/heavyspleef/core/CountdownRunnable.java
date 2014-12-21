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
			game.broadcast(heavySpleef.getVarMessage(Messages.Broadcast.GAME_COUNTDOWN_MESSAGE)
					.setVariable("remaining", String.valueOf(remaining))
					.toString());
		}
		
		--remaining;
	}

}
