package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.command.CommandJoin;
import me.matzefratze123.heavyspleef.core.Game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PVPTimerListener implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location to = e.getTo();
		Location from = e.getFrom();
		
		if (!CommandJoin.pvpTimerTasks.containsKey(p.getName()))
			return;
		if (to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ())
			return;
		if (HeavySpleef.instance.getConfig().getInt("general.pvptimer") <= 0)
			return;
		
		Bukkit.getScheduler().cancelTask(CommandJoin.pvpTimerTasks.get(p.getName()));
		CommandJoin.pvpTimerTasks.remove(p.getName());
		p.sendMessage(Game._("pvpTimerCancelled"));
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		
		if (!CommandJoin.pvpTimerTasks.containsKey(p.getName()))
			return;
		
		Bukkit.getScheduler().cancelTask(CommandJoin.pvpTimerTasks.get(p.getName()));
		CommandJoin.pvpTimerTasks.remove(p.getName());
		p.sendMessage(Game._("pvpTimerCancelled"));
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
		Player p = e.getPlayer();
		
		if (!CommandJoin.pvpTimerTasks.containsKey(p.getName()))
			return;
		
		Bukkit.getScheduler().cancelTask(CommandJoin.pvpTimerTasks.get(p.getName()));
		CommandJoin.pvpTimerTasks.remove(p.getName());
	}
	
}
