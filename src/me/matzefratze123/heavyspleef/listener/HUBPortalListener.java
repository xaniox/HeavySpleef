package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.region.HUBPortal;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HUBPortalListener implements Listener {
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		for (HUBPortal portal : GameManager.getPortals()) {
			portal.onMove(e);
		}
	}
	
}
