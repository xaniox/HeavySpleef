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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.matzefratze123.api.command.Command;
import de.matzefratze123.api.command.CommandHelp;
import de.matzefratze123.api.command.CommandListener;
import de.matzefratze123.api.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandTeamFlag implements CommandListener {
	
	@Command(value = "teamflag", minArgs = 4, onlyIngame = true)
	@CommandPermissions(value = {Permissions.SET_TEAMFLAG})
	@CommandHelp(usage = "/spleef setteamflag <game> <team> <maxplayers|minplayers> <number>", description = "Adds a flag to a team")
	public void execute(Player player, Game game, String color, String flag, String value) {
		if (game == null) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Team team = game.getComponents().getTeam(Color.byName(color));
		if (team == null) {
			player.sendMessage(ChatColor.RED + "This team color doesn't exists!");
			return;
		}
		
		boolean clear = value.equalsIgnoreCase("clear");
		int number = 0;
		
		try {
			number = Integer.parseInt(value);
		} catch (Exception e) {
			if (!clear) {
				player.sendMessage(_("notANumber", value));
				return;
			}
		}
		
		if (flag.equalsIgnoreCase("maxplayers")) {
			if (clear) {
				team.setMaxPlayers(0);
				player.sendMessage(_("flagCleared", "maxplayers"));
			} else {
				team.setMaxPlayers(number);
				player.sendMessage(_("flagSet", "maxplayers"));
			}
		} else if (flag.equalsIgnoreCase("minplayers")) {
			if (clear) {
				team.setMinPlayers(0);
				player.sendMessage(_("flagCleared", "minplayers"));
			} else {
				team.setMinPlayers(number);
				player.sendMessage(_("flagSet", "minplayers"));
			}
		} else player.sendMessage(ChatColor.RED + "Flag doesn't exists!");
	}
	
}
