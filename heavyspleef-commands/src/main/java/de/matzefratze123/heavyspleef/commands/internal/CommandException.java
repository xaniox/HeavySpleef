package de.matzefratze123.heavyspleef.commands.internal;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandException extends Exception {

	private static final long serialVersionUID = 497300006825032085L;
	
	public CommandException(String message) {
		super(message);
	}
	
	public CommandException(Throwable cause) {
		super(cause);
	}
	
	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public void sendToPlayer(CommandSender sender) {
		String message = getMessage();
		
		if (message != null) {
			sender.sendMessage(ChatColor.RED + message);
		}
	}

}
