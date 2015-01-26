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
package de.matzefratze123.heavyspleef.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

import de.matzefratze123.heavyspleef.commands.base.CommandManager;
import de.matzefratze123.heavyspleef.core.FlagManager.DefaultGamePropertyBundle;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.ConfigurationObject;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.floor.SimpleCuboidFloor;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.Hooks;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.ParsedMessage;
import de.matzefratze123.heavyspleef.core.module.Module;
import de.matzefratze123.heavyspleef.core.module.ModuleManager;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.player.PlayerManager;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public final class HeavySpleef {
	
	public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Spleef" + ChatColor.DARK_GRAY + "] ";
	
	private final JavaPlugin plugin;
	private final Logger logger;
	
	private File localeDir;
	private File flagDir;
	
	private Map<ConfigType, ConfigurationObject> configurations;
	
	private ModuleManager moduleManager;
	private FlagRegistry flagRegistry;
	private I18N i18n;
	private CommandManager commandManager;
	private AsyncReadWriteHandler databaseHandler;

	private HookManager hookManager;
	private GameManager gameManager;
	private PlayerManager playerManager;
	
	public HeavySpleef(JavaPlugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}
	
	public void load() {
		File dataFolder = getDataFolder();
		
		this.localeDir = new File(dataFolder, "locale");
		this.localeDir.mkdirs();
		this.flagDir = new File(dataFolder, "flags");
		this.flagDir.mkdirs();
		
		this.configurations = new EnumMap<ConfigType, ConfigurationObject>(ConfigType.class);
		
		Map<ConfigType, Object[]> configArgs = new EnumMap<ConfigType, Object[]>(ConfigType.class);
		configArgs.put(ConfigType.DATABASE_CONFIG, new Object[] { getDataFolder() });
		
		prepareConfigurations(configArgs);
		
		this.moduleManager = new ModuleManager();
				
		DefaultConfig defaultConfig = getConfiguration(ConfigType.DEFAULT_CONFIG);
		this.i18n = new I18N(defaultConfig, localeDir, logger);
		this.playerManager = new PlayerManager(plugin);
		this.hookManager = new HookManager();
		
		hookManager.registerHook(Hooks.VAULT);
	}
	
	public void enable() {
		flagRegistry = new FlagRegistry(this, flagDir);
		
		//Load all games
		databaseHandler.getGames(new FutureCallback<List<Game>>() {
			
			@Override
			public void onSuccess(List<Game> result) {
				for (Game game : result) {
					gameManager.addGame(game, false);
				}
			}
			
			@Override
			public void onFailure(Throwable t) {
				logger.log(Level.SEVERE, "Could not load games from database", t);
			}
		});
		
		gameManager = new GameManager(databaseHandler);
	}
	
	public void disable() {
		moduleManager.disableModules();
		
		HandlerList.unregisterAll(plugin);
		
		ListenableFuture<?> future = databaseHandler.saveGames(gameManager.getGames(), null);
		
		try {
			//Wait for the task to be completed, as Bukkit  
			//does not mind async thread and just kills them
			future.get();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Server-Thread interrupted while saving games to database", e);
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, "Could not save games to database", e);
		}
	}
	
	private void prepareConfigurations(Map<ConfigType, Object[]> args) {
		for (ConfigType type : ConfigType.values()) {
			File destinationFile = new File(getDataFolder(), type.getDestinationFileName());
			YamlConfiguration config;
			
			try {
				if (!destinationFile.exists()) {
					URL resourceUrl = getClass().getResource(type.getClasspathResourceName());
					copyResource(resourceUrl, destinationFile);
				}
				
				config = new YamlConfiguration();
				config.load(destinationFile);
			} catch (IOException | InvalidConfigurationException e) {
				logger.log(Level.SEVERE, "Could not load configuration \"" + type.getDestinationFileName() + "\"", e);
				continue;
			}
			
			ConfigurationObject obj = type.newConfigInstance(config, args.get(type));			
			configurations.put(type, obj);
		}
	}
	
	public static void copyResource(URL resourceUrl, File destination) throws IOException {		
		URLConnection connection = resourceUrl.openConnection();
		
		if (!destination.exists()) {
			destination.getParentFile().mkdirs();
			destination.createNewFile();
		}
		
		final int bufferSize = 1024;
		
		try (InputStream inStream = connection.getInputStream(); 
				FileOutputStream outStream = new FileOutputStream(destination)) {
			byte[] buffer = new byte[bufferSize];
			
			int read;
			while ((read = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, read);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ConfigurationObject> T getConfiguration(ConfigType type) {
		return (T) configurations.get(type);
	}
	
	public File getDataFolder() {
		return plugin.getDataFolder();
	}
	
	public File getLocaleDir() {
		return localeDir;
	}
	
	public File getFlagDir() {
		return flagDir;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}
	
	public FlagRegistry getFlagRegistry() {
		return flagRegistry;
	}
	
	public GameManager getGameManager() {
		return gameManager;
	}
	
	public String getMessage(String key) {
		return i18n.getString(key);
	}
	
	public ParsedMessage getVarMessage(String key) {
		return i18n.getVarString(key);
	}
	
	public String[] getMessageArray(String key) {
		return i18n.getStringArray(key);
	}
	
	public SpleefPlayer getSpleefPlayer(Object base) {
		if (base instanceof Player) {
			return playerManager.getSpleefPlayer((Player)base);
		} else if (base instanceof String) {
			return playerManager.getSpleefPlayer((String)base);
		} else if (base instanceof UUID) {
			return playerManager.getSpleefPlayer((UUID)base);
		}
		
		throw new IllegalArgumentException("base must be an instance of Player, String or UUID");
	}
	
	public void registerModule(Module module) {
		moduleManager.registerModule(module);
	}
	
	public void setCommandManager(CommandManager manager) {
		this.commandManager = manager;
	}
	
	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	public void setDatabaseHandler(AsyncReadWriteHandler handler) {
		this.databaseHandler = handler;
	}
	
	public AsyncReadWriteHandler getDatabaseHandler() {
		return databaseHandler;
	}
	
	public HookManager getHookManager() {
		return hookManager;
	}
	
	public List<Class<?>> getPersistentBeans() {
		List<Class<?>> listOfClasses = new LinkedList<Class<?>>();
		//listOfClasses.add(Game.class);
		listOfClasses.add(Statistic.class);
		listOfClasses.add(FlagManager.class);
		listOfClasses.add(SimpleCuboidFloor.class);
		listOfClasses.add(CuboidRegion.class);
		listOfClasses.add(Vector.class);
		listOfClasses.add(AbstractFlag.class);
		listOfClasses.add(DefaultGamePropertyBundle.class);
		
		return listOfClasses;
	}

}
