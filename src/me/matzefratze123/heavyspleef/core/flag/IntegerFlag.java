package me.matzefratze123.heavyspleef.core.flag;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.entity.Player;

public class IntegerFlag extends Flag<Integer> {

	public IntegerFlag(String name) {
		super(name);
	}

	@Override
	public Integer parse(Player player, String input) {
		try {
			int i = Integer.parseInt(input);
			i = Math.abs(i);
			return i;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + " /spleef flag <name> " + getName() + " [number]";
	}

}
