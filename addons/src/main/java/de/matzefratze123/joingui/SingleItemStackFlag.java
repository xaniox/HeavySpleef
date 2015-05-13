package de.matzefratze123.joingui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.flag.presets.ItemStackFlag;

public abstract class SingleItemStackFlag extends ItemStackFlag {

	@Override
	public ItemStack parseInput(Player player, String input) throws InputParseException {
		ItemStack stack = super.parseInput(player, input);
		stack.setAmount(1);
		
		return stack;
	}

}
