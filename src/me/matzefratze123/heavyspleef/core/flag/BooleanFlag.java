package me.matzefratze123.heavyspleef.core.flag;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BooleanFlag extends Flag<Boolean> {

	public BooleanFlag(String name) {
		super(name);
	}

	@Override
	public Boolean parse(Player player, String input) {
		if (input.equalsIgnoreCase("true")
			|| input.equalsIgnoreCase("on")
			|| input.equalsIgnoreCase("activate")
			|| input.equalsIgnoreCase("yes"))
			return true;
		else if (input.equalsIgnoreCase("false")
			|| input.equalsIgnoreCase("off")
			|| input.equalsIgnoreCase("deactivate")
			|| input.equalsIgnoreCase("no"))
			return false;
		
		return false;
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + ChatColor.RED + " /spleef flag <name> " + getName() + " <true|false>";
	}
	
}
