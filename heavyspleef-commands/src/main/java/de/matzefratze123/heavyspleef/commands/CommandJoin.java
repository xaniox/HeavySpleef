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

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.internal.Command;
import de.matzefratze123.heavyspleef.commands.internal.CommandContext;
import de.matzefratze123.heavyspleef.commands.internal.CommandException;
import de.matzefratze123.heavyspleef.commands.internal.CommandValidate;
import de.matzefratze123.heavyspleef.commands.internal.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandJoin {
	
	@Command(name = "join", minArgs = 1, usage = "/spleef join <game>",
			description = "Joins a game with the given name",
			permission = "heavyspleef.join")
	@PlayerOnly
	public void onJoinCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), heavySpleef.getMessage(Messages.Command.GAME_DOESNT_EXIST));
		Game game = manager.getGame(gameName);
		
		CommandValidate.isTrue(game.getGameState().isGameEnabled(), heavySpleef.getMessage(Messages.Command.GAME_JOIN_IS_DISABLED));
		
		SpleefPlayer spleefPlayer = heavySpleef.getSpleefPlayer(player);
		game.join(spleefPlayer);
	}
	
}
