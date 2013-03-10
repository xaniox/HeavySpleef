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

import java.io.IOException;
import java.util.HashMap;

import me.matzefratze123.heavyspleef.command.CommandHandler;
import me.matzefratze123.heavyspleef.core.AntiCampingTask;
import me.matzefratze123.heavyspleef.database.YamlDatabase;
import me.matzefratze123.heavyspleef.database.statistic.IStatisticDatabase;
import me.matzefratze123.heavyspleef.database.statistic.MySQLStatisticDatabase;
import me.matzefratze123.heavyspleef.database.statistic.YamlStatisticDatabase;
import me.matzefratze123.heavyspleef.hooks.HookManager;
import me.matzefratze123.heavyspleef.listener.PlayerListener;
import me.matzefratze123.heavyspleef.listener.SignListener;
import me.matzefratze123.heavyspleef.selection.SelectionListener;
import me.matzefratze123.heavyspleef.selection.SelectionManager;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;
import me.matzefratze123.heavyspleef.utility.Metrics;
import me.matzefratze123.heavyspleef.utility.PlayerState;
import me.matzefratze123.heavyspleef.utility.UpdateChecker;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeavySpleef extends JavaPlugin {
		
	private SelectionManager sel;
	public HashMap<String, PlayerState> playerStates = new HashMap<String, PlayerState>();
	
	public static FileConfig config;
	public static HeavySpleef instance;
	
	public static String PREFIX = "[Spleef]"; 
	public static HookManager hooks;

	public YamlDatabase database;
	public IStatisticDatabase statisticDatabase;
	
	@Override
	public void onEnable() {
		hooks = new HookManager();
		sel = new SelectionManager();
		instance = this;
		config = new FileConfig(this);
		database = new YamlDatabase();
		database.load();
		PREFIX = getConfig().getString("general.spleef-prefix");
		
		LanguageHandler.loadLanguageFiles();
		UpdateChecker.check();
		
		this.setupStatisticDatabase();
		this.statisticDatabase.load();
		this.startMetrics();
		this.registerEvents();
		this.getCommand("spleef").setExecutor(new CommandHandler());
		
		if (getConfig().getBoolean("anticamping.enabled"))
			this.startAntiCampingTask();
		if (getConfig().getBoolean("general.saveInIntervall"))
			this.startSaveTask();
		
		CommandHandler.initCommands();
		CommandHandler.setPluginInstance(this);
		CommandHandler.setConfigInstance(this);
		
		this.getLogger().info("HeavySpleef v" + getDescription().getVersion() + " activated!");
	}

	@Override
	public void onDisable() {
		
		this.getServer().getScheduler().cancelTasks(this);
		this.database.save(true);
		this.statisticDatabase.save();
		this.getLogger().info("HeavySpleef deactivated!");
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
	
	public SelectionManager getSelectionManager() {
		return sel;
	}
	
	private void registerEvents() {
		PluginManager pm = this.getServer().getPluginManager();
		
		pm.registerEvents(new SignListener(), this);
		pm.registerEvents(new SelectionListener(this), this);
		pm.registerEvents(new PlayerListener(), this);
	}
	
	private void startMetrics() {
		try {
			Metrics m = new Metrics(this);
			m.start();
			this.getLogger().info("Metrics started...");
		} catch (IOException e) {
			this.getLogger().info("An error occured on submitting stats to metrics...");
		}
	}
	
	public void startAntiCampingTask() {
		boolean warn = getConfig().getBoolean("anticamping.campWarn");
		int warnAt = getConfig().getInt("anticamping.warnAt");
		int teleportAt = getConfig().getInt("anticamping.teleportAt");
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AntiCampingTask(warn, warnAt, teleportAt), 0L, 20L);
	}
	
	public void startSaveTask() {
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				database.save(false);
				statisticDatabase.save();
			}
		}, 0L, getConfig().getInt("general.saveIntervall") * 20L * 60L);
	}
	
}