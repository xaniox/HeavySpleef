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

import java.io.File;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;

import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Updater;
import de.matzefratze123.heavyspleef.core.Updater.CheckResult;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.i18n.Messages.Player;

public class CommandUpdate {
	
	private final I18N i18n = I18N.getInstance();
	
	@Command(name = "update", permission = "heavyspleef.admin.update",
			usage = "/spleef update", descref = Messages.Help.Description.UPDATE)
	public void onUpdateCommand(CommandContext context, final HeavySpleef heavySpleef) throws CommandException {
		final CommandSender sender = context.getSender();
		
		final Updater updater = heavySpleef.getUpdater();
		final CheckResult result = updater.getResult();
		
		CommandValidate.notNull(result, i18n.getString(Messages.Command.UPDATER_NOT_FINISHED_YET));
		CommandValidate.isTrue(result.isUpdateAvailable(), i18n.getString(Messages.Command.NO_UPDATE_AVAILABLE));
		
		sender.sendMessage(i18n.getString(Messages.Command.STARTING_UPDATE));
		updater.update(sender, new FutureCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				File folder = updater.getUpdateFolder();
				
				sender.sendMessage(i18n.getVarString(Messages.Command.SUCCESSFULLY_PULLED_UPDATE)
						.setVariable("folder", folder.getPath())
						.toString());
				sender.sendMessage(i18n.getString(Messages.Command.RESTART_SERVER_TO_UPDATE));
				
				if (sender instanceof Player) {
					heavySpleef.getLogger().log(Level.INFO, "Successfully pulled the latest version of HeavySpleef into '" + folder.getPath() + "'");
				}
			}
			
			@Override
			public void onFailure(Throwable t) {
				sender.sendMessage(i18n.getString(Messages.Command.ERROR_ON_UPDATING));
				heavySpleef.getLogger().log(Level.SEVERE, "Could not update HeavySpleef to latest version v" + result.getVersion(), t);
			}
		});
	}
	
}
