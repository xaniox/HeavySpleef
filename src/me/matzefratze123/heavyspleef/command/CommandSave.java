package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;

public class CommandSave extends HSCommand {

	public CommandSave() {
		setMinArgs(0);
		setMaxArgs(0);
		setPermission(Permissions.SAVE.getPerm());
		setUsage("/spleef save");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		HeavySpleef.instance.database.save(false);
		HeavySpleef.instance.statisticDatabase.save();
		sender.sendMessage(_("gamesSaved"));
	}

}
