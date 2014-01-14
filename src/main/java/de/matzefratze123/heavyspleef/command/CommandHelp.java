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
package de.matzefratze123.heavyspleef.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.command.handler.CommandHandler;
import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandHelp extends HSCommand {
	
	public CommandHelp() {
		setMinArgs(0);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		List<HSCommand> commands = new ArrayList<HSCommand>(CommandHandler.getCommands().values());
		Collections.sort(commands);
		
		if (!sender.hasPermission(Permissions.HELP_ADMIN.getPerm()) && !sender.hasPermission(Permissions.HELP_USER.getPerm())) {
			sender.sendMessage(_("noPermission"));
			return;
		}
		
		sender.sendMessage(ChatColor.GRAY + "   -----   HeavySpleef Help   -----   ");
		
		//We don't want to print aliases again...
		List<Class<?>> printedCommands = new ArrayList<Class<?>>();
		
		for (HSCommand cmd : commands) {
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
				Help help = new Help(cmd);
					
				sender.sendMessage(ChatColor.GRAY + "/spleef " + cmd.getName() + ChatColor.RED + " - " + ChatColor.YELLOW + help.getHelp().get(0));
			}
				
			printedCommands.add(cmd.getClass());
		}
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef help [page]");
		help.addHelp("Shows Spleef help");
		
		return help;
	}

}
