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

import static de.matzefratze123.heavyspleef.util.I18N._;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.ADMIN)
public class CommandInfo implements CommandListener {
	
	@Command(value = "info", minArgs = 1)
	@CommandPermissions(value = {Permissions.INFO})
	@CommandHelp(usage = "/spleef info <game>", description = "Prints out information about this game")
	public void execute(CommandSender sender, Game game) {
		if (game == null) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Name: " + game.getName() + ChatColor.GRAY + ", type: " + game.getType().name());
		if (game.getFlags().size() > 0) {
			sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Flags: " + ChatColor.BLUE + getFriendlyFlagInfo(game));
		}
		
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Floors:");
		for (IFloor floor : game.getComponents().getFloors()) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "# " + floor.asPlayerInfo());
		}
		
		sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Losezones:");
		for (LoseZone zone : game.getComponents().getLoseZones()) {
			sender.sendMessage(ChatColor.YELLOW + "# " + zone.asInfo());
		}
	}
	
	private String getFriendlyFlagInfo(Game game) {
		Map<Flag<?>, Object> flags = game.getFlags();
		String[] info = new String[flags.size()];
		
		int i = 0;
		for (Flag<?> flag : flags.keySet()) {
			info[i] = flag.toInfo(flags.get(flag));
			i++;
		}
		
		return Util.toFriendlyString(info, ", ");
	}

}
