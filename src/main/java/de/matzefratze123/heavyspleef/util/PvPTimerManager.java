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
package de.matzefratze123.heavyspleef.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;

/**
 * Provides an manager for pvp timers
 * 
 * @author matzefratze123
 */
public class PvPTimerManager {
	
	private static Map<String, Integer> pvpTimerTasks = new HashMap<String, Integer>();
	
	public static void addToTimer(Player player, Runnable target) {
		int pvptimer = HeavySpleef.getSystemConfig().getGeneralSection().getPvPTimer();
		
		if (pvpTimerTasks.containsKey(player.getName())) {
			cancelTimerTask(player);
		}
		
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.getInstance(), target, Math.abs(pvptimer) * 20L);
		
		pvpTimerTasks.put(player.getName(), taskId);
	}
	
	public static void cancelTimerTask(Player player) {
		if (!pvpTimerTasks.containsKey(player.getName())) {
			return;
		}
		
		Bukkit.getScheduler().cancelTask(pvpTimerTasks.get(player.getName()));
		pvpTimerTasks.remove(player.getName());
	}
	
	public static boolean isOnTimer(Player player) {
		return pvpTimerTasks.containsKey(player.getName());
	}
	
}
