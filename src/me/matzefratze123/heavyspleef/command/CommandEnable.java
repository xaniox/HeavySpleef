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
import me.matzefratze123.heavyspleef.core.GameState;
import me.matzefratze123.heavyspleef.util.Permissions;
import me.matzefratze123.heavyspleef.util.ViPManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandEnable extends HSCommand {

	public CommandEnable() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ENABLE);
		setUsage("/spleef enable <Name>");
		setTabHelp(new String[]{"<name>"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())){
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		if (!game.isDisabled()) {
			player.sendMessage(_("gameIsAlreadyEnabled"));
			return;
		}
		game.broadcast(_("gameEnabled", game.getName(), ViPManager.colorName(player.getName())));
		game.setGameState(GameState.JOINABLE);
		player.sendMessage(_("gameEnabledToPlayer", game.getName()));
	}

}
