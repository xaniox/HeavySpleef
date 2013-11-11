/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.PvPTimerManager;

public class PVPTimerListener implements Listener {
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location to = e.getTo();
		Location from = e.getFrom();
		
		if (!PvPTimerManager.isOnTimer(p))
			return;
		if (to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ())
			return;
		if (HeavySpleef.getSystemConfig().getInt("general.pvptimer") <= 0)
			return;
		
		PvPTimerManager.cancelTimerTask(p);
		p.sendMessage(I18N._("pvpTimerCancelled"));
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		
		if (!PvPTimerManager.isOnTimer(p))
			return;
		
		PvPTimerManager.cancelTimerTask(p);
		p.sendMessage(I18N._("pvpTimerCancelled"));
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
		
		if (!PvPTimerManager.isOnTimer(p))
			return;
		
		PvPTimerManager.cancelTimerTask(p);
	}
	
}
