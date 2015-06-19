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
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

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
		
		boolean success = game.countdown();
		
		if (success) {
			player.sendMessage(i18n.getVarString(Messages.Command.GAME_STARTED)
					.setVariable("game", game.getName())
					.toString());
		}
	}

}
