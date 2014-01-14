/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.TagAPI;

import de.matzefratze123.api.sql.AbstractDatabase;
import de.matzefratze123.api.sql.MySQLDatabase;
import de.matzefratze123.api.sql.SQLiteDatabase;
import de.matzefratze123.heavyspleef.api.GameManagerAPI;
import de.matzefratze123.heavyspleef.api.IGameManager;
import de.matzefratze123.heavyspleef.command.handler.CommandHandler;
import de.matzefratze123.heavyspleef.config.SpleefConfig;
import de.matzefratze123.heavyspleef.core.task.AntiCampingTask;
import de.matzefratze123.heavyspleef.database.YamlDatabase;
import de.matzefratze123.heavyspleef.hooks.Hook;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.TagAPIHook;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.listener.PlayerListener;
import de.matzefratze123.heavyspleef.listener.QueuesListener;
import de.matzefratze123.heavyspleef.listener.ReadyListener;
import de.matzefratze123.heavyspleef.listener.SignWallListener;
import de.matzefratze123.heavyspleef.listener.TagListener;
import de.matzefratze123.heavyspleef.listener.UpdateListener;
import de.matzefratze123.heavyspleef.objects.InventoryJoinGUI;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.selection.SelectionListener;
import de.matzefratze123.heavyspleef.selection.SelectionManager;
import de.matzefratze123.heavyspleef.signs.SpleefSignExecutor;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignJoin;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignLeave;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignSpectate;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignStart;
import de.matzefratze123.heavyspleef.signs.signobjects.SpleefSignVote;
import de.matzefratze123.heavyspleef.stats.IStatisticDatabase;
import de.matzefratze123.heavyspleef.stats.SQLStatisticDatabase;
import de.matzefratze123.heavyspleef.stats.StatisticModule;
import de.matzefratze123.heavyspleef.stats.YamlConverter;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Logger;
import de.matzefratze123.heavyspleef.util.Metrics;
import de.matzefratze123.heavyspleef.util.SpleefLogger;
import de.matzefratze123.heavyspleef.util.Updater;

public class HeavySpleef extends JavaPlugin implements Listener {
	
	public static String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Spleef" + ChatColor.DARK_GRAY + "]";
	public static final String[] COMMANDS = new String[] {"spleef", "spl", "hspleef"};
	public static final String PLUGIN_NAME = "HeavySpleef";
	public static final Random random = new Random();
	
	//Instance
	private static HeavySpleef instance;
	private static boolean noWorldEdit;
	
	//Object instances start
	private SpleefConfig config;
	private YamlDatabase database;
	private IStatisticDatabase statisticDatabase;
	private SelectionManager selectionManager;
	private InventoryJoinGUI joinGui;
	
	//Tasks
	private AntiCampingTask antiCampTask;
	
	// Updater
	private Updater updater;
	
	//List of online players
	private List<SpleefPlayer> players = new ArrayList<SpleefPlayer>();
	
	@Override
	public void onEnable() {
		//Set the instance first
		instance = this;
		
		if (!HookManager.getInstance().getService(WorldEditHook.class).hasHook()) {
			Logger.warning("WARNING !!! Failed to detect WorldEdit !!! WARNING");
			Logger.warning(" In order to use HeavySpleef make sure to install ");
			Logger.warning("       WorldEdit before using this plugin!        ");
			Logger.warning(" ");
			Logger.warning("Disabling HeavySpleef due to no WorldEdit.");
			
			noWorldEdit = true;
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		getDataFolder().mkdirs();
		
		config = new SpleefConfig();
		I18N.loadLanguageFiles();
		
		selectionManager = new SelectionManager();
		database = new YamlDatabase();
		database.load();
		
		PREFIX = config.getGeneralSection().getPrefix();
		
		joinGui = new InventoryJoinGUI();
		initStatisticDatabase();
		
		SpleefLogger.logRaw("Starting plugin version " + getDescription().getVersion() + "!");
		
		//Start metrics
		startMetrics();
		
		initUpdate();
		registerEvents();
		registerSigns();
		
		//Register our main command
		getCommand("spleef").setExecutor(new CommandHandler());
		
		antiCampTask = new AntiCampingTask();
		antiCampTask.start();
		
		//Command stuff
		CommandHandler.initCommands();
		CommandHandler.setPluginInstance(this);
		CommandHandler.setConfigInstance(this);
		
		Logger.info("HeavySpleef v" + getDescription().getVersion() + " activated!");
	}

	@Override
	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);
		
		if (!noWorldEdit) {
			this.database.save();
			this.statisticDatabase.saveAccounts();
		}
		
		SpleefLogger.logRaw("Stopping plugin!");
		
		Logger.info("HeavySpleef disabled!");
	}
	
	public static HeavySpleef getInstance() {
		return instance;
	}
	
	public static Random getRandom() {
		return random;
	}
	
	public Updater getUpdater() {
		return updater;
	}
	
	public YamlDatabase getGameDatabase() {
		return database;
	}
	
	public synchronized IStatisticDatabase getStatisticDatabase() {
		return statisticDatabase;
	}
	
	public static SpleefConfig getSystemConfig() {
		return instance.config;
	}
	
	public static IGameManager getAPI() {
		return GameManagerAPI.getInstance();
	}
	
	public static void debug(String msg) {
		System.out.println("[HeavySpleef] [Debug] " + msg);
	}
	
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}
	
	public AntiCampingTask getAntiCampingTask() {
		return antiCampTask;
	}
	
	public InventoryJoinGUI getJoinGUI() {
		return joinGui;
	}
	
	private void registerEvents() {
		PluginManager pm = this.getServer().getPluginManager();
		
		pm.registerEvents(this, this);
		pm.registerEvents(new SelectionListener(this), this);
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new UpdateListener(), this);
		pm.registerEvents(new SignWallListener(), this);
		pm.registerEvents(new QueuesListener(), this);
		pm.registerEvents(new ReadyListener(), this);
		pm.registerEvents(SpleefSignExecutor.getInstance(), this);
		
		Hook<TagAPI> tagAPIHook = HookManager.getInstance().getService(TagAPIHook.class);
		if (tagAPIHook.hasHook()) {
			pm.registerEvents(new TagListener(), this);
		}
	}
	
	private void registerSigns() {
		SpleefSignExecutor executor = SpleefSignExecutor.getInstance();
		
		executor.registerSign(new SpleefSignJoin());
		executor.registerSign(new SpleefSignLeave());
		executor.registerSign(new SpleefSignStart());
		executor.registerSign(new SpleefSignSpectate());
		executor.registerSign(new SpleefSignVote());
	}
	
	public void initStatisticDatabase() {
		if (SQLStatisticDatabase.isDatabaseEnabled()) {
			//Load authentication data
			String statsDB = config.getStatisticSection().getDatabaseType();
			String host = config.getStatisticSection().getHost();
			int port = config.getStatisticSection().getPort();
			String databaseName = config.getStatisticSection().getDbName();
			String user = config.getStatisticSection().getDbUser();
			String password = config.getStatisticSection().getDbPassword();
			
			AbstractDatabase database;
			if (statsDB.equalsIgnoreCase("mysql")) {
				database = new MySQLDatabase(this, host, port, databaseName, user, password);
			} else {
				database = new SQLiteDatabase(this, SQLStatisticDatabase.SQLITE_FILE);	
			}
			
			statisticDatabase = new SQLStatisticDatabase(database);
			
			//Convert old yaml statistics
			YamlConverter.convertYamlData();
		}
	}
	
	private void initUpdate() {
		//Don't check for updates if the user has disabled this function
		if (!config.getRootSection().isAutoUpdate())
			return;
		
		this.updater = new Updater();
		
		//Makes sure the updater thread has completed its work
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
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
	
	public SpleefPlayer getSpleefPlayer(Object base) {
		Player bukkitPlayer = null;
		
		if (base instanceof Player) {
			bukkitPlayer = (Player) base;
		} else if (base instanceof String) {
			bukkitPlayer = Bukkit.getPlayer((String)base);
		}
		
		if (bukkitPlayer == null) {
			return null;
		}
		
		SpleefPlayer player;
		
		for (SpleefPlayer pl : players) {
			if (pl.getBukkitPlayer() == bukkitPlayer) {
				return pl;
			}
		}
		
		//Player isn't registered yet
		player = new SpleefPlayer(bukkitPlayer);
		players.add(player);
		
		if (SQLStatisticDatabase.isDatabaseEnabled()) {
			StatisticModule module = statisticDatabase.loadAccount(player.getRawName());
			player.setStatistic(module);
		} else {
			player.setStatistic(new StatisticModule(player.getRawName()));
		}
		
		return player;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent e) {
		handleQuit(e);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onKick(PlayerKickEvent e) {
		handleQuit(e);
	}
	
	private void handleQuit(PlayerEvent e) {
		SpleefPlayer player = getSpleefPlayer(e.getPlayer());
		player.setOnline(false);
		players.remove(player);
	}
	
}