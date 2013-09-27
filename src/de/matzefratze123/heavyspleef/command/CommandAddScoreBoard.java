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


import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.util.Permissions;

public class CommandAddScoreBoard extends HSCommand {

	public CommandAddScoreBoard() {
		setMinArgs(2);
		setMaxArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_SCOREBOARD);
		setUsage("/spleef addscoreboard <name> <EAST|WEST|SOUTH|NORTH>");
		setTabHelp(new String[]{"<name> <EAST|WEST|SOUTH|NORTH>"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		args[1] = args[1].toUpperCase();
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		if (!args[1].equalsIgnoreCase("EAST") && !args[1].equalsIgnoreCase("WEST") && !args[1].equalsIgnoreCase("NORTH") && !args[1].equalsIgnoreCase("SOUTH")) {
			p.sendMessage(_("invalidBlockFace"));
			return;
		}
			
		BlockFace face = null;
		
		try {
			face = BlockFace.valueOf(args[1]);
		} catch (NullPointerException npe) {}
		
		if (face == null) {
			p.sendMessage(_("invalidBlockFace"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		game.addScoreBoard(p.getLocation(), face);
		p.sendMessage(_("scoreBoardAdded"));
	}

}
