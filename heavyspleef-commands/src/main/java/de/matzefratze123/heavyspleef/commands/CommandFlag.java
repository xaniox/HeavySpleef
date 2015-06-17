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

import java.util.Iterator;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.collection.DualKeyBiMap;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.flag.NullFlag;
import de.matzefratze123.heavyspleef.core.flag.ValidationException;
import de.matzefratze123.heavyspleef.core.hook.Hook;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandFlag {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@SuppressWarnings("unchecked")
	@Command(name = "flag", usage = "/spleef flag <game> <flag> [flag-value|remove]", 
			descref = Messages.Help.Description.FLAG,
			permission = "heavyspleef.flag")
	@PlayerOnly
	public <T> void onFlagCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		FlagRegistry registry = heavySpleef.getFlagRegistry();
		
		if (context.argsLength() < 2) {
			//Print some fancy information about available flags
			printAvailableFlags(registry, player, heavySpleef.getSpleefPrefix());
			return;
		}
		
		GameManager manager = heavySpleef.getGameManager();
		String gameName = context.getString(0);
		
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		String flagPath = context.getString(1);
		String removeIdentifier = context.getStringSafely(2);
		Class<? extends AbstractFlag<?>> flagClass = registry.getFlagClass(flagPath);
		
		CommandValidate.notNull(flagClass, i18n.getVarString(Messages.Command.FLAG_DOESNT_EXIST)
				.setVariable("flag", flagPath)
				.toString());
		
		if (context.argsLength() > 2 && (removeIdentifier.equalsIgnoreCase("remove") || removeIdentifier.equalsIgnoreCase("clear"))) {
			CommandValidate.isTrue(game.isFlagPresent(flagPath), i18n.getVarString(Messages.Command.FLAG_NOT_PRESENT)
					.setVariable("flag", flagPath)
					.toString());
			
			game.removeFlag(flagClass);
			player.sendMessage(i18n.getVarString(Messages.Command.FLAG_REMOVED)
					.setVariable("flag", flagPath)
					.toString());
		} else {
			//Parse value
			StringBuilder inputBuilder = new StringBuilder();
			for (int i = 2; i < context.argsLength(); i++) {
				inputBuilder.append(context.getString(i));
				
				if (i + 1 < context.argsLength()) {
					inputBuilder.append(' ');
				}
			}
			
			Flag flagData = registry.getFlagData(flagClass);
			
			HookReference[] references = flagData.depend();
			HookManager hookManager = heavySpleef.getHookManager();
			
			for (HookReference ref : references) {
				Hook hook = hookManager.getHook(ref);
				
				CommandValidate.isTrue(hook.isProvided(), i18n.getVarString(Messages.Command.FLAG_REQUIRES_HOOK)
						.setVariable("hook", hook.getName())
						.toString());
			}
			
			AbstractFlag<Object> flag;
			boolean existingFlag = false;
			
			if (game.isFlagPresent(flagClass)) {
				flag = (AbstractFlag<Object>) game.getFlag(flagClass);
				existingFlag = true;
			} else {
				flag = (AbstractFlag<Object>) registry.newFlagInstance(flagPath, flagClass, game);
			}
			
			Object value = null;
			String extraMessage = null;
			
			try {
				value = flag.parseInput(player, inputBuilder.toString());
				flag.validateInput(value);
			} catch (InputParseException e) {
				String message = e.getMessage();
				
				if (!flagData.ignoreParseException()) {
					String malformedInput = e.getMalformedInput();
					message = message != null ? message : i18n.getString(Messages.Command.INVALID_FLAG_INPUT);
					
					throw new CommandException(message + (malformedInput != null ? ": " + malformedInput : ""));
				} else if (!e.getMessage().isEmpty()){
					extraMessage = message;
				}
			} catch (ValidationException e) {
				throw new CommandException(e.getMessage());
			}
			
			validateFlagParents(flagData, game);
			
			if (!existingFlag) {
				game.addFlag(flag);
			}
			
			if (value != null || NullFlag.class.isAssignableFrom(flagClass)) {
				flag.setValue(value);
			}
			
			player.sendMessage(i18n.getVarString(Messages.Command.FLAG_SET)
					.setVariable("flag", flagData.name())
					.toString());
			
			if (extraMessage != null) {
				player.sendMessage(extraMessage);
			}
		}
		
		heavySpleef.getDatabaseHandler().saveGame(game, null);
	}
	
	private void printAvailableFlags(FlagRegistry registry, CommandSender sender, String prefix) {
		DualKeyBiMap<String, Flag, Class<? extends AbstractFlag<?>>> flags = registry.getAvailableFlags();
		Set<String> flagNames = Sets.newTreeSet();
		
		for (String flagPath : flags.primaryKeySet()) {
			flagNames.add(flagPath);
		}
		
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterator = flagNames.iterator();
		
		while (iterator.hasNext()) {
			String name = iterator.next();
			
			builder.append(name);
			
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		
		sender.sendMessage(prefix + i18n.getVarString(Messages.Player.AVAILABLE_FLAGS)
				.setVariable("flags", builder.toString())
				.toString());
	}
	
	private void validateFlagParents(Flag flagData, Game game) throws CommandException {
		Class<? extends AbstractFlag<?>> parentClass;
		Flag parentFlagData = flagData;
		
		while (true) {
			parentClass = parentFlagData.parent();
			
			if (parentClass != NullFlag.class) {
				parentFlagData = parentClass.getAnnotation(Flag.class);
				String parentName = parentFlagData.name();
				
				if (!game.isFlagPresent(parentName)) {
					throw new CommandException(i18n.getVarString(Messages.Command.PARENT_FLAG_NOT_SET)
							.setVariable("parent-flag", parentName)
							.toString());
				}
			} else {
				return;
			}
		}
	}

}