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

import com.google.common.util.concurrent.FutureCallback;
import de.xaniox.heavyspleef.commands.base.*;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandRename {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "rename", descref = Messages.Help.Description.RENAME,
			permission = Permissions.PERMISSION_RENAME, minArgs = 2,
			usage = "/spleef rename <game> <to>")
	public void onRenameCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		final CommandSender sender = context.getSender() instanceof Player ? 
				heavySpleef.getSpleefPlayer(context.getSender()) : context.getSender();
		
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
	
	@TabComplete("rename")
	public void onRenameTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		GameManager manager = heavySpleef.getGameManager();
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				list.add(game.getName());
			}
		}
	}
	
}