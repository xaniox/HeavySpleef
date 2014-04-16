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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.ADMIN)
public class CommandList implements CommandListener {

	@Command(value = "list", onlyIngame = true)
	@CommandPermissions(value = { Permissions.LIST })
	@CommandHelp(usage = "/spleef list <game>", description = "Lists all spleef games or lists ingame players in an game")
	public void execute(Player bukkitPlayer, Game game) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(bukkitPlayer);

		if (game == null) {
			if (player.isActive()) {
				Game g = player.getGame();
				printList(g, player);
			} else {
				List<Game> games = GameManager.getGames();
				Set<String> gameNameList = new HashSet<String>();

				for (Game g : games) {
					gameNameList.add(g.getName());
				}

				player.sendMessage(ChatColor.GRAY + "All games: " + Util.toFriendlyString(gameNameList, ", "));
			}
		} else {
			printList(game, player);
		}
	}

	private void printList(Game game, SpleefPlayer player) {
		List<SpleefPlayer> active = game.getIngamePlayers();
		List<OfflinePlayer> out = game.getOutPlayers();

		Set<String> activeString = new HashSet<String>();
		Set<String> outString = new HashSet<String>();

		for (SpleefPlayer activePlayer : active) {
			activeString.add(activePlayer.getName() + ChatColor.AQUA);
		}

		for (OfflinePlayer outPlayer : out) {
			outString.add(outPlayer.getName());
		}

		player.sendMessage(ChatColor.AQUA + "Active: " + Util.toFriendlyString(activeString, ", "));
		player.sendMessage(ChatColor.RED + "Out: " + Util.toFriendlyString(outString, ", "));
	}

}
