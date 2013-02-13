package me.matzefratze123.heavyspleef.core;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AntiCampingTask implements Runnable {

	private boolean warn;
	private int     warnAt;
	private int     teleportAt;
	
	
	private Map<String, Location> lastLocation = new HashMap<String, Location>();
	
	public AntiCampingTask(boolean warn, int warnAt, int teleportAt) {
		this.warn = warn;
		this.warnAt = warnAt;
		this.teleportAt = teleportAt;
	}
	
	@Override
	public void run() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!GameManager.isInAnyGameIngame(p))
				continue;
			if (GameManager.antiCamping.containsKey(p.getName())) {
				int current = GameManager.antiCamping.get(p.getName());
				compareLocations(p, current);
				lastLocation.put(p.getName(), p.getLocation());
			} else {
				int current = 0;
				compareLocations(p, current);	
				lastLocation.put(p.getName(), p.getLocation());
			}
			
		}
	}
	
	private void compareLocations(Player p, int current) {
		if (lastLocation.containsKey(p.getName())) {
			Location last = lastLocation.get(p.getName());
			Location now = p.getLocation();
			
			double differenceX = last.getX() < now.getX() ? now.getX() - last.getX() : last.getX() - now.getX();
			double differenceZ = last.getZ() < now.getZ() ? now.getZ() - last.getZ() : last.getZ() - now.getZ();
			
			if ((differenceX < 1.0D && differenceZ < 1.0D) || p.isSneaking())
				addSecond(current, p);
			else
				GameManager.antiCamping.put(p.getName(), 0);
		}
	}
	
	private void addSecond(int current, Player p) {
		current++;
		
		if (current == this.warnAt) {
			if (this.warn)
				p.sendMessage(Game._("antiCampWarn", String.valueOf(teleportAt - warnAt)));
		} else if (current >= this.teleportAt) {
			p.sendMessage(Game._("antiCampTeleport"));
			teleportDown(p);
			GameManager.antiCamping.put(p.getName(), 0);
			return;
		}
		GameManager.antiCamping.put(p.getName(), current);
		lastLocation.put(p.getName(), p.getLocation());
	}
	
	private void teleportDown(Player p) {
		Location loc = p.getLocation();
		loc.setY(loc.getY() - 2);
		p.teleport(loc);
	}

}
