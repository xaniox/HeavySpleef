/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.core.task;

import org.bukkit.Bukkit;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.util.I18N;

public class RegenerationTask implements Runnable, Task {
	
	private Game game;
	
	private int pid = -1;
	
	public RegenerationTask(Game game) {
		this.game = game;
	}
	
	@Override
	public void run() {
		game.getComponents().regenerateFloors();
		game.broadcast(I18N._("floorsRegenerated"), ConfigUtil.getBroadcast(MessageType.FLOOR_REGENERATION));
	}

	@Override
	public int start() {
		if (pid != -1) {
			return -1;
		}
		
		long interval = game.getFlag(FlagType.REGEN_INTERVALL) * 20L;
		pid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 0L, interval);
		return pid;
	}

	@Override
	public void cancel() {
		if (pid == -1) {
			return;
		}
		
		Bukkit.getScheduler().cancelTask(pid);
		pid = -1;
	}

	@Override
	public boolean isAlive() {
		return pid != -1;
	}
	
}
