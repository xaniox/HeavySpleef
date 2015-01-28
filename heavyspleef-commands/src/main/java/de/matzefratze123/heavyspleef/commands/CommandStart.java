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
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandStart {
	
	@Command(name = "start", usage = "/spleef start [game]",
			description = "Starts the current game or a given game",
			permission = "heavyspleef.admin.start")
	@PlayerOnly
	public void onStartCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		
		Game game;
		if (context.argsLength() > 0) {
			String gameName = context.getString(0);
			game = manager.getGame(gameName);
			CommandValidate.notNull(game, I18N.getInstance().getVarString(Messages.Command.GAME_DOESNT_EXIST)
					.setVariable("game", gameName)
					.toString());
		} else {
			game = manager.getGame(player);
			CommandValidate.notNull(game, I18N.getInstance().getString(Messages.Command.NOT_INGAME));
		}
		
		game.countdown();
		player.sendMessage(I18N.getInstance().getVarString(Messages.Command.GAME_STARTED)
				.setVariable("game", game.getName())
				.toString());
	}

}
