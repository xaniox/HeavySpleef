package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class QueuesListener implements Listener {
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		
		if (!GameManager.isInQueue(p))
			return;
		if (HeavySpleef.instance.getConfig().getBoolean("queues.commandsInQueue", false))
			return;
		
		String[] split = e.getMessage().split(" ");
		String cmd = split[0];
		if (cmd.equalsIgnoreCase("/spleef") || cmd.equalsIgnoreCase("/hs") || cmd.equalsIgnoreCase("/hspleef"))
			return;
		
		e.setCancelled(true);
		p.sendMessage(Game._("noCommandsInQueue"));
	}

}
