package de.matzefratze123.heavyspleef.flag.presets;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class LocationFlag extends AbstractFlag<Location> {

	@Override
	public Location parseInput(Player player, String input) throws InputParseException {
		return player.getLocation();
	}
	
}
