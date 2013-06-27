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

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Team;
import me.matzefratze123.heavyspleef.core.flag.FlagType;
import me.matzefratze123.heavyspleef.util.LanguageHandler;
import me.matzefratze123.heavyspleef.util.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandJoin extends HSCommand {
	
	public CommandJoin() {
		setMaxArgs(2);
		setMinArgs(0);
		setOnlyIngame(true);
		setUsage("/spleef join <arena> [team]");
		setTabHelp(new String[]{"<arena>", "<arena> [team]"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (args.length == 0) {
			if (!player.hasPermission(Permissions.JOIN_GAME_INV.getPerm())) {
				player.sendMessage(getUsage());
				return;
			}
			
			HeavySpleef.instance.getInventoryMenu().open(player);
			return;
		}
		
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		if (!player.hasPermission(Permissions.JOIN_GAME.getPerm())) {
			if (!player.hasPermission(Permissions.JOIN_GAME.getPerm() + "." + game.getName().toLowerCase())) {
				player.sendMessage(LanguageHandler._("noPermission"));
				return;
			}
		}
		
		if (args.length == 1) {
			if (game.getFlag(FlagType.TEAM)) {
				player.sendMessage(_("specifieTeam", game.getTeamColors().toString()));
				return;
			}
			
			game.addPlayer(player, null);
		} else if (args.length == 2) {
			if (!game.getFlag(FlagType.TEAM)) {
				game.addPlayer(player, null);
				return;
			}
			
			ChatColor color = null;
			
			for (ChatColor colors : Team.allowedColors) {
				if (colors.name().equalsIgnoreCase(args[1]))
					color = colors;
			}
			
			if (color == null) {
				player.sendMessage(getUsage());
				return;
			}
			
			game.addPlayer(player, color);
		}
	}

}
