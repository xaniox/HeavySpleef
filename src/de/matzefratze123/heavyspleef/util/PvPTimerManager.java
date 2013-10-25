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
		int pvptimer = HeavySpleef.getSystemConfig().getInt("general.pvptimer");
		
		if (pvpTimerTasks.containsKey(player.getName())) {
			cancelTimerTask(player);
		}
		
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.instance, target, Math.abs(pvptimer) * 20L);
		
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
