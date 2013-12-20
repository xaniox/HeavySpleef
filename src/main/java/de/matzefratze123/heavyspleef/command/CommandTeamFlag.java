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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandTeamFlag extends HSCommand {
	
	public CommandTeamFlag() {
		setMinArgs(4);
		setPermission(Permissions.SET_TEAMFLAG);
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		Team team = game.getComponents().getTeam(Color.byName(args[1]));
		if (team == null) {
			sender.sendMessage(getUsage());
			return;
		}
		
		boolean clear = args[3].equalsIgnoreCase("clear");
		int number = 0;
		
		try {
			number = Integer.parseInt(args[3]);
		} catch (Exception e) {
			if (!clear) {
				player.sendMessage(_("notANumber", args[3]));
				return;
			}
		}
		
		if (args[2].equalsIgnoreCase("maxplayers")) {
			if (clear) {
				team.setMaxPlayers(0);
				player.sendMessage(_("flagCleared", "maxplayers"));
			} else {
				team.setMaxPlayers(number);
				player.sendMessage(_("flagSet", "maxplayers"));
			}
		} else if (args[2].equalsIgnoreCase("minplayers")) {
			if (clear) {
				team.setMinPlayers(0);
				player.sendMessage(_("flagCleared", "minplayers"));
			} else {
				team.setMinPlayers(number);
				player.sendMessage(_("flagSet", "minplayers"));
			}
		} else player.sendMessage(getUsage());
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef setteamflag <arena> <team> <maxplayers|minplayers> <number>");
		help.addHelp("Adds a flag to a team");
		
		return help;
	}
	
}
