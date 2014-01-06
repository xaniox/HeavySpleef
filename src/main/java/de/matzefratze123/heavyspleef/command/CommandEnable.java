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
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandEnable extends HSCommand {

	public CommandEnable() {
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ENABLE);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
	
		if (!GameManager.hasGame(args[0].toLowerCase())){
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		if (game.getGameState() != GameState.DISABLED) {
			player.sendMessage(_("gameIsAlreadyEnabled"));
			return;
		}
		
		game.enable();
		game.broadcast(_("gameEnabled", game.getName(), player.getName()), ConfigUtil.getBroadcast(MessageType.GAME_ENABLE));
		player.sendMessage(_("gameEnabledToPlayer", game.getName()));
	
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef enable <Name>");
		help.addHelp("Enables a game");
		
		return help;
	}

}
