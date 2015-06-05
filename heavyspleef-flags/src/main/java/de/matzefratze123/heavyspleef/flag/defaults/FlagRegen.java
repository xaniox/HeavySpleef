package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import com.sk89q.worldedit.EditSession;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.SimpleBasicTask;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "regen")
public class FlagRegen extends IntegerFlag {

	private RegenerationTask task;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Regenerates all floors of the game in a specified interval");
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		Game game = event.getGame();
		
		if (task == null) {
			task = new RegenerationTask(game, getValue());			
		}
		
		if (task.isRunning()) {
			task.cancel();
		}
		
		task.run();
	}
	
	@Subscribe
	public void onGameEnd(GameEndEvent event) {
		if (task != null && task.isRunning()) {
			task.cancel();
		}
	}
	
	private class RegenerationTask extends SimpleBasicTask {

		private Game game;
		
		public RegenerationTask(Game game, int interval) {
			super(getHeavySpleef().getPlugin(), TaskType.SYNC_REPEATING_TASK, interval * 20L, interval * 20L);
		}

		@Override
		public void run() {
			EditSession session = game.newEditSession();
			
			for (Floor floor : game.getFloors()) {
				floor.generate(session);
			}
		}
		
	}

}
