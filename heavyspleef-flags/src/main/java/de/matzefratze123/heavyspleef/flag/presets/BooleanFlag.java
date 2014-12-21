package de.matzefratze123.heavyspleef.flag.presets;

import java.util.List;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;

public abstract class BooleanFlag extends AbstractFlag<Boolean> {

	private static final List<String> TRUE_MATCHING_KEYWORDS = Lists.newArrayList("true", "yes", "on", "enable");
	
	@Override
	public Boolean parseInput(Player player, String input) {
		boolean bool = false;
		for (String keyword : TRUE_MATCHING_KEYWORDS) {
			if (keyword.equals(input)) {
				bool = true;
			}
		}
		
		return Boolean.valueOf(bool);
	}
	
}
