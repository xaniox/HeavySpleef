package de.matzefratze123.heavyspleef.core.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.LanguageHandler;

public class AntiCampingTask implements Runnable {
	
	private static boolean taskEnabled = false;
	
	private boolean warnUser;
	private int     warnAt;
	private int     teleportAt;
	
	private Map<String, Location> lastLocation = new HashMap<String, Location>();
	private Map<String, Integer> antiCamping = new HashMap<String, Integer>();
	
	public AntiCampingTask() {
		if (taskEnabled && isTaskRunning(HeavySpleef.getInstance().antiCampTid))
			throw new IllegalStateException("Cannot start AntiCampingTask twice!");
		
		taskEnabled = true;
		
		//Get config values
		warnAt = HeavySpleef.getSystemConfig().getInt("anticamping.warnAt", 3);
		warnUser = HeavySpleef.getSystemConfig().getBoolean("anticamping.campWarn", true);
		teleportAt = HeavySpleef.getSystemConfig().getInt("anticamping.teleportAt", 6);
	}
	
	/**
	 * Resets the anticamping timer for a player
	 */
	public void resetTimer(Player player) {
		antiCamping.remove(player.getName());
	}
	
	@Override
	public void run() {
		//Check every player
		for (Player bukkitPlayer : Bukkit.getOnlinePlayers()) {
			SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(bukkitPlayer);
			
			//Goto the next player when he is not ingame
			if (!player.isActive())
				continue;
			
			Game game = player.getGame();
			if (game == null || game.getGameState() != GameState.INGAME) {
				continue;
			}
			
			//Get the base value
			int current = antiCamping.containsKey(player.getName()) ? antiCamping.get(player.getName()) : 0;
			
			if (lastLocation.containsKey(player.getName())) {
				Location last = lastLocation.get(player.getName());
				Location now = player.getBukkitPlayer().getLocation();
				
				//Compare the differences of the last location
				double differenceX = last.getX() < now.getX() ? now.getX() - last.getX() : last.getX() - now.getX();
				double differenceZ = last.getZ() < now.getZ() ? now.getZ() - last.getZ() : last.getZ() - now.getZ();
				
				if ((differenceX < 1.0 && differenceZ < 1.0) || player.getBukkitPlayer().isSneaking()) {
					//Add one second to map
					current++;
					
					if (current == warnAt && warnUser)
						player.sendMessage(LanguageHandler._("antiCampWarn", String.valueOf(teleportAt - warnAt)));
					
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
			
			lastLocation.put(player.getName(), player.getBukkitPlayer().getLocation());
		}
	}
	
	private void teleportDown(SpleefPlayer player) {
		Location location = player.getBukkitPlayer().getLocation();
		
		Game game = player.getGame();
		if (game == null)
			return;
		
		List<IFloor> floors = new ArrayList<IFloor>(game.getComponents().getFloors());
		IFloor nearestFloor = null;
		
		//Calculate the nearest floor
		for (IFloor floor : floors) {
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
				player.getBukkitPlayer().teleport(player.getBukkitPlayer().getLocation().clone().add(0, -1, 0));
				player.sendMessage(LanguageHandler._("antiCampTeleport"));
				return;
			} else if (floors.get(i).getY() == nearestFloor.getY()){
				Location cloned = player.getBukkitPlayer().getLocation().clone();
				cloned.setY(floors.get(i - 1).getY() + 1.25);
				
				player.getBukkitPlayer().teleport(cloned);
				player.sendMessage(LanguageHandler._("antiCampTeleport"));
				return;
			}
			
		}
	}
	
	private static boolean isTaskRunning(int task) {
		if (task < 0)
			return false;
		
		return Bukkit.getScheduler().isCurrentlyRunning(task) || Bukkit.getScheduler().isQueued(task);
	}
	
}
