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

import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.persistence.OperationBatch;
import de.matzefratze123.heavyspleef.core.persistence.OperationBatch.BatchResult;
import de.matzefratze123.heavyspleef.core.persistence.ReadWriteHandler;

public class CommandSave {

	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "save", permission = Permissions.PERMISSION_SAVE,
			usage = "/spleef save [games|statistics|all]", descref = Messages.Help.Description.SAVE)
	public void onSaveCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		SaveOperation operation = SaveOperation.ALL;
		if (context.argsLength() > 0) {
			String opCode = context.getString(0);
			
			if ((operation = SaveOperation.valueOfSafe(opCode)) == null) {
				operation = SaveOperation.ALL;
			}
		}
		
		AsyncReadWriteHandler handler = heavySpleef.getDatabaseHandler();
		sender.sendMessage(i18n.getString(Messages.Command.SAVING_DATA));
		operation.complete(handler, heavySpleef, sender);
	}
	
	private enum SaveOperation {
		
		GAMES(Messages.Command.GAMES_SAVED) {
			@Override
			public void complete(AsyncReadWriteHandler handler, HeavySpleef heavySpleef, CommandSender sender) {
				GameManager manager = heavySpleef.getGameManager();
				handler.saveGames(manager.getGames(), new SaveOperationCallback<Void>(heavySpleef, sender));
				
			}
		},
		STATISTICS(Messages.Command.STATISTICS_SAVED) {
			@Override
			public void complete(AsyncReadWriteHandler handler, HeavySpleef heavySpleef, CommandSender sender) {
				handler.forceCacheSave(new SaveOperationCallback<Void>(heavySpleef, sender));
			}
		},
		ALL(Messages.Command.EVERYTHING_SAVED) {
			@Override
			public void complete(AsyncReadWriteHandler handler, HeavySpleef heavySpleef, CommandSender sender) {
				GameManager manager = heavySpleef.getGameManager();
				final Collection<Game> games = manager.getGames();
				
				OperationBatch batch = new OperationBatch() {
					
					@Override
					public void executeBatch(ReadWriteHandler handler, BatchResult resultWriter) throws Exception {
						handler.saveGames(games);
						handler.forceCacheSave();
					}
				};

				handler.executeBatch(batch, new SaveOperationCallback<BatchResult>(heavySpleef, sender));
			}
		};
		
		private String successMessageKey;
		
		private SaveOperation(String successMessageKey) {
			this.successMessageKey = successMessageKey;
		}
		
		public abstract void complete(AsyncReadWriteHandler handler, HeavySpleef heavySpleef, CommandSender sender);
		
		public static SaveOperation valueOfSafe(String opCode) {
			for (SaveOperation op : values()) {
				if (op.name().equalsIgnoreCase(opCode)) {
					return op;
				}
			}
			
			return null;
		}
		
		public class SaveOperationCallback<T> implements FutureCallback<T> {

			private final I18N i18n = I18NManager.getGlobal();
			private HeavySpleef heavySpleef;
			private CommandSender receiver;
			
			public SaveOperationCallback(HeavySpleef heavySpleef, CommandSender receiver) {
				this.heavySpleef = heavySpleef;
				this.receiver = receiver;
			}
			
			@Override
			public void onSuccess(T result) {
				receiver.sendMessage(i18n.getString(successMessageKey));
			}

			@Override
			public void onFailure(Throwable t) {
				receiver.sendMessage(i18n.getVarString(Messages.Command.ERROR_ON_SAVE)
						.setVariable("detail-message", t.toString())
						.toString());
				heavySpleef.getLogger().log(Level.SEVERE, "Exception occured while saving data: ", t);
			}
			
		}
		
	}
	
}
