/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Permissions;

public abstract class HSCommand {

	private Permissions permission = null;
	private int minArgs = 0;
	private int maxArgs = -1;
	private boolean onlyIngame = false;
	private String help;
	private String usage;
	
	public static HeavySpleef plugin;
	public static FileConfiguration config;
	

	public abstract void execute(CommandSender sender, String[] args);
	
	void setMaxArgs(int arg) {
		this.maxArgs = arg;
	}
	
	void setMinArgs(int arg) {
		this.minArgs = arg;
	}
	
	int getMaxArg() {
		return maxArgs;
	}
	
	int getMinArg() {
		return minArgs;
	}
	
	void setPermission(Permissions perm) {
		this.permission = perm;
	}
	
	Permissions getPermission() {
		return permission;
	}
	
	void setUsage(String usage) {
		this.usage = usage;
	}
	
	String getUsage() {
		return HeavySpleef.PREFIX + ChatColor.RED + " " + usage;
	}
	
	String getExactUsage() {
		return usage;
	}
	
	boolean onlyIngame() {
		return onlyIngame;
	}
	
	void setOnlyIngame(boolean ingame) {
		this.onlyIngame = ingame;
	}
	
	void setHelp(String description) {
		this.help = description;
	}
	
	String getHelp() {
		return help;
	}
	
	public static String _(String... key) {
		return LanguageHandler._(key);
	}
	
	public static String __(String str) {
		return HeavySpleef.PREFIX + " " + str;
	}
	
	static void setPluginInstance(HeavySpleef instance) {
		plugin = instance;
	}
	
	static void setFileConfiguration(FileConfiguration c) {
		config = c;
	}
	
}
