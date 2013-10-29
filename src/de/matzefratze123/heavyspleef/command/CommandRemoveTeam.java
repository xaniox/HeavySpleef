/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.ADMIN)
public class CommandRemoveTeam extends HSCommand {
	
	public CommandRemoveTeam() {
		setMinArgs(2);
		setUsage("/spleef removeteam <arena> <red|blue|green|yellow|gray>");
		setPermission(Permissions.REMOVE_TEAM);
		setOnlyIngame(true);
		setHelp("Removes a team from a game");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		ChatColor color = null;
		
		for (ChatColor colors : Team.allowedColors) {
			if (colors.name().equalsIgnoreCase(args[1]))
				color = colors;
		}
		
		if (color == null) {
			player.sendMessage(getUsage());
			return;
		}
		
		game.removeTeam(color);
		player.sendMessage(_("teamRemoved", color + Util.toFriendlyString(color.name())));
	}

}
