package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetHub extends HSCommand {

	public CommandSetHub() {
		setUsage("/spleef sethub");
		setOnlyIngame(true);
		setPermission(Permissions.SET_HUB);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (args.length <= 0) {
			GameManager.setSpleefHub(player.getLocation());
			player.sendMessage(_("spleefHubSet"));
		} else if (args[0].equalsIgnoreCase("clear")){
			GameManager.setSpleefHub(null);
			player.sendMessage(_("spleefHubSet"));
		}
	}

}
