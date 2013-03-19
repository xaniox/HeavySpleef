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
package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAutoStart extends HSCommand {

	public CommandAutoStart() {
		setMaxArgs(2);
		setMinArgs(2);
		setPermission(Permissions.SET_AUTOSTART.getPerm());
		setUsage("/spleef autostart <Name> <amount>");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			p.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		try {
			int start = Integer.parseInt(args[1]);
			if (start < 2 && start != 0) {
				p.sendMessage("onlyOverTwoAndZero");
				return;
			}
			
			game.setAutoStart(start);
			p.sendMessage(_("autoStartSet", args[1]));
		} catch (NumberFormatException e) {
			p.sendMessage(_("notANumber", args[1]));
		}
	}

}
