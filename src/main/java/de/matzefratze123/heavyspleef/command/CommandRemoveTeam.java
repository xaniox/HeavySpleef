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

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandRemoveTeam implements CommandListener {

	@Command(value = "removeteam", minArgs = 2, onlyIngame = true)
	@CommandPermissions(value = { Permissions.REMOVE_TEAM })
	@CommandHelp(usage = "/spleef removeteam <game> <red|blue|green|yellow|gray>", description = "Removes a team from a game")
	public void execute(Player player, Game game, String colorString) {
		if (game == null) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}

		Color color = Color.byName(colorString);
		Team team = game.getComponents().getTeam(color);

		if (team == null) {
			player.sendMessage(ChatColor.RED + "/spleef removeteam <game> <red|blue|green|yellow|gray>");
			return;
		}

		game.getComponents().removeTeam(team);
		player.sendMessage(_("teamRemoved", color.toMessageColorString()));
	}

}
