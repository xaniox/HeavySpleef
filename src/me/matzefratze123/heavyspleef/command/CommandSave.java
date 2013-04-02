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
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandSave extends HSCommand {

	public CommandSave() {
		setMinArgs(0);
		setMaxArgs(0);
		setPermission(Permissions.SAVE);
		setUsage("/spleef save");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		long millis = System.currentTimeMillis();
		HeavySpleef.instance.database.save(false);
		HeavySpleef.instance.statisticDatabase.save();
		sender.sendMessage(_("gamesSaved"));
		sender.sendMessage(__(ChatColor.GRAY + "Took " + (System.currentTimeMillis() - millis) + "ms"));
	}

}
