package de.matzefratze123.joingui;

import java.text.ParseException;
import java.util.List;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.config.ThrowingConfigurationObject;

public class InventoryEntryConfig extends ThrowingConfigurationObject<ParseException> {

	private static final String DEFAULT_TITLE = "{if ($[state] == Ingame) then \"" + ChatColor.RED + "\" else \"" + ChatColor.GREEN + "\"}"
			+ ChatColor.BOLD + "Join " + ChatColor.GRAY + "$[game]";
	private static final List<String> DEFAULT_LORE = Lists.newArrayList("{if ($[state] == Ingame) then \"" + ChatColor.RED + "\" else \""
			+ ChatColor.GREEN + "\"}$[players]" + ChatColor.DARK_GRAY + ChatColor.BOLD + "/" + ChatColor.GREEN
			+ "{if (has_flag:max-players) then \"$[flag_value:max-players]\" else \"∞\"}", ChatColor.AQUA + "" + ChatColor.BOLD + "$[state]");
	
	private @Getter InventoryEntryLayout layout;
	
	public InventoryEntryConfig(Configuration config) {
		super(config);
	}

	@Override
	public final void inflateUnsafe(Configuration config, Object[] args) throws ParseException {
		ConfigurationSection layoutSection = config.getConfigurationSection("layout");
		String title;
		List<String> lore;
		
		if (layoutSection != null) {
			title = layoutSection.getString("title", DEFAULT_TITLE);
			lore = layoutSection.getStringList("lore");
			
			if (lore == null || lore.isEmpty()) {
				lore = DEFAULT_LORE;
			}
		} else {
			title = DEFAULT_TITLE;
			lore = DEFAULT_LORE;
		}
		
		layout = new InventoryEntryLayout(title, lore);
	}

	@Override
	protected Class<? extends ParseException> getExceptionClass() {
		return ParseException.class;
	}

}
