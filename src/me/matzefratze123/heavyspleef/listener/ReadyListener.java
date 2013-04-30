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
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.util.SimpleBlockData;
import me.matzefratze123.heavyspleef.util.Util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ReadyListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();
		
		if (player == null)
			return;
		if (block == null)
			return;
		if (!GameManager.isInAnyGame(player))
			return;
		
		Game game = GameManager.fromPlayer(player);
		if (!game.isPreLobby())
			return;
		
		SimpleBlockData readyBlock = Util.fromString(HeavySpleef.getSystemConfig().getString("general.ready-block"), false);
		if (readyBlock == null)
			return;
		
		Material mat = readyBlock.getMaterial();
		byte data = readyBlock.getData();
		
		if (mat != block.getType())
			return;
		if (data != block.getData())
			return;
		
		boolean success = game.addVote(player);
		String message = success ? Game._("taggedAsReady") : Game._("alreadyVoted");
		player.sendMessage(message);
	}
	
}
