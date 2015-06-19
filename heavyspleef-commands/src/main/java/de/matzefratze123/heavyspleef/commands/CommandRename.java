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

import com.google.common.util.concurrent.FutureCallback;

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

public class CommandRename {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "rename", descref = Messages.Help.Description.RENAME,
			permission = Permissions.PERMISSION_RENAME, minArgs = 2,
			usage = "/spleef rename <game> <to>")
	public void onRenameCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		final CommandSender sender = context.getSender();
		final String from = context.getString(0);
		final String to = context.getString(1);
		
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(from), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", from)
				.toString());
		CommandValidate.isTrue(!manager.hasGame(to), i18n.getString(Messages.Command.GAME_ALREADY_EXIST));
		
		Game game = manager.getGame(from);
		final String oldName = game.getName();
		
		manager.renameGame(game, to, new FutureCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				sender.sendMessage(i18n.getVarString(Messages.Command.GAME_RENAMED)
						.setVariable("from", oldName)
						.setVariable("to", to)
						.toString());
			}
			
			@Override
			public void onFailure(Throwable t) {
				sender.sendMessage(i18n.getVarString(Messages.Command.ERROR_ON_SAVE)
						.setVariable("detail-message", t.toString())
						.toString());
			}
		});
		
	}
	
}
