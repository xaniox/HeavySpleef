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
import me.matzefratze123.heavyspleef.util.Permissions;

import org.bukkit.command.CommandSender;

public class CommandDelete extends HSCommand {

	public CommandDelete() {
		setMaxArgs(1);
		setMinArgs(1);
		setPermission(Permissions.DELETE_GAME);
		setUsage("/spleef delete <name>");
		setTabHelp(new String[]{"<name>"});
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		if (game.isIngame() || game.isCounting() || game.isPreLobby()) {
			sender.sendMessage(_("cantDeleteGameWhileIngame"));
			return;
		}
		
		game.removeAllFromQueue();
		GameManager.deleteGame(args[0].toLowerCase());
		sender.sendMessage(_("gameDeleted"));
	}

}
