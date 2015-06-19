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
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandKick {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "kick", minArgs = 1, usage = "/spleef kick <player>",
			descref = Messages.Help.Description.KICK,
			permission = Permissions.PERMISSION_KICK)
	public void onKickCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		Player target = context.getPlayer(0);
		String targetName = context.getString(0);
		
		CommandValidate.notNull(target, i18n.getVarString(Messages.Command.PLAYER_NOT_FOUND)
				.setVariable("player", targetName)
				.toString());
		GameManager manager = heavySpleef.getGameManager();
		SpleefPlayer targetSpleefPlayer = heavySpleef.getSpleefPlayer(target);
		
		Game game = manager.getGame(targetSpleefPlayer);
		CommandValidate.notNull(game, i18n.getVarString(Messages.Command.PLAYER_NOT_IN_GAME)
				.setVariable("player", target.getName())
				.toString());
		
		String message = null;
		final int argsLength = context.argsLength();
		
		if (argsLength > 1) {
			StringBuilder builder = new StringBuilder();
			
			for (int i = 0; i < argsLength; i++) {
				builder.append(context.getString(i));
				
				if (i + 1 < argsLength) {
					// Append whitespace
					builder.append(' ');
				}
			}
			
			message = builder.toString();
		}
		
		game.kickPlayer(targetSpleefPlayer, message, sender);
		sender.sendMessage(i18n.getVarString(Messages.Command.PLAYER_KICKED)
				.setVariable("player", target.getName())
				.toString());
	}
	
}
