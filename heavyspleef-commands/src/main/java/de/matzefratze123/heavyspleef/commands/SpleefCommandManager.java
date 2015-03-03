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

import mkremins.fanciful.FancyMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContainer;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandManager;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.commands.base.NestedCommands;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class SpleefCommandManager implements CommandManager {
	
	private CommandManagerService service;
	
	public SpleefCommandManager(final HeavySpleef plugin) {
		service = new CommandManagerService(plugin.getPlugin(), plugin.getLogger(), plugin) {
			
			@Override
			public boolean checkPermission(CommandSender sender, String permission) {
				//Use the built-in bukkit permission check
				return sender.hasPermission(permission);
			}
			
			@Override
			public String getMessage(String key, String... messageArgs) {
				String message = null;
				
				switch (key) {
				case "message.player_only":
					message = I18N.getInstance().getString(Messages.Command.PLAYER_ONLY);
					break;
				case "message.no_permission":
					message = I18N.getInstance().getString(Messages.Command.NO_PERMISSION);
					break;
				case "message.description_format":
					message = I18N.getInstance().getVarString(Messages.Command.DESCRIPTION_FORMAT)
						.setVariable("description", messageArgs[0])
						.toString();
					break;
				case "message.usage_format":
					message = I18N.getInstance().getVarString(Messages.Command.USAGE_FORMAT)
						.setVariable("usage", messageArgs[0])
						.toString();
					break;
				case "message.unknown_command":
					message = I18N.getInstance().getString(Messages.Command.UNKNOWN_COMMAND);
					break;
				default:
					break;
				}
				
				return message;
			}
			
		};
		
		PluginCommand spleefCommand = plugin.getPlugin().getCommand("spleef");
		spleefCommand.setExecutor(service);
	}
	
	/**
	 * Initializes this command manager and adds all command classes 
	 * to its {@link de.matzefratze123.heavyspleef.commands.base.CommandManagerService}
	 */
	public void init() {
		service.registerCommands(getClass());
	}
	
	@Override
	public void registerSpleefCommands(Class<?> clazz) {
		CommandContainer spleefCommand = service.containerOf("spleef");
		service.registerCommands(clazz, spleefCommand);
	}
	
	@Command(name = "spleef", usage = "/spleef [sub-command]",
			description = "Spleef command for HeavySpleef")
	@NestedCommands(value = {
			CommandCreate.class,
			CommandDelete.class,
			CommandDisable.class,
			CommandEnable.class,
			CommandFlag.class,
			CommandHelp.class,
			CommandInfo.class,
			CommandJoin.class,
			CommandKick.class,
			CommandLeave.class,
			CommandList.class,
			CommandStart.class,
			CommandStats.class,
			CommandStop.class
	})
	public void onSpleefCommand(CommandContext context, HeavySpleef heavySpleef) {
		CommandSender sender = context.getSender();
		PluginDescriptionFile desc = heavySpleef.getPlugin().getDescription();
		
		// Send a nice, formatted message to the player
		new FancyMessage(desc.getName())
				.color(ChatColor.GRAY)
				.style(ChatColor.BOLD)
			.then(" version " + desc.getVersion() + "\nType ")
				.color(ChatColor.GOLD)
			.then("/spleef help")
				.command("/spleef help")
				.tooltip("Click here to access help")
			.then(" for help or click ")
			.then("here")
				.color(ChatColor.GRAY)
				.command("/spleef help")
				.tooltip("Click here to access help")
			.send(sender);
	}
	
}