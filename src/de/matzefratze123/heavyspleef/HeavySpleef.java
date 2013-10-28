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
package de.matzefratze123.heavyspleef;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.TagAPI;

import de.matzefratze123.heavyspleef.api.GameAPI;
import de.matzefratze123.heavyspleef.command.CommandHandler;
import de.matzefratze123.heavyspleef.config.FileConfig;
import de.matzefratze123.heavyspleef.core.task.AntiCampingTask;
import de.matzefratze123.heavyspleef.database.YamlDatabase;
import de.matzefratze123.heavyspleef.hooks.Hook;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.TagAPIHook;
import de.matzefratze123.heavyspleef.listener.HUBPortalListener;
import de.matzefratze123.heavyspleef.listener.PVPTimerListener;
import de.matzefratze123.heavyspleef.listener.PlayerListener;
import de.matzefratze123.heavyspleef.listener.QueuesListener;
import de.matzefratze123.heavyspleef.listener.ReadyListener;
import de.matzefratze123.heavyspleef.listener.SignWallListener;
import de.matzefratze123.heavyspleef.listener.StatisticAccountListener;
import de.matzefratze123.heavyspleef.listener.TagListener;
import de.matzefratze123.heavyspleef.listener.UpdateListener;
import de.matzefratze123.heavyspleef.selection.SelectionListener;
import de.matzefratze123.heavyspleef.selection.SelectionManager;
import de.matzefratze123.heavyspleef.signs.SpleefSignExecutor;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignHub;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignJoin;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignLeave;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignSpectate;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignStart;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignVote;
import de.matzefratze123.heavyspleef.stats.IStatisticDatabase;
import de.matzefratze123.heavyspleef.stats.MySQLStatisticDatabase;
import de.matzefratze123.heavyspleef.stats.YamlStatisticDatabase;
import de.matzefratze123.heavyspleef.util.JoinGUI;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Logger;
import de.matzefratze123.heavyspleef.util.Metrics;
import de.matzefratze123.heavyspleef.util.SpleefLogger;
import de.matzefratze123.heavyspleef.util.Updater;
import de.matzefratze123.heavyspleef.util.ViPManager;

public class HeavySpleef extends JavaPlugin {
		
	//Object instances start
	private static HookManager hooks;
	private YamlDatabase database;
	private IStatisticDatabase statisticDatabase;
	private SelectionManager selectionManager;
	private JoinGUI menu;
	//Object instances end
	
	//Main instance
	private static HeavySpleef instance;

	//Other utility stuff
	public static String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Spleef" + ChatColor.DARK_GRAY + "]";
	public static final String[] commands = new String[] {"spleef", "spl", "hs", "hspleef"};
	
	//Task id's
	public int saverTid = -1;
	public int antiCampTid = -1;
	
	// Updater
	private Updater updater;
	
	@Override
	public void onEnable() {
		//Set the instance first
		instance = this;
		
		new FileConfig(this);
		hooks = HookManager.getInstance();
		selectionManager = new SelectionManager();
		database = new YamlDatabase();
		database.load();
		
		PREFIX = ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.spleef-prefix", PREFIX));
		
		//Load languages
		LanguageHandler.loadLanguageFiles();
		ViPManager.initVips();
		
		menu = new JoinGUI(LanguageHandler._("inventory"), this);
		
		setupStatisticDatabase();
		//statisticDatabase.load();
		SpleefLogger.logRaw("Starting plugin version " + getDescription().getVersion() + "!");
		
		//Start metrics
		startMetrics();
		
		initUpdate();
		registerEvents();
		registerSigns();
		
		//Register our main command
		getCommand("spleef").setExecutor(new CommandHandler());
		
		startAntiCampingTask();
		
		//Command stuff
		CommandHandler.initCommands();
		CommandHandler.setPluginInstance(this);
		CommandHandler.setConfigInstance(this);
		
		Logger.info("HeavySpleef v" + getDescription().getVersion() + " activated!");
	}

	@Override
	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);
		this.database.save();
		this.statisticDatabase.saveAccounts();
		SpleefLogger.logRaw("Stopping plugin!");
		
		Logger.info("HeavySpleef disabled!");
	}
	
	public static HeavySpleef getInstance() {
		return instance;
	}
	
	public Updater getUpdater() {
		return updater;
	}
	
	public HookManager getHookManager() {
		return hooks;
	}
	
	public YamlDatabase getGameDatabase() {
		return database;
	}
	
	public synchronized IStatisticDatabase getStatisticDatabase() {
		return statisticDatabase;
	}
	
	private void registerEvents() {
		PluginManager pm = this.getServer().getPluginManager();
		
		pm.registerEvents(new SelectionListener(this), this);
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new UpdateListener(), this);
		pm.registerEvents(new SignWallListener(), this);
		pm.registerEvents(new QueuesListener(), this);
		pm.registerEvents(new PVPTimerListener(), this);
		pm.registerEvents(new HUBPortalListener(), this);
		pm.registerEvents(new ReadyListener(), this);
		pm.registerEvents(new StatisticAccountListener(), this);
		pm.registerEvents(SpleefSignExecutor.getInstance(), this);
		
		Hook<TagAPI> tagAPIHook = hooks.getService(TagAPIHook.class);
		if (tagAPIHook.hasHook()) {
			pm.registerEvents(new TagListener(), this);
		}
	}
	
	private void registerSigns() {
		SpleefSignExecutor executor = SpleefSignExecutor.getInstance();
		
		executor.registerSign(new SpleefSignJoin());
		executor.registerSign(new SpleefSignLeave());
		executor.registerSign(new SpleefSignStart());
		executor.registerSign(new SpleefSignHub());
		executor.registerSign(new SpleefSignSpectate());
		executor.registerSign(new SpleefSignVote());
	}
	
	private void setupStatisticDatabase() {
		String type = this.getConfig().getString("statistic.dbType");
		
		if (type.equalsIgnoreCase("yaml")) {
			this.statisticDatabase = new YamlStatisticDatabase();
		} else if (type.equalsIgnoreCase("mysql")) {
			this.statisticDatabase = new MySQLStatisticDatabase();
		} else {
			Logger.warning("Invalid statistic database type! Setting to YAML...");
			this.statisticDatabase = new YamlStatisticDatabase();
		}
	}
	
	private void initUpdate() {
		//Don't check for updates if the user has disabled this function
		if (!getConfig().getBoolean("auto-update"))
			return;
		
		this.updater = new Updater();
		
		//Makes sure the updater thread has completed its work
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
				if (updater.isUpdateAvailable()) {
					Logger.info("A new version of HeavySpleef is available: " + updater.getFileTitle());
					Logger.info("If you wish to update type /spleef update");
				}
			}
		}, 50L);
		
	}
	
	private void startMetrics() {
		try {
			Metrics m = new Metrics(HeavySpleef.this);
			m.start();
		} catch (IOException e) {
			Logger.info("An error occured while submitting stats to metrics...");
		}
	}
	
	public static FileConfiguration getSystemConfig() {
		return instance.getConfig();
	}
	
	public static GameAPI getAPI() {
		return GameAPI.getInstance();
	}
	
	public static void debug(String msg) {
		System.out.println("[HeavySpleef] [Debug] " + msg);
	}
	
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}
	
	public void startAntiCampingTask() {
		if (!getConfig().getBoolean("anticamping.enabled"))
			return;
		
		this.antiCampTid = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AntiCampingTask(), 0L, 20L);
	}
	
	public JoinGUI getJoinGUI() {
		return menu;
	}
	
}