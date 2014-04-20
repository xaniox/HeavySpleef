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
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.I18N;

public class TaskAntiCamping implements Runnable {

	private static boolean				taskEnabled		= false;

	private int							id;

	private Map<SpleefPlayer, Location>	lastLocation	= new HashMap<SpleefPlayer, Location>();
	private Map<SpleefPlayer, Integer>	antiCamping		= new HashMap<SpleefPlayer, Integer>();

	public void start() {
		if (taskEnabled) {
			throw new IllegalStateException("Task already running!");
		}

		taskEnabled = true;

		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 20L, 20L);
	}

	public void restart() {
		if (taskEnabled && isTaskRunning(id)) {
			Bukkit.getScheduler().cancelTask(id);
		}

		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 20L, 20L);
	}

	/**
	 * Resets the anticamping timer for a player
	 */
	public void resetTimer(Player player) {
		antiCamping.remove(player.getName());
	}

	@Override
	public void run() {
		final boolean warnEnabled = HeavySpleef.getSystemConfig().getAnticampingSection().isWarnEnabled();
		final int warnAt = HeavySpleef.getSystemConfig().getAnticampingSection().getWarnAt();
		final int teleportAt = HeavySpleef.getSystemConfig().getAnticampingSection().getTeleportAt();

		// Check every game
		for (Game game : GameManager.getGames()) {
			if (game.getGameState() != GameState.INGAME) {
				continue;
			}

			if (!game.getFlag(FlagType.CAMP_DETECTION)) {
				continue;
			}

			for (SpleefPlayer player : game.getIngamePlayers()) {
				// Get the base value
				int current = antiCamping.containsKey(player) ? antiCamping.get(player) : 0;

				if (lastLocation.containsKey(player)) {
					Location last = lastLocation.get(player);
					Location now = player.getBukkitPlayer().getLocation();

					// Compare the differences of the last location
					double differenceX = last.getX() < now.getX() ? now.getX() - last.getX() : last.getX() - now.getX();
					double differenceZ = last.getZ() < now.getZ() ? now.getZ() - last.getZ() : last.getZ() - now.getZ();

					if ((differenceX < 1.0 && differenceZ < 1.0) || player.getBukkitPlayer().isSneaking()) {
						// Add one second to map
						current++;

						if (current == warnAt && warnEnabled)
							player.sendMessage(I18N._("antiCampWarn", String.valueOf(teleportAt - warnAt)));

						if (current >= HeavySpleef.getSystemConfig().getAnticampingSection().getTeleportAt()) {
							teleportDown(player);
							antiCamping.remove(player);
						} else {
							antiCamping.put(player, current);
						}
					} else {
						antiCamping.remove(player);
					}

				}

				lastLocation.put(player, player.getBukkitPlayer().getLocation());
			}
		}
	}

	private void teleportDown(SpleefPlayer player) {
		Location location = player.getBukkitPlayer().getLocation();

		Game game = player.getGame();
		if (game == null)
			return;

		List<IFloor> floors = new ArrayList<IFloor>(game.getComponents().getFloors());
		IFloor nearestFloor = null;

		// Calculate the nearest floor
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
			// Check if the player is at the last floor
			if (i == 0 && nearestFloor.getY() == floors.get(i).getY()) {
				player.getBukkitPlayer().teleport(player.getBukkitPlayer().getLocation().add(0, -1, 0));
				player.sendMessage(I18N._("antiCampTeleport"));
				return;
			} else if (floors.get(i).getY() == nearestFloor.getY()) {
				Location cloned = player.getBukkitPlayer().getLocation().clone();
				cloned.setY(floors.get(i - 1).getY() + 1.25);

				player.getBukkitPlayer().teleport(cloned);
				player.sendMessage(I18N._("antiCampTeleport"));
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
