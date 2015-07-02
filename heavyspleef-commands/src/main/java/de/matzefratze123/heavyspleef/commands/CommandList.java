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

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.MinecraftVersion;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

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
	
	private void sendGameEntry(CommandSender sender, Game game) {
		if (MinecraftVersion.isSpigot() && sender instanceof SpleefPlayer) {
			TextComponent comp = new TextComponent("");
			TextComponent openBracket = new TextComponent("[");
			openBracket.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
			TextComponent closeBracket = new TextComponent("]");
			closeBracket.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);

			TextComponent joinComp = new TextComponent(i18n.getString(Messages.Command.JOIN));
			joinComp.setColor(net.md_5.bungee.api.ChatColor.GREEN);
			joinComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(i18n
					.getVarString(Messages.Command.CLICK_TO_JOIN).setVariable("game", game.getName()).toString())));
			joinComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spleef join " + game.getName()));

			comp.addExtra(openBracket.duplicate());
			comp.addExtra(joinComp);
			comp.addExtra(closeBracket.duplicate());
			comp.addExtra(" ");

			if (sender.hasPermission(Permissions.PERMISSION_INFO)) {
				TextComponent infoComp = new TextComponent(i18n.getString(Messages.Command.ADMIN_INFO));
				infoComp.setColor(net.md_5.bungee.api.ChatColor.RED);
				infoComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(i18n
						.getString(Messages.Command.SHOW_ADMIN_INFO))));
				infoComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spleef info " + game.getName()));

				comp.addExtra(openBracket.duplicate());
				comp.addExtra(infoComp);
				comp.addExtra(closeBracket.duplicate());
				comp.addExtra(" ");
			}

			TextComponent dashComp = new TextComponent("-");
			dashComp.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
			TextComponent gameComp = new TextComponent(game.getName());
			gameComp.setColor(net.md_5.bungee.api.ChatColor.GRAY);

			comp.addExtra(dashComp);
			comp.addExtra(" ");
			comp.addExtra(gameComp);

			SpleefPlayer player = (SpleefPlayer) sender;
			player.getBukkitPlayer().spigot().sendMessage(new BaseComponent[] { comp });
		} else {
			// Standard message, no api is available
			sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + game.getName());
		}
	}
	
}
