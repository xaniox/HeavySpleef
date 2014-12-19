package de.matzefratze123.heavyspleef.flag.presets;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class DoubleFlag extends AbstractFlag<Double> {
	
	@Override
	public Double parseInput(Player player, String input) throws InputParseException {
		double result;
		
		try {
			result = Double.parseDouble(input);
		} catch (NumberFormatException nfe) {
			throw new InputParseException(input);
		}
		
		return Double.valueOf(result);
	}
	
}
