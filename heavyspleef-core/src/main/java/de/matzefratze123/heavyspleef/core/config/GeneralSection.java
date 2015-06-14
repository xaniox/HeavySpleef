package de.matzefratze123.heavyspleef.core.config;

import java.util.List;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class GeneralSection {
	
	private static final char TRANSLATE_CHAR = '&';
	
	private String spleefPrefix;
	private List<String> whitelistedCommands;
	private String vipPrefix;
	private boolean vipJoinFull;
	private int pvpTimer;
	
	public GeneralSection(ConfigurationSection section) {
		String prefix = section.getString("spleef-prefix");
		if (prefix != null) {
			this.spleefPrefix = ChatColor.translateAlternateColorCodes(TRANSLATE_CHAR, prefix);
		} else {
			this.spleefPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Spleef" + ChatColor.DARK_GRAY + "]";
		}
		
		this.whitelistedCommands = section.getStringList("command-whitelist");
		String vipPrefix = section.getString("vip-prefix");
		if (vipPrefix != null) {
			this.vipPrefix = ChatColor.translateAlternateColorCodes(TRANSLATE_CHAR, vipPrefix);
		} else {
			this.vipPrefix = ChatColor.RED.toString();
		}
		
		this.vipJoinFull = section.getBoolean("vip-join-full", true);
		this.pvpTimer = section.getInt("pvp-timer", 0);
	}
	
}
