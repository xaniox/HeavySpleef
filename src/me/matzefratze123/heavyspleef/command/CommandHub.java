/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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
package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.LoseCause;
import me.matzefratze123.heavyspleef.util.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHub extends HSCommand {

	public CommandHub() {
		setPermission(Permissions.TELEPORT_HUB);
		setUsage("/spleef hub");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		tpToHub(player);
	}
	
	public static void tpToHub(final Player player) {
		int pvpTimer = HeavySpleef.getSystemConfig().getInt("general.pvptimer", 5);
		
		if (GameManager.getSpleefHub() == null) {
			player.sendMessage(_("noSpleefHubSet"));
			return;
		}
		if (GameManager.isInAnyGame(player)) {
			Game game = GameManager.fromPlayer(player);
			game.removePlayer(player, LoseCause.QUIT);
		}
		
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				Game.pvpTimerTasks.remove(player.getName());
				player.teleport(GameManager.getSpleefHub());
				player.sendMessage(_("welcomeToHUB"));
			}
		}, pvpTimer * 20L);
		Game.pvpTimerTasks.put(player.getName(), taskId);
		player.sendMessage(_("teleportingToHub"));
	}
	
}
