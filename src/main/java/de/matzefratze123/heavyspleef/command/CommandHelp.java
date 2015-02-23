/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.command;

import static de.matzefratze123.heavyspleef.util.I18N._;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandAliases;
import de.matzefratze123.api.hs.command.CommandData;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandHelp implements CommandListener {

	@Command(value = "help")
	@de.matzefratze123.api.hs.command.CommandHelp(usage = "/spleef help", description = "Shows Spleef help")
	@CommandAliases({ "?" })
	public void execute(CommandSender sender) {
		Map<CommandListener, CommandData[]> commands = HeavySpleef.getInstance().getCommandExecutorService().getCommands();
		List<CommandPair> cmds = new ArrayList<CommandPair>();

		for (Entry<CommandListener, CommandData[]> entry : commands.entrySet()) {
			for (CommandData data : entry.getValue()) {
				cmds.add(new CommandPair(data, entry.getKey()));
			}
		}

		Collections.sort(cmds);

		if (!sender.hasPermission(Permissions.HELP_ADMIN.getPerm()) && !sender.hasPermission(Permissions.HELP_USER.getPerm())) {
			sender.sendMessage(_("noPermission"));
			return;
		}

		sender.sendMessage(ChatColor.GRAY + "   -----   HeavySpleef Help   -----   ");

		for (CommandPair cmd : cmds) {
			if (!cmd.getListener().getClass().isAnnotationPresent(UserType.class)) {
				continue;
			}

			UserType userType = cmd.getListener().getClass().getAnnotation(UserType.class);
			Type type = userType.value();

			boolean isPermitted = false;

			if (type == Type.ADMIN) {
				isPermitted = sender.hasPermission(Permissions.HELP_ADMIN.getPerm());
			}

			if (type == Type.PLAYER) {
				isPermitted = sender.hasPermission(Permissions.HELP_USER.getPerm());
			}

			if (isPermitted) {
				sender.sendMessage(ChatColor.GRAY + "/spleef " + cmd.getData().getName() + ChatColor.RED + " - " + ChatColor.YELLOW + cmd.getData().getDescription());
			}
		}
	}

	private static class CommandPair implements Comparable<CommandPair> {

		private CommandData		data;
		private CommandListener	listener;

		public CommandPair(CommandData data, CommandListener listener) {
			this.data = data;
			this.listener = listener;
		}

		public CommandData getData() {
			return data;
		}

		public CommandListener getListener() {
			return listener;
		}

		@Override
		public int compareTo(CommandPair o) {
			return data.getName().compareTo(o.data.getName());
		}

	}

}
