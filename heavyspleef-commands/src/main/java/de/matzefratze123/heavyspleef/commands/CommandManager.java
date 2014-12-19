package de.matzefratze123.heavyspleef.commands;

import mkremins.fanciful.FancyMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;

import de.matzefratze123.heavyspleef.commands.internal.Command;
import de.matzefratze123.heavyspleef.commands.internal.CommandContext;
import de.matzefratze123.heavyspleef.commands.internal.CommandManagerService;
import de.matzefratze123.heavyspleef.commands.internal.NestedCommands;
import de.matzefratze123.heavyspleef.core.HeavySpleef;

public class CommandManager {
	
	private CommandManagerService service;
	
	public CommandManager(HeavySpleef plugin) {
		service = new CommandManagerService(plugin, plugin.getLogger()) {
			
			@Override
			public boolean checkPermission(CommandSender sender, String permission) {
				//Use the built-in bukkit permission check
				return sender.hasPermission(permission);
			}
		};
		
		PluginCommand spleefCommand = plugin.getPlugin().getCommand("spleef");
		spleefCommand.setExecutor(service);
	}
	
	/**
	 * Initializes this command manager and adds all command classes 
	 * to its {@link de.matzefratze123.heavyspleef.commands.internal.CommandManagerService}
	 */
	public void init() {
		service.registerCommands(getClass());
	}
	
	@Command(name = "spleef", usage = "/spleef [sub-command]",
			description = "Spleef command for HeavySpleef")
	@NestedCommands(value = {
			CommandCreate.class,
			CommandDelete.class,
			CommandDisable.class,
			CommandEnable.class,
			CommandHelp.class,
			CommandInfo.class,
			CommandJoin.class,
			CommandKick.class,
			CommandLeave.class,
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
			.then(" for help or click ")
			.then("here")
				.color(ChatColor.GRAY)
				.command("/spleef help")
			.send(sender);
	}
	
}