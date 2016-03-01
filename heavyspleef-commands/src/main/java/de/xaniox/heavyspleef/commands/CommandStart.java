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

import de.xaniox.heavyspleef.commands.base.*;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;

import java.util.List;

public class CommandStart {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "start", usage = "/spleef start [game]",
			descref = Messages.Help.Description.START,
			permission = Permissions.PERMISSION_START)
	@PlayerOnly
	public void onStartCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		
		Game game;
		if (context.argsLength() > 0) {
			String gameName = context.getString(0);
			game = manager.getGame(gameName);
			CommandValidate.notNull(game, i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
					.setVariable("game", gameName)
					.toString());
		} else {
			game = manager.getGame(player);
			CommandValidate.notNull(game, i18n.getString(Messages.Command.NOT_INGAME));
		}
		
		if (game.getGameState().isGameActive()) {
			throw new CommandException(i18n.getString(Messages.Command.GAME_IS_INGAME));
		}
		
		boolean success = game.countdown();
		
		if (success) {
			player.sendMessage(i18n.getVarString(Messages.Command.GAME_STARTED)
					.setVariable("game", game.getName())
					.toString());
		}
	}
	
	@TabComplete("start")
	public void onStartTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				if (game.getGameState().isGameActive()) {
					continue;
				}
				
				list.add(game.getName());
			}
		}
	}

}