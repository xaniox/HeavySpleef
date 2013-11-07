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
package de.matzefratze123.heavyspleef.command;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandHelp extends HSCommand {
	
	public CommandHelp() {
		setMaxArgs(1);
		setMinArgs(0);
		setUsage("/spleef help [page]");
		setHelp("Shows Spleef help");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Map<String, HSCommand> commands = CommandHandler.getCommands();
		
		sender.sendMessage(ChatColor.DARK_BLUE + "   -----   HeavySpleef Help   -----   ");
		
		//We don't want to print aliases again...
		List<Class<?>> printedCommands = new ArrayList<Class<?>>();
		
		for (HSCommand cmd : commands.values()) {
			if (printedCommands.contains(cmd.getClass())) {
				continue;
			}
			
			if (!cmd.getClass().isAnnotationPresent(UserType.class)) {
				continue;
			}
			
			UserType userType = cmd.getClass().getAnnotation(UserType.class);
			Type type = userType.value();
			
			boolean isPermitted = false;
			
			if (type == Type.ADMIN) {
				isPermitted = sender.hasPermission(Permissions.HELP_ADMIN.getPerm());
			}
			
			if (type == Type.PLAYER) {
				isPermitted = sender.hasPermission(Permissions.HELP_USER.getPerm());
			}
			
			if (isPermitted) {
				String msg = ChatColor.GOLD + cmd.getExactUsage() + ChatColor.RED + " - " + ChatColor.YELLOW + cmd.getHelp();
				
				sender.sendMessage(msg);
				printedCommands.add(cmd.getClass());
			}
		}
	}

}
