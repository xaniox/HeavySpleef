package de.matzefratze123.heavyspleef.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.matzefratze123.api.hs.command.RootCommandExecutor;
import de.matzefratze123.heavyspleef.HeavySpleef;

public class RootCommand implements RootCommandExecutor {

	@Override
	public void execute(CommandSender sender) {
		sender.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "HeavySpleef " + ChatColor.GOLD + "v" + HeavySpleef.getInstance().getDescription().getVersion() + " by matzefratze123");
		sender.sendMessage(ChatColor.GOLD + "Type '/spleef help' for help");
	}

}
