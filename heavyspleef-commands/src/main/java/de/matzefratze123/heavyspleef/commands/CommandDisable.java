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

import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandDisable {

	@Command(name = "disable", minArgs = 1, usage = "/spleef disable <game>",
			description = "Disables a game with the given name",
			permission = "heavyspleef.admin.disable")
	public void onDisableCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), heavySpleef.getVarMessage(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		CommandValidate.isTrue(game.getGameState() != GameState.DISABLED, heavySpleef.getVarMessage(Messages.Command.GAME_ALREADY_DISABLED)
				.setVariable("game", gameName)
				.toString());
		game.disable();
		
		sender.sendMessage(heavySpleef.getVarMessage(Messages.Command.GAME_DISABLED)
				.setVariable("game", gameName)
				.toString());
	}
	
}
