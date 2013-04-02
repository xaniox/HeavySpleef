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

import java.io.File;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.Permissions;
import me.matzefratze123.heavyspleef.utility.Updater;
import me.matzefratze123.heavyspleef.utility.Updater.UpdateType;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class CommandUpdate extends HSCommand {

	public CommandUpdate() {
		setPermission(Permissions.UPDATE_PLUGIN);
		setUsage("/spleef update");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!plugin.getConfig().getBoolean("auto-update")) {
			sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Auto-Updater is disabled!");
			return;
		}
		if (!HeavySpleef.updateAvaible) {
			sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " There is no new update avaible!");
			return;
		}
		
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Updating plugin, please wait...");
		long start = System.currentTimeMillis();
		
		new Updater(plugin, "heavyspleef", HeavySpleef.pluginFile, UpdateType.NO_VERSION_CHECK, true);
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Done! Took " + (System.currentTimeMillis() - start) + "ms!");
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " You find the HeavySpleef.jar in the folder \"plugins/" + YamlConfiguration.loadConfiguration(new File("bukkit.yml")).getString("settings.update-folder") + "/HeavySpleef.jar\"");
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " You may have to" + ChatColor.UNDERLINE + " delete " + ChatColor.RESET + ChatColor.DARK_PURPLE + "your config.yml!");
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Visit http://dev.bukkit.org/server-mods/heavyspleef/ for more information!");
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Version will be auto-installed with the next server reload/restart");
		HeavySpleef.updateAvaible = false;
	}

	
	
}
