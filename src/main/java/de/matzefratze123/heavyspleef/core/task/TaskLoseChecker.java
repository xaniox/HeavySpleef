/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
import org.bukkit.Location;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class TaskLoseChecker implements Runnable, Task {
	
	private static final long CHECK_INTERVAL = 5L;
	
	private Game game;
	private int pid = -1;
	
	public TaskLoseChecker(Game game) {
		this.game = game;
	}
	
	@Override
	public int start() {
		if (pid != -1) {
			throw new IllegalStateException("Task already registered!");
		}
		
		pid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 0L, CHECK_INTERVAL);
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

	@Override
	public void run() {
		if (game.getGameState() != GameState.INGAME) {
			return;
		}
		
		for (SpleefPlayer player : game.getIngamePlayers()) {
			if (!player.isActive()) {
				continue;
			}
			
			Location location = player.getBukkitPlayer().getLocation();
			if (location.getBlock().isLiquid()) {
				game.leave(player, LoseCause.LOSE);
			} else {
				for (LoseZone zone : game.getComponents().getLoseZones()) {
					if (!zone.contains(location)) {
						continue;
					}
					
					game.leave(player, LoseCause.LOSE);
					break;
				}
			}
		}
	}
	
}
