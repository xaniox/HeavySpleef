/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;

public class CommandStats {
	
	@Command(name = "stats", usage = "/spleef stats [player|top]",
			description = "Shows spleef statistics",
			permission = "heavyspleef.stats")
	public void onStatsCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		AsyncReadWriteHandler databaseHandler = heavySpleef.getDatabaseHandler();
		
		if (context.argsLength() > 0) {
			String arg = context.getString(0);
			
			if (arg.equalsIgnoreCase("top")) {
				//databaseHandler.getStatistic(player, callback);
			} else {
				
			}
		} else {
			
		}
		
		//TODO Print statistics
	}
	
}
