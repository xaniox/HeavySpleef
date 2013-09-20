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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Permissions;

public abstract class HSCommand implements TabCompleter {

	private Permissions permission = null;
	private int minArgs = 0;
	private int maxArgs = -1;
	private boolean onlyIngame = false;
	public static HeavySpleef plugin;
	public static FileConfiguration config;
	protected String usage;
	protected List<String> tabArgsComplete = new ArrayList<String>();

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
		return HeavySpleef.PREFIX + " " + usage;
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
	
	void setTabHelp(String[] help) {
		this.tabArgsComplete = Arrays.asList(help);
	}
	
	public static String _(String... key) {
		return HeavySpleef.PREFIX + ChatColor.RESET + " " + LanguageHandler._(key);
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
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		return this.tabArgsComplete;
	}
}
