package de.matzefratze123.heavyspleef.stats;

import org.bukkit.Bukkit;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class Cooldown implements Runnable {
	
	private boolean expired = true;
	private int task = -1;
	
	public void cooldown() {
		if (!HeavySpleef.getInstance().isEnabled()) {
			return;
		}
		if (!expired && task >= 0) {
			Bukkit.getScheduler().cancelTask(task);
		}
		
		expired = false;
		task = Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.getInstance(), this);
	}
	
	public boolean isExpired() {
		return expired;
	}
	
	@Override
	public void run() {
		expired = true;
		task = -1;
	}
	
}
