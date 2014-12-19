package de.matzefratze123.heavyspleef.flag.presets;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class IntegerFlag extends AbstractFlag<Integer> {

	@Override
	public Integer parseInput(Player player, String input) throws InputParseException {
		int result;
		
		try {
			result = Integer.parseInt(input);
		} catch (NumberFormatException nfe) {
			throw new InputParseException(input);
		}
		
		return result;
	}
	
}
