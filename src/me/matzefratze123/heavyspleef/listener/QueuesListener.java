/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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
package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.QueuesManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QueuesListener implements Listener {
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		
		if (p == null)
			return;
		if (!QueuesManager.hasQueue(p))
			return;
		if (HeavySpleef.getSystemConfig().getBoolean("queues.commandsInQueue", false))
			return;
		
		String[] split = e.getMessage().split(" ");
		String cmd = split[0];
		for (String command : HeavySpleef.commands) {
			if (cmd.equalsIgnoreCase(command))
				return;
		}
		
		e.setCancelled(true);
		p.sendMessage(Game._("noCommandsInQueue"));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		handleQuit(e);
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent e) {
		handleQuit(e);
	}
	
	private void handleQuit(PlayerEvent e) {
		Player p = e.getPlayer();
		
		if (p == null)
			return;
		if (!QueuesManager.hasQueue(p))
			return;
		
		//Remove the player from the queue if he quits
		QueuesManager.removeFromQueue(p);
	}

}
