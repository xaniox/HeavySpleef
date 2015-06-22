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
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandStop {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "stop", usage = "/spleef stop [game]",
			descref = Messages.Help.Description.STOP,
			permission = Permissions.PERMISSION_STOP)
	public void onStopCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		GameManager manager = heavySpleef.getGameManager();
		
		Game game;
		if (context.argsLength() > 0) {
			String gameName = context.getString(0);
			game = manager.getGame(gameName);
			CommandValidate.notNull(game, i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
					.setVariable("game", gameName)
					.toString());
		} else {
			CommandValidate.isTrue(sender instanceof Player, i18n.getString(Messages.Command.PLAYER_ONLY));
			SpleefPlayer player = heavySpleef.getSpleefPlayer(sender);
			
			game = manager.getGame(player);
			CommandValidate.notNull(game, i18n.getString(Messages.Command.NOT_INGAME));
		}
		
		game.stop();
		
		sender.sendMessage(i18n.getVarString(Messages.Command.GAME_STOPPED)
				.setVariable("game", game.getName())
				.toString());
	}
	
	@TabComplete("stop")
	public void onStopTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				if (!game.getGameState().isGameActive()) {
					continue;
				}
				
				list.add(game.getName());
			}
		}
	}
	
}
