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

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.TabComplete;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.game.GameState;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandEnable {

	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "enable", minArgs = 1, usage = "/spleef enable <game>",
			descref = Messages.Help.Description.ENABLE,
			permission = Permissions.PERMISSION_ENABLE)
	public void onEnableCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		String gameName = context.getString(0);
		
		GameManager manager = heavySpleef.getGameManager();
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		CommandValidate.isTrue(!game.getGameState().isGameEnabled(), i18n.getVarString(Messages.Command.GAME_ALREADY_ENABLED)
				.setVariable("game", gameName)
				.toString());
		
		game.enable();
		sender.sendMessage(i18n.getVarString(Messages.Command.GAME_ENABLED)
				.setVariable("game", gameName)
				.toString());
		
		heavySpleef.getDatabaseHandler().saveGame(game, null);
	}
	
	@TabComplete("enable")
	public void onEnableTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				if (game.getGameState() != GameState.DISABLED) {
					continue;
				}
				
				list.add(game.getName());
			}
		}
	}
	
}
