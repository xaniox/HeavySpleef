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
import de.xaniox.heavyspleef.commands.base.Command;
import de.xaniox.heavyspleef.commands.base.CommandContext;
import de.xaniox.heavyspleef.commands.base.CommandException;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.xaniox.heavyspleef.core.persistence.OperationBatch;
import de.xaniox.heavyspleef.core.persistence.ReadWriteHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.logging.Level;

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

				handler.executeBatch(batch, new SaveOperationCallback<OperationBatch.BatchResult>(heavySpleef, sender));
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