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

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.commands.base.TabComplete;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.JoinRequester;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.JoinRequester.JoinValidationException;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandJoin {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "join", minArgs = 1, usage = "/spleef join <game>",
			descref = Messages.Help.Description.JOIN)
	@PlayerOnly
	public void onJoinCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		if (!player.hasPermission(Permissions.PERMISSION_JOIN) && !player.hasPermission(Permissions.PERMISSION_JOIN + "." + game.getName().toLowerCase())) {
			player.sendMessage(i18n.getString(Messages.Command.NO_PERMISSION));
			return;
		}
		
		try {
			long timer = game.getJoinRequester().request(player, JoinRequester.QUEUE_PLAYER_CALLBACK);
			if (timer > 0) {
				player.sendMessage(i18n.getVarString(Messages.Command.JOIN_TIMER_STARTED)
						.setVariable("timer", String.valueOf(timer))
						.toString());
			}
		} catch (JoinValidationException e) {
			player.sendMessage(e.getMessage());
			JoinRequester.QUEUE_PLAYER_CALLBACK.onJoin(player, game, e.getResult());
		}
	}
	
	@TabComplete("join")
	public void onJoinTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				if (game.getGameState() == GameState.DISABLED) {
					continue;
				}
				
				list.add(game.getName());
			}
		}
	}
	
}
