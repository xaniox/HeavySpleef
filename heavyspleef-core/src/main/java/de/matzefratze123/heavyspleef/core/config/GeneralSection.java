/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core.config;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class GeneralSection {
	
	private static final char TRANSLATE_CHAR = '&';
	
	private String spleefPrefix;
	private List<String> whitelistedCommands;
	private String vipPrefix;
	private boolean vipJoinFull;
	private int pvpTimer;
	private boolean broadcastGameStart;
	private List<String> broadcastGameStartBlacklist;
	
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
		this.broadcastGameStart = section.getBoolean("broadcast-game-start", true);
		this.broadcastGameStartBlacklist = section.getStringList("broadcast-game-start-blacklist");
	}
	
	public String getSpleefPrefix() {
		return spleefPrefix;
	}

	public List<String> getWhitelistedCommands() {
		return whitelistedCommands;
	}

	public String getVipPrefix() {
		return vipPrefix;
	}

	public boolean isVipJoinFull() {
		return vipJoinFull;
	}

	public int getPvpTimer() {
		return pvpTimer;
	}
	
	public boolean getBroadcastGameStart() {
		return broadcastGameStart;
	}
	
	public List<String> getBroadcastGameStartBlacklist() {
		return broadcastGameStartBlacklist;
	}
	
}
