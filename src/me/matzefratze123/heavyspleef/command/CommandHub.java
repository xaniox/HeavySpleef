package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHub extends HSCommand {

	public CommandHub() {
		setPermission(Permissions.TELEPORT_HUB);
		setUsage("/spleef hub");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		tpToHub(player);
	}
	
	public static void tpToHub(final Player player) {
		int pvpTimer = HeavySpleef.getSystemConfig().getInt("general.pvptimer", 5);
		
		if (GameManager.getSpleefHub() == null) {
			player.sendMessage(_("noSpleefHubSet"));
			return;
		}
		
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				player.teleport(GameManager.getSpleefHub());
				player.sendMessage(_("teleportToHub"));
			}
		}, pvpTimer * 20L);
		Game.pvpTimerTasks.put(player.getName(), taskId);
	}
	
}
