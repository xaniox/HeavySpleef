package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.commands.internal.Command;
import de.matzefratze123.heavyspleef.commands.internal.CommandContext;
import de.matzefratze123.heavyspleef.commands.internal.CommandException;
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
