/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.commands;

import de.xaniox.heavyspleef.commands.base.Command;
import de.xaniox.heavyspleef.commands.base.CommandContext;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.MinecraftVersion;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandList {

	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "list", permission = Permissions.PERMISSION_LIST, usage = "/spleef list",
			descref = Messages.Help.Description.LIST)
	public void onListCommand(CommandContext context, HeavySpleef heavySpleef) {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		GameManager gameManager = heavySpleef.getGameManager();
		Collection<Game> games = gameManager.getGames();
		
		if (games.isEmpty()) {
			sender.sendMessage(i18n.getString(Messages.Command.NO_GAMES_EXIST));
			return;
		}
		
		for (Game game : games) {
			sendGameEntry(sender, game);
		}
	}
	
	public static void sendGameEntry(CommandSender sender, Game game) {
		I18N i18n = I18NManager.getGlobal();
		
		if (MinecraftVersion.isSpigot() && sender instanceof SpleefPlayer) {
			ComponentBuilder builder = new ComponentBuilder("[")
					.color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
				.append(i18n.getString(Messages.Command.JOIN))
					.color(net.md_5.bungee.api.ChatColor.GREEN)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(i18n
							.getVarString(Messages.Command.CLICK_TO_JOIN).setVariable("game", game.getName()).toString())))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spleef join " + game.getName()))
				.append("]")
					.color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
				.append(" ");

			if (sender.hasPermission(Permissions.PERMISSION_INFO)) {
				builder.append("[")
						.color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
					.append(i18n.getString(Messages.Command.ADMIN_INFO))
						.color(net.md_5.bungee.api.ChatColor.RED)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(i18n
								.getString(Messages.Command.SHOW_ADMIN_INFO))))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spleef info " + game.getName()))
					.append("]")
						.color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
					.append(" ");
			}

			builder.append("-")
					.color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
				.append(" ")
				.append(game.getName())
					.color(net.md_5.bungee.api.ChatColor.GRAY);
			
			SpleefPlayer player = (SpleefPlayer) sender;
			player.getBukkitPlayer().spigot().sendMessage(builder.create());
		} else {
			// Standard message, no api is available
			sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + game.getName());
		}
	}
	
}