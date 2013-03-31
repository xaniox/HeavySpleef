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

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.SignWall;
import me.matzefratze123.heavyspleef.database.Parser;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SignWallListener implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		
		for (Game game : GameManager.getGames()) {
			for (SignWall wall : game.getWalls()) {
				if (wall.contains(e.getBlock())) {
					if (p.hasPermission(Permissions.REMOVE_WALL.getPerm())) {
						p.sendMessage(Game._("cannotDestroyWallAdmin"));
					} else {
						p.sendMessage(Game._("cannotDestroyWallUser"));
					}
					
					e.setCancelled(true);
				} else {
					for (Sign sign : wall.getSigns()) {
						if (Parser.roundLocation(SignWall.getAttachedBlock(sign).getLocation()).equals(e.getBlock().getLocation()))
							e.setCancelled(true);
					}
				}
			}
		}
	}
	
}
