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
