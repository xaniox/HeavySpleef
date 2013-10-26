package de.matzefratze123.heavyspleef.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.stats.StatisticManager;
import de.matzefratze123.heavyspleef.stats.StatisticModule;

public class StatisticAccountListener implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		if (StatisticManager.hasStatistic(player.getName())) {
			return;
		}
		
		HeavySpleef.getInstance().getStatisticDatabase().loadAccount(player.getName());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		handleQuit(e);
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent e) {
		handleQuit(e);
	}
	
	private void handleQuit(PlayerEvent e) {
		Player player = e.getPlayer();
		
		StatisticModule module = StatisticManager.getStatistic(player.getName(), true);
		HeavySpleef.getInstance().getStatisticDatabase().unloadAccount(module);
	}
	
}
