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
package de.matzefratze123.heavyspleef.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.Permissions;

public class UpdateListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		if (!HeavySpleef.getSystemConfig().getRootSection().isAutoUpdate()) {
			return;
		}
		if (!player.hasPermission(Permissions.UPDATE_PLUGIN.getPerm())) {
			return;
		}
		if (!HeavySpleef.getInstance().getUpdater().isUpdateAvailable()) {
			return;
		}
		
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Your version of spleef is outdated! New version: " + ChatColor.GOLD + HeavySpleef.getInstance().getUpdater().getFileTitle());
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " If you wish to download the new version of HeavySpleef type /spleef update");
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " You may have to " + ChatColor.UNDERLINE + "delete" + ChatColor.RESET + ChatColor.DARK_PURPLE + " your config.yml for a new one.");
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Please visit http://dev.bukkit.org/bukkit-plugins/heavyspleef/ for more information.");
	}

	
}
