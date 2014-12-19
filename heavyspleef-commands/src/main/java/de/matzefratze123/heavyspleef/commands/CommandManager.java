package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;

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
	public void onSpleefCommand(CommandContext context) {
		
	}
	
}
