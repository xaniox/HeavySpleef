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

import com.sk89q.worldedit.EditSession;

import de.matzefratze123.heavyspleef.core.SimpleBasicTask;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.ValidationException;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "regen")
public class FlagRegen extends IntegerFlag {

	private RegenerationTask task;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Regenerates all floors of the game in a specified interval");
	}
	
	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 0) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_REGEN_INTERVAL));
		}
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
		
		task.start();
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
			
			this.game = game;
		}

		@Override
		public void run() {
			EditSession session = game.newEditSession();
			
			for (Floor floor : game.getFloors()) {
				floor.generate(session);
			}
			
			game.broadcast(getI18N().getString(Messages.Broadcast.FLOORS_REGENERATED));
		}
		
	}

}