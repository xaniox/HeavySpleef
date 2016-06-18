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

import com.google.common.collect.Sets;
import de.xaniox.heavyspleef.commands.base.*;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.MinecraftVersion;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.Updater;
import de.xaniox.heavyspleef.core.collection.DualKeyBiMap;
import de.xaniox.heavyspleef.core.flag.*;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.hook.Hook;
import de.xaniox.heavyspleef.core.hook.HookManager;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CommandFlag {
	
	private static final String REMOVE_IDENTIFIER = "remove";
	private final I18N i18n = I18NManager.getGlobal();
	
	@SuppressWarnings("unchecked")
	@Command(name = "flag", usage = "/spleef flag <game> <flag> [flag-value|remove]", 
			descref = Messages.Help.Description.FLAG,
			permission = Permissions.PERMISSION_FLAG)
	@PlayerOnly
	public void onFlagCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
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
		
		if (context.argsLength() > 2 && (removeIdentifier.equalsIgnoreCase(REMOVE_IDENTIFIER) || removeIdentifier.equalsIgnoreCase("clear"))) {
			CommandValidate.isTrue(game.isFlagPresent(flagPath), i18n.getVarString(Messages.Command.FLAG_NOT_PRESENT)
					.setVariable("flag", flagPath)
					.toString());
			
			List<Class<? extends AbstractFlag<?>>> childs = registry.getChildFlags(flagClass);
			childs.add(flagClass);
			
			for (Class<? extends AbstractFlag<?>> clazz : childs) {
				if (clazz == null) {
					continue;
				}
				
				game.removeFlag(clazz);
				player.sendMessage(i18n.getVarString(Messages.Command.FLAG_REMOVED)
						.setVariable("flag", registry.getFlagPath(clazz))
						.toString());
			}
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
            if (flagData.requiresVersion() != MinecraftVersion.UNKNOWN_VERSION) {
                Updater.Version implementationVersion = MinecraftVersion.getImplementationVersion();
                Updater.Version requiredVersion = MinecraftVersion.getVersionByInt(flagData.requiresVersion());

                if (implementationVersion.compareTo(requiredVersion) < 0) {
                    player.sendMessage(i18n.getVarString(Messages.Command.NEED_MC_VERSION_FOR_FLAG)
                            .setVariable("required", requiredVersion.toString())
                            .toString());
                    return;
                }
            }
			
			HookReference[] references = flagData.depend();
			HookManager hookManager = heavySpleef.getHookManager();
			
			for (HookReference ref : references) {
				Hook hook = hookManager.getHook(ref);
				
				CommandValidate.isTrue(hook.isProvided(), i18n.getVarString(Messages.Command.FLAG_REQUIRES_HOOK)
						.setVariable("hook", hook.getName())
						.toString());
			}
			
			List<FlagManager.Conflict> conflicts = game.getFlagManager().computeConflicts(flagClass, flagData);
			if (!conflicts.isEmpty()) {
				for (FlagManager.Conflict conflict : conflicts) {
					player.sendMessage(i18n.getVarString(Messages.Command.FLAG_CONFLICT)
							.setVariable("conflict-source", conflict.getConflictSourceAnnotation().name())
							.setVariable("conflict-with", conflict.getConflictWithAnnotation().name())
							.toString());
				}
				
				return;
			}
			
			AbstractFlag<Object> flag;
			boolean existingFlag = false;
			
			if (game.isFlagPresent(flagClass)) {
				flag = (AbstractFlag<Object>) game.getFlag(flagClass);
				existingFlag = true;
			} else {
				flag = (AbstractFlag<Object>) registry.newFlagInstance(flagPath.toLowerCase(), flagClass, game);
			}
			
			Object value = null;
			String extraMessage = null;
			
			try {
				value = flag.parseInput(player, inputBuilder.toString());
				flag.validateInput(value);
				flag.validateInput(value, game);
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
			if (value != null || NullFlag.class.isAssignableFrom(flagClass)) {
				flag.setValue(value);
			}
			
			if (!existingFlag) {
				game.addFlag(flag);
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
		
		sender.sendMessage(i18n.getVarString(Messages.Player.AVAILABLE_FLAGS)
				.setVariable("flags", builder.toString())
				.toString());
	}
	
	private void validateFlagParents(Flag flagData, Game game) throws CommandException {
		Class<? extends AbstractFlag<?>> parentClass = flagData.parent();

        if (parentClass != NullFlag.class) {
            Flag parentFlagData = parentClass.getAnnotation(Flag.class);
            String path = FlagManager.generatePath(parentFlagData);

            if (!game.isFlagPresent(path)) {
                throw new CommandException(i18n.getVarString(Messages.Command.PARENT_FLAG_NOT_SET)
                        .setVariable("parent-flag", path)
                        .toString());
            }
        }
	}
	
	@TabComplete("flag")
	public void onFlagTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		FlagRegistry registry = heavySpleef.getFlagRegistry();
		
		if (context.argsLength() == 1) {
			GameManager manager = heavySpleef.getGameManager();
			for (Game game : manager.getGames()) {
				list.add(game.getName());
			}
		} else if (context.argsLength() == 2) {
			DualKeyBiMap<String, Flag, Class<? extends AbstractFlag<?>>> flags = registry.getAvailableFlags();
			
			Set<String> flagNames = Sets.newTreeSet();
			for (String flagPath : flags.primaryKeySet()) {
				flagNames.add(flagPath);
			}
			
			for (String flagName : flagNames) {
				list.add(flagName);
			}
		} else if (context.argsLength() == 4) {
			list.add(REMOVE_IDENTIFIER);
		}
	}

}