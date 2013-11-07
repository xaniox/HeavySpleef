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

import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.ViPManager;

@UserType(Type.ADMIN)
public class CommandDisable extends HSCommand {

	public CommandDisable() {
		setMinArgs(0);
		setPermission(Permissions.DISABLE);
		setUsage("/spleef disable [Name]");
		setOnlyIngame(true);
		setHelp("Disables a game");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (args.length > 0) {
			if (!GameManager.hasGame(args[0].toLowerCase())) {
				player.sendMessage(_("arenaDoesntExists"));
				return;
			}
			
			Game game = GameManager.getGame(args[0]);
			
			if (game.getGameState() == GameState.DISABLED) {
				player.sendMessage(_("gameIsAlreadyDisabled"));
				return;
			}
			
			game.disable();
			game.broadcast(_("gameDisabled", game.getName(), ViPManager.colorName(player.getName())), ConfigUtil.getBroadcast("game-disable"));
			player.sendMessage(_("gameDisabledToPlayer", game.getName()));
		} else if (args.length == 0) {
			for (Game game : GameManager.getGames()) {
				if (game.getGameState() == GameState.DISABLED)
					continue;
				
				game.disable();
				game.broadcast(_("gameDisabled", game.getName(), ViPManager.colorName(player.getName())), ConfigUtil.getBroadcast("game-disable"));
			}
			
			player.sendMessage(_("allGamesDisabledToPlayer"));
		}
	}

}
