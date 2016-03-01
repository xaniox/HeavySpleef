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

import com.sk89q.worldedit.EditSession;
import de.xaniox.heavyspleef.core.SimpleBasicTask;
import de.xaniox.heavyspleef.core.event.GameEndEvent;
import de.xaniox.heavyspleef.core.event.GameStartEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.floor.FloorRegenerator;
import de.xaniox.heavyspleef.core.floor.RegenerationCause;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;

import java.util.List;

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
			FloorRegenerator regenerator = game.getFloorRegenerator();
			
			for (Floor floor : game.getFloors()) {
				regenerator.regenerate(floor, session, RegenerationCause.OTHER);
			}
			
			game.broadcast(getI18N().getString(Messages.Broadcast.FLOORS_REGENERATED));
		}
		
	}

}