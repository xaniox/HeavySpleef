package de.matzefratze123.heavyspleef.flag.presets;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class StringFlag extends AbstractFlag<String> {

	@Override
	public String parseInput(Player player, String input) throws InputParseException {
		return input;
	}
	
}
