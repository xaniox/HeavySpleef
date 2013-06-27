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
package me.matzefratze123.heavyspleef;

import java.io.File;
import java.io.IOException;

import me.matzefratze123.heavyspleef.api.GameAPI;
import me.matzefratze123.heavyspleef.command.CommandHandler;
import me.matzefratze123.heavyspleef.config.FileConfig;
import me.matzefratze123.heavyspleef.core.task.AntiCampingTask;
import me.matzefratze123.heavyspleef.database.YamlDatabase;
import me.matzefratze123.heavyspleef.hooks.Hook;
import me.matzefratze123.heavyspleef.hooks.HookManager;
import me.matzefratze123.heavyspleef.hooks.TagAPIHook;
import me.matzefratze123.heavyspleef.listener.HUBPortalListener;
import me.matzefratze123.heavyspleef.listener.PVPTimerListener;
import me.matzefratze123.heavyspleef.listener.PlayerListener;
import me.matzefratze123.heavyspleef.listener.QueuesListener;
import me.matzefratze123.heavyspleef.listener.ReadyListener;
import me.matzefratze123.heavyspleef.listener.SignListener;
import me.matzefratze123.heavyspleef.listener.SignWallListener;
import me.matzefratze123.heavyspleef.listener.TagListener;
import me.matzefratze123.heavyspleef.listener.UpdateListener;
import me.matzefratze123.heavyspleef.selection.SelectionListener;
import me.matzefratze123.heavyspleef.selection.SelectionManager;
import me.matzefratze123.heavyspleef.stats.IStatisticDatabase;
import me.matzefratze123.heavyspleef.stats.MySQLStatisticDatabase;
import me.matzefratze123.heavyspleef.stats.YamlStatisticDatabase;
import me.matzefratze123.heavyspleef.util.InventoryMenu;
import me.matzefratze123.heavyspleef.util.LanguageHandler;
import me.matzefratze123.heavyspleef.util.Metrics;
import me.matzefratze123.heavyspleef.util.Updater;
import me.matzefratze123.heavyspleef.util.ViPManager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.TagAPI;

public class HeavySpleef extends JavaPlugin {
		
	private SelectionManager selectionManager;
	
	public static FileConfig config;
	public static HeavySpleef instance;
	
	public static String PREFIX = ChatColor.RED + "[" + ChatColor.GOLD + "Spleef" + ChatColor.RED + "]"; 
	public static HookManager hooks;

	public YamlDatabase database;
	public IStatisticDatabase statisticDatabase;
	
	public static String[] commands = new String[] {"/spleef", "/hs", "/hspleef"};
	private InventoryMenu menu;
	
	public int saverTid = -1;
	public int antiCampTid = -1;
	
	/* Updater stuff start */
	public static boolean updateAvaible = false;
	public static String updateName = "";
	public static long updateSize = 0L;
	public static File pluginFile = null;
	/* Updater stuff end */
	
	@Override
	public void onEnable() {
		//Set the instance FIRST!
		instance = this;
		config = new FileConfig(this);
		
		hooks = HookManager.getInstance();
		selectionManager = new SelectionManager();
		database = new YamlDatabase();
		database.load();
		pluginFile = this.getFile();
		PREFIX = ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.spleef-prefix", PREFIX));
		
		LanguageHandler.loadLanguageFiles();
		ViPManager.initVips();
		
		menu = new InventoryMenu(LanguageHandler._("inventory"), this);
		
		setupStatisticDatabase();
		statisticDatabase.load();
		
		//Start metrics
		startMetrics();
		
		initUpdate();
		registerEvents();
		getCommand("spleef").setExecutor(new CommandHandler());
		
		startAntiCampingTask();
		
		CommandHandler.initCommands();
		CommandHandler.setPluginInstance(this);
		CommandHandler.setConfigInstance(this);
		
		this.getLogger().info("HeavySpleef v" + getDescription().getVersion() + " activated!");
	}

	@Override
	public void onDisable() {
		
		this.getServer().getScheduler().cancelTasks(this);
		this.database.save();
		this.statisticDatabase.save();
		this.getLogger().info("HeavySpleef deactivated!");
	}
	
	public static FileConfiguration getSystemConfig() {
		return instance.getConfig();
	}
	
	private void setupStatisticDatabase() {
		String type = this.getConfig().getString("statistic.dbType");
		
		if (type.equalsIgnoreCase("yaml")) {
			this.statisticDatabase = new YamlStatisticDatabase();
		} else if (type.equalsIgnoreCase("mysql")) {
			this.statisticDatabase = new MySQLStatisticDatabase();
		} else {
			this.getLogger().warning("Invalid statistic database type! Setting to YAML...");
			this.statisticDatabase = new YamlStatisticDatabase();
		}
	}
	
	public static GameAPI getAPI() {
		return GameAPI.getInstance();
	}
	
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}
	
	private void registerEvents() {
		PluginManager pm = this.getServer().getPluginManager();
		
		pm.registerEvents(new SignListener(), this);
		pm.registerEvents(new SelectionListener(this), this);
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new UpdateListener(), this);
		pm.registerEvents(new SignWallListener(), this);
		pm.registerEvents(new QueuesListener(), this);
		pm.registerEvents(new PVPTimerListener(), this);
		pm.registerEvents(new HUBPortalListener(), this);
		pm.registerEvents(new ReadyListener(), this);
		
		Hook<TagAPI> tagAPIHook = hooks.getService(TagAPIHook.class);
		if (tagAPIHook.hasHook()) {
			pm.registerEvents(new TagListener(), this);
		}
	}
	
	private void initUpdate() {
		if (!getConfig().getBoolean("auto-update"))
			return;
		
		Updater updater = new Updater(this, "heavyspleef", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
		
		updateAvaible = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
		updateName = updater.getLatestVersionString();
		updateSize = updater.getFileSize();
	}
	
	public void startAntiCampingTask() {
		if (!getConfig().getBoolean("anticamping.enabled"))
			return;
		
		this.antiCampTid = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AntiCampingTask(), 0L, 20L);
	}
	
	private void startMetrics() {
		try {
			Metrics m = new Metrics(HeavySpleef.this);
			m.start();
		} catch (IOException e) {
			HeavySpleef.this.getLogger().info("An error occured while submitting stats to metrics...");
		}
	}
	
	public InventoryMenu getInventoryMenu() {
		return menu;
	}
	
}