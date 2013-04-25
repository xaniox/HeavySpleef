package me.matzefratze123.heavyspleef.core.region;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Represents a portal in a world which points to the spleef hub
 * 
 * @author matzefratze123
 *
 */
public class HUBPortal extends RegionBase {

	private Location firstCorner;
	private Location secondCorner;
	
	public HUBPortal(int id, Location firstCorner, Location secondCorner) {
		super(id);
		
		this.firstCorner = firstCorner;
		this.secondCorner = secondCorner;
	}
	
	public HUBPortal(Location firstCorner, Location secondCorner) {
		this(-1, firstCorner, secondCorner);
	}

	@Override
	public boolean contains(Location location) {
		return RegionBase.contains(firstCorner, secondCorner, location);
	}
	
	public Location getFirstCorner() {
		return this.firstCorner;
	}
	
	public Location getSecondCorner() {
		return this.secondCorner;
	}
	
	public void onMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		
		if (!contains(player.getLocation()))
			return;
		if (!player.hasPermission(Permissions.USE_PORTAL.getPerm())) {
			player.sendMessage(Game._("noPermission"));
			player.teleport(e.getFrom());
			return;
		}
		
		travel(player);
	}
	
	private void travel(Player player) {
		if (GameManager.getSpleefHub() == null)
			return;
		
		player.teleport(GameManager.getSpleefHub());
		player.sendMessage(Game._("welcomeToHUB"));
	}
	
}
