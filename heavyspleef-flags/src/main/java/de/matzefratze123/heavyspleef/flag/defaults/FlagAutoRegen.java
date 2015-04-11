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

import com.sk89q.worldedit.EditSession;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameEventHandler;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "auto-regen")
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
	
	@GameEventHandler
	public void onGameStart(GameStartEvent event) {
		FloorRegenRunnable runnable = new FloorRegenRunnable(event.getGame());
		final long intervalTicks = getValue() * TICKS_MULTIPLIER;
		
		scheduler.runTaskTimer(getHeavySpleef().getPlugin(), runnable, intervalTicks, intervalTicks);
	}
	
	@GameEventHandler
	public void onGameEnd(GameEndEvent event) {
		if (task != null) {
			//Cancel the task as this game ends
			task.cancel();
		}
	}
	
	private class FloorRegenRunnable implements Runnable {

		private Game game;
		private EditSession editSession;
		
		public FloorRegenRunnable(Game game) {
			this.game = game;
			this.editSession = game.newEditSession();
		}
		
		@Override
		public void run() {
			for (Floor floor : game.getFloors()) {
				floor.generate(editSession);
			}
		}
		
	}

}
