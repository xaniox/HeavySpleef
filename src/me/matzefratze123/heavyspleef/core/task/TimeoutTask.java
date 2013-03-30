package me.matzefratze123.heavyspleef.core.task;

import me.matzefratze123.heavyspleef.core.Game;

public class TimeoutTask extends Countdown {

	private Game game;
	public TimeoutTask(int start, Game game) {
		super(start);
		this.game = game;
	}
	
	@Override
	public void onCount() {
		if (getTimeRemaining() <= 120) {
			if (getTimeRemaining() <= 5) {
				String message = Game._("timeLeftSeconds", String.valueOf(getTimeRemaining()));//TODO
				game.broadcast(message);
				return;
			}
			
			if (getTimeRemaining() % 30 != 0)
				return;
			
			int minutes = getTimeRemaining() / 60;
			int seconds = getTimeRemaining() % 60;
			
			String message = minutes == 0 ? Game._("timeLeftSeconds", String.valueOf(getTimeRemaining())) : Game._("timeLeftMinutes", String.valueOf(minutes), String.valueOf(seconds));//TODO
			
			game.broadcast(message);
		}
	}
	
	@Override
	public void onFinish() {
		game.broadcast(Game._("timeoutReached"));//TODO
		game.endInDraw();
	}

}
