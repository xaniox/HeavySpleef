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

import org.bukkit.command.CommandSender;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandEnable implements CommandListener {

	@Command(value = "enable", minArgs = 1)
	@CommandPermissions(value = {Permissions.ENABLE})
	@CommandHelp(usage = "/spleef enable <game>", description = "Enables a game.")
	public void execute(CommandSender sender, Game game) {
		if (game == null) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		if (game.getGameState() != GameState.DISABLED) {
			sender.sendMessage(_("gameIsAlreadyEnabled"));
			return;
		}
		
		game.enable();
		game.broadcast(_("gameEnabled", game.getName(), sender.getName()), ConfigUtil.getBroadcast(MessageType.GAME_ENABLE));
		sender.sendMessage(_("gameEnabledToPlayer", game.getName()));
	
	}

}
