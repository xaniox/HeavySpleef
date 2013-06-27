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
import org.bukkit.entity.Player;

public class CommandStart extends HSCommand {

	public CommandStart() {
		setOnlyIngame(true);
		setPermission(Permissions.START_GAME);
		setUsage("/spleef start <Name>");
		setTabHelp(new String[]{"<name>"});
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Game game;
		
		if (args.length <= 0) {
			if (!GameManager.isInAnyGame(player)) {
				player.sendMessage(_("notIngame"));
				return;
			}
			
			game = GameManager.fromPlayer(player);
		} else {
			if (!GameManager.hasGame(args[0].toLowerCase())) {
				sender.sendMessage(_("arenaDoesntExists"));
				return;
			}
			
			permissionsCheck: {
				Game playerGame = GameManager.fromPlayer(player);
				
				if (playerGame != null && playerGame.getName().equalsIgnoreCase(args[0]))
					break permissionsCheck;
				if (player.hasPermission(Permissions.START_GAME_OTHER.getPerm()))
					break permissionsCheck;
				
				player.sendMessage(_("noPermission"));
				return;
			}
			
			game = GameManager.getGame(args[0]);
		}
		
		start(player, game);
	}

	public static void start(Player player, Game game) {
		if (!game.isAbleToStart(player))
			return;
		
		game.countdown();
		player.sendMessage(_("gameStarted"));
	}
	
}
