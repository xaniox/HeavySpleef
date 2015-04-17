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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContainer;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandHelp {
	
	private static final String BASE_COMMAND = "spleef";
	private static final CommandContainerComparator COMPARATOR = new CommandContainerComparator();
	private static final int RECORDS_PER_PAGE = 10;

	private final I18N i18n = I18N.getInstance();
	
	@Command(name = "help", permission = "heavyspleef.help",
			descref = Messages.Help.Description.HELP,
			usage = "/spleef help")
	public void onHelpCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		SpleefCommandManager manager = (SpleefCommandManager) heavySpleef.getCommandManager();
		CommandManagerService service = manager.getService();
		
		CommandContainer container = service.getCommand(BASE_COMMAND);
		List<CommandContainer> childs = Lists.newArrayList(container.getChildCommands());
		Collections.sort(childs, COMPARATOR);
		
		int maxPage = (int) Math.ceil(childs.size() / (double)RECORDS_PER_PAGE);
		int page = 0;
		if (context.argsLength() > 0) {
			try {
				page = Integer.parseInt(context.getString(0));
			} catch (NumberFormatException nfe) {}
			
			page = Math.max(page - 1, 0);
		}
		
		sender.sendMessage(i18n.getVarString(Messages.Command.HELP_HEADER)
				.setVariable("page", String.valueOf(page + 1))
				.setVariable("max-pages", String.valueOf(maxPage))
				.toString());
		
		for (int i = page * RECORDS_PER_PAGE; i < page * RECORDS_PER_PAGE + RECORDS_PER_PAGE; i++) {
			if (i >= childs.size()) {
				break;
			}
			
			CommandContainer child = childs.get(i);
			
			String desc = child.getDescription();
			if (desc.isEmpty() && !child.getDescriptionRef().isEmpty()) {
				desc = i18n.getString(child.getDescriptionRef());
			}
			
			sender.sendMessage(i18n.getVarString(Messages.Command.HELP_RECORD)
					.setVariable("command_fq", child.getFullyQualifiedName())
					.setVariable("command", child.getName())
					.setVariable("usage", child.getUsage())
					.setVariable("description", desc)
					.toString());
		}
	}
	
	public static class CommandContainerComparator implements Comparator<CommandContainer> {

		@Override
		public int compare(CommandContainer o1, CommandContainer o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	}

}
