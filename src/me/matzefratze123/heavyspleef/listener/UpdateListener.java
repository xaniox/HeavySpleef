package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		if(!player.hasPermission(Permissions.CREATE_GAME.getPerm()))
			return;
		if (!HeavySpleef.updateAvaible)
			return;
		
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Your version is outdated! " + ChatColor.GOLD + HeavySpleef.updateName + ChatColor.DARK_PURPLE + " (" + HeavySpleef.updateSize + " bytes)");
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " If you want to download a new version of HeavySpleef type /spleef update");
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " You may have to" + ChatColor.UNDERLINE + " delete " + ChatColor.RESET + ChatColor.DARK_PURPLE + "your config.yml and your language files for new ones!");
	}

	
}
