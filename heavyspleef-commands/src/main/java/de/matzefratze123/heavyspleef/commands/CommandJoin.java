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

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game.JoinResult;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandJoin {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "join", minArgs = 1, usage = "/spleef join <game> [args]",
			descref = Messages.Help.Description.JOIN,
			permission = "heavyspleef.join")
	@PlayerOnly
	public void onJoinCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		CommandValidate.isTrue(game.getGameState().isGameEnabled(), i18n.getVarString(Messages.Command.GAME_JOIN_IS_DISABLED)
				.setVariable("game", gameName)
				.toString());
		
		CommandValidate.isTrue(!game.getGameState().isGameActive(), i18n.getVarString(Messages.Command.GAME_IS_INGAME)
				.setVariable("game", gameName)
				.toString());
		
		CommandValidate.isTrue(manager.getGame(player) == null, i18n.getString(Messages.Command.ALREADY_PLAYING));
		
		JoinResult result = game.join(player);
		if (result == JoinResult.TEMPORARY_DENY) {
			//Remove the player from all other queues
			for (Game otherGame : manager.getGames()) {
				otherGame.unqueue(player);
			}
			
			//Queue the player
			boolean success = game.queue(player);
			
			if (success) {
				player.sendMessage(i18n.getVarString(Messages.Command.ADDED_TO_QUEUE)
						.setVariable("game", game.getName())
						.toString());
			} else {
				player.sendMessage(i18n.getString(Messages.Command.COULD_NOT_ADD_TO_QUEUE));
			}
			
		}
	}
	
}
