package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class HSCommand {

	private String permission = "";
	private int minArgs = 0;
	private int maxArgs = -1;
	private boolean onlyIngame = false;
	public static HeavySpleef plugin;
	public static FileConfiguration config;
	protected String usage;

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
	
	void setPermission(String perm) {
		this.permission = perm;
	}
	
	String getPermission() {
		return permission;
	}
	
	void setUsage(String usage) {
		this.usage = usage;
	}
	
	String getUsage() {
		return ChatColor.RED + "[HeavySpleef] " + usage;
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
	
	public static String _(String... key) {
		return ChatColor.RED + "[" + ChatColor.GOLD + "HeavySpleef" + ChatColor.RED + "] " + ChatColor.RESET + LanguageHandler._(key);
	}
	
	static void setPluginInstance(HeavySpleef instance) {
		plugin = instance;
	}
	
	static void setFileConfiguration(FileConfiguration c) {
		config = c;
	}
}
