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

import org.bukkit.entity.Player;

import de.matzefratze123.api.command.Command;
import de.matzefratze123.api.command.CommandHelp;
import de.matzefratze123.api.command.CommandListener;
import de.matzefratze123.api.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandRename implements CommandListener {
	
	@Command(value = "rename", minArgs = 2, onlyIngame = true)
	@CommandPermissions(value = {Permissions.RENAME})
	@CommandHelp(usage = "/spleef rename <game> <new-name>", description = "Renames a game")
	public void execute(Player player, Game game, String newName) {
		if (game == null) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		if (GameManager.hasGame(newName)) {
			player.sendMessage(_("arenaAlreadyExists"));
		} else {
			String oldName = game.getName();
			
			game.rename(newName);
			player.sendMessage(_("gameRenamed", oldName, newName));
		}
	}

}
