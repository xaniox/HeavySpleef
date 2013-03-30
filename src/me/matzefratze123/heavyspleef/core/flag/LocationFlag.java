package me.matzefratze123.heavyspleef.core.flag;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationFlag extends Flag<Location> {

	public LocationFlag(String name) {
		super(name);
	}

	@Override
	public Location parse(Player player, String input) {
		if (player == null)
			return null;
		
		return player.getLocation();
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + ChatColor.RED + " /spleef flag <name> " + getName();
	}

}
