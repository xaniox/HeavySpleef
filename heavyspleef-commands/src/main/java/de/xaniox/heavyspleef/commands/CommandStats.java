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

import de.xaniox.heavyspleef.commands.base.*;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.xaniox.heavyspleef.core.player.PlayerManager;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.core.stats.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandStats {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "stats", usage = "/spleef stats [player|top [page]]",
			descref = Messages.Help.Description.STATS)
	public void onStatsCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		AsyncReadWriteHandler databaseHandler = heavySpleef.getDatabaseHandler();
		
		Statistic.StatisticPrinter printer;
		
		if (context.argsLength() > 0) {
			String arg = context.getString(0);
			
			if (arg.equalsIgnoreCase("top")) {
				CommandValidate.isTrue(sender.hasPermission(Permissions.PERMISSION_STATS_TOP), i18n.getString(Messages.Command.NO_PERMISSION));
				int page = 1;
				
				if (context.argsLength() > 1) {
					//Try to parse a page number
					try {
						page = Integer.parseInt(context.getString(1));
					} catch (NumberFormatException nfe) {
						//Just use the default value
					}	
				}
				
				printer = new Statistic.TopStatisticPrinter(sender, page, databaseHandler, heavySpleef.getLogger());
			} else {
				CommandValidate.isTrue(sender.hasPermission(Permissions.PERMISSION_STATS_OTHER), i18n.getString(Messages.Command.NO_PERMISSION));
				printer = new Statistic.FullStatisticPrinter(databaseHandler, sender, arg, heavySpleef.getLogger());
			}
		} else {
			if (!(sender instanceof SpleefPlayer)) {
				sender.sendMessage(i18n.getString(Messages.Command.PLAYER_ONLY));
				return;
			}
			
			CommandValidate.isTrue(sender.hasPermission(Permissions.PERMISSION_STATS), i18n.getString(Messages.Command.NO_PERMISSION));
			printer = new Statistic.FullStatisticPrinter(databaseHandler, sender, sender.getName(), heavySpleef.getLogger());
		}
		
		printer.print();
	}
	
	@TabComplete("stats")
	public void onStatsTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		PlayerManager manager = heavySpleef.getPlayerManager();
		
		if (context.argsLength() == 1) {
			for (SpleefPlayer player : manager.getSpleefPlayers()) {
				list.add(player.getName());
			}
		}
	}
	
}