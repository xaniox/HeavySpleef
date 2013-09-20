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


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.util.Permissions;

public class CommandStart extends HSCommand {

	public CommandStart() {
		setPermission(Permissions.START_GAME);
		setUsage("/spleef start <Name>");
		setTabHelp(new String[]{"<name>"});
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Game game;
		
		if (args.length <= 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(_("onlyIngame"));
				return;
			}
			
			Player player = (Player)sender;
			
			if (!GameManager.isActive(player)) {
				player.sendMessage(_("notIngame"));
				return;
			}
			
			game = GameManager.fromPlayer(player);
		} else {
			if (!GameManager.hasGame(args[0].toLowerCase())) {
				sender.sendMessage(_("arenaDoesntExists"));
				return;
			}
			
			if (sender instanceof Player) {
				Player player = (Player)sender;
				
				permissionsCheck: {
					Game playerGame = GameManager.fromPlayer(player);
					
					if (playerGame != null && playerGame.getName().equalsIgnoreCase(args[0]))
						break permissionsCheck;
					if (sender.hasPermission(Permissions.START_GAME_OTHER.getPerm()))
						break permissionsCheck;
					
					sender.sendMessage(_("noPermission"));
					return;
				}
			}
			
			game = GameManager.getGame(args[0]);
		}
		
		start(sender, game);
	}

	public static void start(CommandSender sender, Game game) {
		if (!game.isAbleToStart(sender))
			return;
		
		game.countdown();
		sender.sendMessage(_("gameStarted"));
	}
	
}
