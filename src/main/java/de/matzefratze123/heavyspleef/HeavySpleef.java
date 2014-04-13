/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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

import de.matzefratze123.api.hs.command.CommandExecutorService;
import de.matzefratze123.api.hs.sql.AbstractDatabase;
import de.matzefratze123.api.hs.sql.MySQLDatabase;
import de.matzefratze123.api.hs.sql.SQLiteDatabase;
import de.matzefratze123.heavyspleef.api.GameManagerAPI;
import de.matzefratze123.heavyspleef.api.IGameManager;
import de.matzefratze123.heavyspleef.command.CommandAddFloor;
import de.matzefratze123.heavyspleef.command.CommandAddLose;
import de.matzefratze123.heavyspleef.command.CommandAddScoreBoard;
import de.matzefratze123.heavyspleef.command.CommandAddTeam;
import de.matzefratze123.heavyspleef.command.CommandAddWall;
import de.matzefratze123.heavyspleef.command.CommandCreate;
import de.matzefratze123.heavyspleef.command.CommandDelete;
import de.matzefratze123.heavyspleef.command.CommandDisable;
import de.matzefratze123.heavyspleef.command.CommandEnable;
import de.matzefratze123.heavyspleef.command.CommandFlag;
import de.matzefratze123.heavyspleef.command.CommandHelp;
import de.matzefratze123.heavyspleef.command.CommandInfo;
import de.matzefratze123.heavyspleef.command.CommandJoin;
import de.matzefratze123.heavyspleef.command.CommandKick;
import de.matzefratze123.heavyspleef.command.CommandLeave;
import de.matzefratze123.heavyspleef.command.CommandList;
import de.matzefratze123.heavyspleef.command.CommandRegenerate;
import de.matzefratze123.heavyspleef.command.CommandReload;
import de.matzefratze123.heavyspleef.command.CommandRemoveFloor;
import de.matzefratze123.heavyspleef.command.CommandRemoveLose;
import de.matzefratze123.heavyspleef.command.CommandRemoveScoreBoard;
import de.matzefratze123.heavyspleef.command.CommandRemoveTeam;
import de.matzefratze123.heavyspleef.command.CommandRemoveWall;
import de.matzefratze123.heavyspleef.command.CommandRename;
import de.matzefratze123.heavyspleef.command.CommandSave;
import de.matzefratze123.heavyspleef.command.CommandSpectate;
import de.matzefratze123.heavyspleef.command.CommandStart;
import de.matzefratze123.heavyspleef.command.CommandStats;
import de.matzefratze123.heavyspleef.command.CommandStop;
import de.matzefratze123.heavyspleef.command.CommandTeamFlag;
import de.matzefratze123.heavyspleef.command.CommandUpdate;
import de.matzefratze123.heavyspleef.command.CommandVote;
import de.matzefratze123.heavyspleef.command.RootCommand;
import de.matzefratze123.heavyspleef.command.handler.FlagTransformer;
import de.matzefratze123.heavyspleef.command.handler.GameTransformer;
import de.matzefratze123.heavyspleef.config.SpleefConfig;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.task.TaskAntiCamping;
import de.matzefratze123.heavyspleef.database.YamlDatabase;
import de.matzefratze123.heavyspleef.hooks.Hook;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.TagAPIHook;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.listener.PlayerListener;
import de.matzefratze123.heavyspleef.listener.QueuesListener;
import de.matzefratze123.heavyspleef.listener.ReadyListener;
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
import de.matzefratze123.heavyspleef.stats.AccountException;
import de.matzefratze123.heavyspleef.stats.IStatisticDatabase;
import de.matzefratze123.heavyspleef.stats.SQLStatisticDatabase;
import de.matzefratze123.heavyspleef.stats.YamlConverter;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Logger;
import de.matzefratze123.heavyspleef.util.Metrics;
import de.matzefratze123.heavyspleef.util.SpleefLogger;
import de.matzefratze123.heavyspleef.util.Updater;

public class HeavySpleef extends JavaPlugin implements Listener {
	
	public static String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Spleef" + ChatColor.DARK_GRAY + "]";
	public static final String MAIN_COMMAND = "spleef";
	public static final String[] COMMANDS = new String[] {"spleef", "spl", "hspleef"};
	public static final String PLUGIN_NAME = "HeavySpleef";
	public static final Random RANDOM = new Random();
	
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
	private TaskAntiCamping antiCampTask;
	
	// Updater
	private Updater updater;
	
	//List of online players
	private List<SpleefPlayer> players = new ArrayList<SpleefPlayer>();
	
	private CommandExecutorService ces;
	
	@Override
	public void onLoad() {
		//Set the instance first
		instance = this;
	}
	
	@Override
	public void onEnable() {
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
		
		//Check tagapi version
		if (getServer().getPluginManager().getPlugin("TagAPI") != null) {
			try {
				Class.forName("org.kitteh.tag.AsyncPlayerReceiveNameTagEvent");
			} catch (ClassNotFoundException e) {
				//Ooops, user hasn't installed the latest release of tagapi
				Logger.info("Warning: Found an outdated version of TagAPI. Please update your TagAPI to v3.0 in order to use team games!");
			}
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
		
		antiCampTask = new TaskAntiCamping();
		antiCampTask.start();
		
		//Command stuff
		ces = new CommandExecutorService(MAIN_COMMAND, this);
		ces.registerTransformer(Game.class, new GameTransformer());
		ces.registerTransformer(Flag.class, new FlagTransformer());
		ces.setRootCommandExecutor(new RootCommand());
		ces.setUnknownCommandMessage(I18N._("unknownCommand"));
		
		registerCommands();
		
		Logger.info("HeavySpleef v" + getDescription().getVersion() + " activated!");
	}

	@Override
	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);
		
		if (!noWorldEdit) {
			this.database.save();
			
			try {
				this.statisticDatabase.saveAccounts();
			} catch (AccountException e) {
				Logger.severe("Failed to save statistics.");
				e.printStackTrace();
			}
		}
		
		SpleefLogger.logRaw("Stopping plugin!");
		
		Logger.info("HeavySpleef disabled!");
	}
	
	public static HeavySpleef getInstance() {
		return instance;
	}
	
	public static Random getRandom() {
		return RANDOM;
	}
	
	public CommandExecutorService getCommandExecutorService() {
		return ces;
	}
	
	public Updater getUpdater() {
		return updater;
	}
	
	public YamlDatabase getGameDatabase() {
		return database;
	}
	
	public IStatisticDatabase getStatisticDatabase() {
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
	
	public TaskAntiCamping getAntiCampingTask() {
		return antiCampTask;
	}
	
	public InventoryJoinGUI getJoinGUI() {
		return joinGui;
	}
	
	private void registerCommands() {
		ces.registerListener(new CommandAddFloor());
		ces.registerListener(new CommandAddLose());
		ces.registerListener(new CommandAddScoreBoard());
		ces.registerListener(new CommandAddTeam());
		ces.registerListener(new CommandAddWall());
		ces.registerListener(new CommandCreate());
		ces.registerListener(new CommandDelete());
		ces.registerListener(new CommandDisable());
		ces.registerListener(new CommandEnable());
		ces.registerListener(new CommandFlag());
		ces.registerListener(new CommandHelp());
		ces.registerListener(new CommandInfo());
		ces.registerListener(new CommandJoin());
		ces.registerListener(new CommandKick());
		ces.registerListener(new CommandList());
		ces.registerListener(new CommandLeave());
		ces.registerListener(new CommandRegenerate());
		ces.registerListener(new CommandReload());
		ces.registerListener(new CommandRemoveFloor());
		ces.registerListener(new CommandRemoveLose());
		ces.registerListener(new CommandRemoveScoreBoard());
		ces.registerListener(new CommandRemoveTeam());
		ces.registerListener(new CommandRemoveWall());
		ces.registerListener(new CommandRename());
		ces.registerListener(new CommandSave());
		ces.registerListener(new CommandSpectate());
		ces.registerListener(new CommandStart());
		ces.registerListener(new CommandStats());
		ces.registerListener(new CommandStop());
		ces.registerListener(new CommandTeamFlag());
		ces.registerListener(new CommandUpdate());
		ces.registerListener(new CommandVote());
	}
	
	private void registerEvents() {
		PluginManager pm = this.getServer().getPluginManager();
		
		pm.registerEvents(this, this);
		pm.registerEvents(new SelectionListener(this), this);
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new UpdateListener(), this);
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
				database = new MySQLDatabase(getLogger(), host, port, databaseName, user, password);
			} else {
				database = new SQLiteDatabase(getLogger(), SQLStatisticDatabase.SQLITE_FILE);	
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
	
	public synchronized SpleefPlayer getSpleefPlayer(Object base) {
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
			player.loadStatistics();
		}
		
		return player;
	}
	
	public SpleefPlayer[] getOnlineSpleefPlayers() {
		SpleefPlayer[] playersArray = new SpleefPlayer[players.size()];
		
		synchronized (players) {
			for (int i = 0; i < players.size(); i++) {
				playersArray[i] = players.get(i);
			}
		}
		
		return playersArray;
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