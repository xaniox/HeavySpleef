package me.matzefratze123.heavyspleef.core.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.GameState;
import me.matzefratze123.heavyspleef.core.region.Floor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AntiCampingTask implements Runnable {
	
	private static boolean taskEnabled = false;
	
	private boolean warnUser;
	private int     warnAt;
	private int     teleportAt;
	
	private Map<String, Location> lastLocation = new HashMap<String, Location>();
	private Map<String, Integer> antiCamping = new HashMap<String, Integer>();
	
	public AntiCampingTask() {
		if (taskEnabled)
			throw new IllegalStateException("Cannot start AntiCampingTask twice!");
		
		taskEnabled = true;
		
		//Get config values
		warnAt = HeavySpleef.getSystemConfig().getInt("anticamping.warnAt", 3);
		warnUser = HeavySpleef.getSystemConfig().getBoolean("anticamping.campWarn", true);
		teleportAt = HeavySpleef.getSystemConfig().getInt("anticamping.teleportAt", 6);
	}
	
	@Override
	public void run() {
		//Check every player
		for (Player player : Bukkit.getOnlinePlayers()) {
			//Goto the next player when he is not ingame
			if (!GameManager.isInAnyGame(player))
				continue;
			
			Game game = GameManager.fromPlayer(player);
			if (game.getGameState() != GameState.INGAME)
				continue;
			
			//Get the base value
			int current = antiCamping.containsKey(player.getName()) ? antiCamping.get(player.getName()) : 0;
			
			if (lastLocation.containsKey(player.getName())) {
				Location last = lastLocation.get(player.getName());
				Location now = player.getLocation();
				
				//Compare the differences of the last location
				double differenceX = last.getX() < now.getX() ? now.getX() - last.getX() : last.getX() - now.getX();
				double differenceZ = last.getZ() < now.getZ() ? now.getZ() - last.getZ() : last.getZ() - now.getZ();
				
				if ((differenceX < 1.0 && differenceZ < 1.0) || player.isSneaking()) {
					//Add one second to map
					current++;
					
					if (current == warnAt && warnUser)
						player.sendMessage(Game._("antiCampWarn", String.valueOf(teleportAt - warnAt)));
					
					if (current >= teleportAt) {
						teleportDown(player);
						antiCamping.remove(player.getName());
					} else {
						antiCamping.put(player.getName(), current);
					}
				} else {
					antiCamping.remove(player.getName());
				}
				
			}
			
			lastLocation.put(player.getName(), player.getLocation());
		}
	}
	
	private void teleportDown(Player player) {
		Location location = player.getLocation();
		
		Game game = GameManager.fromPlayer(player);
		if (game == null)
			return;
		
		List<Floor> floors = new ArrayList<Floor>(game.getFloors());
		Floor nearestFloor = null;
		
		//Calculate the nearest floor
		for (Floor floor : floors) {
			if (floor.getY() >= location.getY())
				continue;
			
			if (nearestFloor == null) {
				nearestFloor = floor;
				continue;
			}
			
			if (location.getY() - floor.getY() < location.getY() - nearestFloor.getY())
				nearestFloor = floor;
		}
		
		if (nearestFloor == null)
			return;
		Collections.sort(floors);
		for (int i = 0; i < floors.size(); i++) {
			//Check if the player is at the last floor
			if (i == 0 && nearestFloor.getY() == floors.get(i).getY()) {
				player.teleport(player.getLocation().clone().add(0, -1, 0));
				player.sendMessage(Game._("antiCampTeleport"));
				return;
			} else if (floors.get(i).getY() == nearestFloor.getY()){
				Location cloned = player.getLocation();
				cloned.setY(floors.get(i - 1).getY() + 1.25);
				
				player.teleport(cloned);
				player.sendMessage(Game._("antiCampTeleport"));
				return;
			}
			
		}
	}
	
}
