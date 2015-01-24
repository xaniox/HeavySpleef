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
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandStop {
	
	@Command(name = "stop", usage = "/spleef stop [game]",
			description = "Stops the current game or the given game",
			permission = "heavyspleef.admin.stop")
	public void onStopCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		GameManager manager = heavySpleef.getGameManager();
		
		Game game;
		if (context.argsLength() > 0) {
			String gameName = context.getString(0);
			game = manager.getGame(gameName);
			CommandValidate.notNull(game, heavySpleef.getVarMessage(Messages.Command.GAME_DOESNT_EXIST)
					.setVariable("game", gameName)
					.toString());
		} else {
			CommandValidate.isTrue(sender instanceof Player, heavySpleef.getMessage(Messages.Command.PLAYER_ONLY));
			SpleefPlayer player = heavySpleef.getSpleefPlayer(sender);
			
			game = manager.getGame(player);
			CommandValidate.notNull(game, heavySpleef.getMessage(Messages.Command.NOT_INGAME));
		}
		
		game.stop();
		
		sender.sendMessage(heavySpleef.getVarMessage(Messages.Command.GAME_STOPPED)
				.setVariable("game", game.getName())
				.toString());
	}
	
}
