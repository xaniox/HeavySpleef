package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.Permissions;
import me.matzefratze123.heavyspleef.utility.Updater;
import me.matzefratze123.heavyspleef.utility.Updater.UpdateType;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandUpdate extends HSCommand {

	public CommandUpdate() {
		setMinArgs(0);
		setMaxArgs(0);
		setPermission(Permissions.UPDATE_PLUGIN.getPerm());
		setUsage("/spleef update");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!HeavySpleef.updateAvaible) {
			sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " There is no new update avaible!");
			return;
		}
		
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Updating plugin, please wait...");
		long start = System.currentTimeMillis();
		
		Updater updater = new Updater(plugin, "heavyspleef", HeavySpleef.pluginFile, UpdateType.NO_VERSION_CHECK, true);
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Done! Took " + (System.currentTimeMillis() - start) + "ms!");
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " You find the HeavySpleef.jar in the folder \"plugins/" + updater.updateFolder + "/HeavySpleef.jar\"");
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " You may have to" + ChatColor.UNDERLINE + " delete " + ChatColor.RESET + ChatColor.DARK_PURPLE + "your config.yml and your language files for new ones!");
		sender.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " New version of the HeavySpleef will functionable at the next server restart/reload!");
		HeavySpleef.updateAvaible = false;
	}

	
	
}
