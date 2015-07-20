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
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.game.QuitCause;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandLeave {

	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "leave", usage = "/spleef leave",
			descref = Messages.Help.Description.LEAVE,
			permission = Permissions.PERMISSION_LEAVE)
	@PlayerOnly
	public void onLeaveCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		GameManager manager = heavySpleef.getGameManager();
		Game game = manager.getGame(player);
		
		if (game != null) {
			game.requestLose(player, QuitCause.SELF);
		} else {
			Game gameUnqueued = null;
			for (Game otherGame : manager.getGames()) {
				if (!otherGame.isQueued(player)) {
					continue;
				}
				
				otherGame.unqueue(player);
				gameUnqueued = otherGame;
				break;
			}
			
			if (gameUnqueued != null) {
				player.sendMessage(i18n.getVarString(Messages.Command.REMOVED_FROM_QUEUE)
						.setVariable("game", gameUnqueued.getName())
						.toString());
			} else {
				throw new CommandException(i18n.getString(Messages.Command.NOT_INGAME));
			}
		}
	}
	
}
