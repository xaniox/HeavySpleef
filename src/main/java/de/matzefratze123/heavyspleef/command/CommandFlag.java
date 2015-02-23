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
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.BooleanFlag;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.ADMIN)
public class CommandFlag implements CommandListener {

	@SuppressWarnings("rawtypes")
	@Command(value = "flag", minArgs = 2, onlyIngame = true)
	@CommandPermissions(value = { Permissions.SET_FLAG })
	@CommandHelp(usage = "/spleef flag <name> <flag> [state]\n§cAvailable flags: " + FlagType.FRIENDLY_FLAG_LIST, description = "Sets a flag for this game")
	public void execute(CommandSender sender, Game game, Flag fl, String[] values) {
		Player player = (Player) sender;

		if (game == null) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}

		if (fl == null) {
			player.sendMessage(_("invalidFlag"));
			player.sendMessage(ChatColor.RED + "Available flags: " + FlagType.FRIENDLY_FLAG_LIST);
			return;
		}

		Flag<?> flag = fl;

		// Check required flags
		for (Flag<?> required : flag.getRequiredFlags()) {
			if (!game.hasFlag(required) || (required instanceof BooleanFlag && !(Boolean) game.getFlag(required))) {
				List<String> flagNames = new ArrayList<String>();
				for (Flag<?> f : flag.getRequiredFlags()) {
					flagNames.add(f.getName());
				}

				player.sendMessage(_("requiringFlags", Util.toFriendlyString(flagNames, ", ")));
				return;
			}
		}

		// Check conflicting flags
		for (Flag<?> conflicting : flag.getConflictingFlags()) {
			if (game.hasFlag(conflicting) || (conflicting instanceof BooleanFlag && (Boolean) game.getFlag(conflicting))) {
				List<String> flagNames = new ArrayList<String>();
				for (Flag<?> f : flag.getConflictingFlags()) {
					flagNames.add(f.getName());
				}

				player.sendMessage(_("conflictingFlags", Util.toFriendlyString(flagNames, ", ")));
				return;
			}
		}

		StringBuilder buildArgs = new StringBuilder();
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				buildArgs.append(values[i]);

				if (i + 1 < values.length) {
					buildArgs.append(" ");
				}
			}
		}

		if (values != null && values[0].equalsIgnoreCase("clear")) {
			game.setFlag(flag, null);
			player.sendMessage(_("flagCleared", flag.getName()));
			return;
		}

		try {
			Object previousValue = game.getFlag(flag);

			Object value = flag.parse(player, buildArgs.toString(), previousValue);

			if (value == null) {
				player.sendMessage(_("invalidFlagFormat"));
				player.sendMessage(flag.getHelp());
				return;
			}

			setFlag(game, flag, value);
			player.sendMessage(_("flagSet", flag.getName()));
		} catch (Exception e) {
			player.sendMessage(_("invalidFlagFormat"));
			player.sendMessage(flag.getHelp());
		}
	}

	@SuppressWarnings("unchecked")
	public <V> void setFlag(Game game, Flag<V> flag, Object value) {
		game.setFlag(flag, (V) value);
	}

}
