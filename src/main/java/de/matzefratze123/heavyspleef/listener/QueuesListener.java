/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.QueuesManager;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.I18N;

public class QueuesListener implements Listener {

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(e.getPlayer());

		if (player == null)
			return;
		if (!QueuesManager.hasQueue(player))
			return;
		if (HeavySpleef.getSystemConfig().getQueuesSection().isAllowCommands())
			return;

		if (e.getMessage().equalsIgnoreCase("/spleef leave") || e.getMessage().equalsIgnoreCase("/spl leave"))
			return;

		e.setCancelled(true);
		player.sendMessage(I18N._("noCommandsInQueue"));
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
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(e.getPlayer());

		if (player == null)
			return;
		if (!QueuesManager.hasQueue(player))
			return;

		// Remove the player from the queue if he quits
		QueuesManager.removeFromQueue(player);
	}

}
