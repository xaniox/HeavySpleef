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

public class CommandCreate {

	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "create", minArgs = 1, usage = "/spleef create <game>", 
			descref = Messages.Help.Description.CREATE,
			permission = Permissions.PERMISSION_DELETE)
	@PlayerOnly
	public void onCreateCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer sender = heavySpleef.getSpleefPlayer(context.getSender());
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(!manager.hasGame(gameName), i18n.getString(Messages.Command.GAME_ALREADY_EXIST));
		
		Game game = new Game(heavySpleef, gameName, sender.getBukkitPlayer().getWorld());
		manager.addGame(game);
		sender.sendMessage(i18n.getVarString(Messages.Command.GAME_CREATED)
				.setVariable("game", gameName)
				.toString());
	}

}
