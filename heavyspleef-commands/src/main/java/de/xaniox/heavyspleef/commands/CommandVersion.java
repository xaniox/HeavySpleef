/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.xaniox.heavyspleef.commands;

import de.xaniox.heavyspleef.commands.base.Command;
import de.xaniox.heavyspleef.commands.base.CommandContext;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.i18n.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandVersion {
	
	@Command(name = "version", descref = Messages.Help.Description.VERSION,
			usage = "/spleef version")
	public void onVersionCommand(CommandContext context, HeavySpleef heavySpleef) {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		Plugin plugin = heavySpleef.getPlugin();
		PluginDescriptionFile desc = plugin.getDescription();
		
		sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "HeavySpleef " + ChatColor.GREEN + "v" + desc.getVersion() + ChatColor.GRAY
				+ " by " + ChatColor.GREEN + "Matze" + ChatColor.GRAY + " (matzefratze123)");
		sender.sendMessage(ChatColor.GRAY + "Visit http://dev.bukkit.org/bukkit-plugins/heavyspleef for more information");
	}

}