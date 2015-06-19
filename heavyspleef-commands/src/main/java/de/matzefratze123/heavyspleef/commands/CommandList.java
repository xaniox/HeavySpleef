/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.commands;

import java.util.Collection;

import mkremins.fanciful.FancyMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandList {

	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "list", permission = Permissions.PERMISSION_LIST, usage = "/spleef list",
			descref = Messages.Help.Description.LIST)
	public void onListCommand(CommandContext context, HeavySpleef heavySpleef) {
		CommandSender sender = context.getSender();
		GameManager gameManager = heavySpleef.getGameManager();
		Collection<Game> games = gameManager.getGames();
		
		for (Game game : games) {
			FancyMessage message = new FancyMessage("");
			
			if (sender instanceof Player) {
				message.then(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + i18n.getString(Messages.Command.JOIN) + ChatColor.DARK_GRAY + "]")
					.command("/spleef join " + game.getName())
					.tooltip(i18n.getVarString(Messages.Command.CLICK_TO_JOIN)
							.setVariable("game", game.getName())
							.toString());
				message.then(" ");
				
				if (sender.hasPermission(Permissions.PERMISSION_INFO)) {
					message.then(ChatColor.DARK_GRAY + "[" + ChatColor.RED + i18n.getString(Messages.Command.ADMIN_INFO) + ChatColor.DARK_GRAY + "]")
						.command("/spleef info " + game.getName())
						.tooltip(i18n.getString(Messages.Command.SHOW_ADMIN_INFO));
					message.then(" ");
				}
			}
			
			message.then(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + game.getName());
			message.send(sender);
		}
	}
	
}
