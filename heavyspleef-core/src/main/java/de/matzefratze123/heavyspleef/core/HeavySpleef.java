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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import de.matzefratze123.heavyspleef.commands.base.CommandManager;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.commands.base.DefaultCommandExecution;
import de.matzefratze123.heavyspleef.core.Updater.CheckResult;
import de.matzefratze123.heavyspleef.core.Updater.Version;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.ConfigurationObject;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.GeneralSection;
import de.matzefratze123.heavyspleef.core.config.ThrowingConfigurationObject.UnsafeException;
import de.matzefratze123.heavyspleef.core.config.UpdateSection;
import de.matzefratze123.heavyspleef.core.event.GlobalEventBus;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall;
import de.matzefratze123.heavyspleef.core.extension.ExtensionRegistry;
import de.matzefratze123.heavyspleef.core.extension.JoinSignExtension;
import de.matzefratze123.heavyspleef.core.extension.LeaveSignExtension;
import de.matzefratze123.heavyspleef.core.extension.StartSignExtension;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry.InitializationPolicy;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.I18N.LoadingMode;
import de.matzefratze123.heavyspleef.core.i18n.I18NBuilder;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.module.LoadPolicy.Lifecycle;
import de.matzefratze123.heavyspleef.core.module.Module;
import de.matzefratze123.heavyspleef.core.module.ModuleManager;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.player.PlayerManager;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public final class HeavySpleef {
	
	private static final String I18N_CLASSPATH_FOLDER = "i18n/";
	
	private Map<ConfigType, ConfigurationObject> configurations;
	private ModuleManager moduleManager;
	private File localeDir;
	
	private @Getter final JavaPlugin plugin;
	private @Getter final Logger logger;
	
	private @Getter String spleefPrefix;
	private @Getter FlagRegistry flagRegistry;
	private @Getter ExtensionRegistry extensionRegistry;
	private @Getter @Setter CommandManager commandManager;
	private @Getter @Setter AsyncReadWriteHandler databaseHandler;

	private @Getter HookManager hookManager;
	private @Getter GameManager gameManager;
	private @Getter PlayerManager playerManager;
	private @Getter BukkitListener bukkitListener;
	private @Getter RegionVisualizer regionVisualizer;
	private @Getter Updater updater;
	private @Getter PlayerPostActionHandler postActionHandler;
	private @Getter GlobalEventBus globalEventBus;
	private @Getter I18NManager i18NManager;
	private @Getter boolean gamesLoaded;
	private Set<GamesLoadCallback> gamesLoadCallbacks;
	
	public HeavySpleef(JavaPlugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.moduleManager = new ModuleManager();
	}
	
	public void load() {
		File dataFolder = getDataFolder();
		
		localeDir = new File(dataFolder, "locale");
		localeDir.mkdirs();
		File layoutDir = new File(dataFolder, "layout");
		layoutDir.mkdirs();
		
		gamesLoadCallbacks = Sets.newLinkedHashSet();
		flagRegistry = new FlagRegistry(this);
		
		configurations = new EnumMap<ConfigType, ConfigurationObject>(ConfigType.class);
		loadConfigurations();
				
		DefaultConfig defaultConfig = getConfiguration(ConfigType.DEFAULT_CONFIG);
		Locale locale = defaultConfig.getLocalization().getLocale();
		I18NBuilder builder = I18NBuilder.builder()
				.setLoadingMode(LoadingMode.FILE_SYSTEM)
				.setLocale(locale)
				.setFileSystemFolder(localeDir)
				.setClasspathFolder(I18N_CLASSPATH_FOLDER)
				.setLogger(logger);
		
		I18NManager.setGlobalBuilder(builder);
		i18NManager = new I18NManager();
		
		playerManager = new PlayerManager(this);
		hookManager = new HookManager();
		
		hookManager.registerHook(HookReference.VAULT);
		hookManager.registerHook(HookReference.WORLDEDIT);
		
		postActionHandler = new PlayerPostActionHandler(this);
		regionVisualizer = new RegionVisualizer(getPlugin());
		globalEventBus = new GlobalEventBus(logger);
	}
	
	public void enable() {
		gameManager = new GameManager(this);
		
		extensionRegistry = new ExtensionRegistry(this);
		extensionRegistry.registerExtension(ExtensionLobbyWall.class);
		extensionRegistry.registerExtension(JoinSignExtension.class);
		extensionRegistry.registerExtension(LeaveSignExtension.class);
		extensionRegistry.registerExtension(StartSignExtension.class);
		
		flagRegistry.flushAndExecuteInitMethods();
		flagRegistry.setInitializationPolicy(InitializationPolicy.REGISTER);
		
		//Load all games
		databaseHandler.getGames(new FutureCallback<List<Game>>() {
			
			@Override
			public void onSuccess(List<Game> result) {
				for (Game game : result) {
					gameManager.addGame(game, false);
				}
				
				gamesLoaded = true;
				for (GamesLoadCallback callback : gamesLoadCallbacks) {
					callback.onGamesLoaded(result);
				}
			}
			
			@Override
			public void onFailure(Throwable t) {
				logger.log(Level.SEVERE, "Could not load games from database", t);
			}
		});
		
		bukkitListener = new BukkitListener(playerManager, gameManager, plugin);
		LoseCheckerTask loseCheckTask = new LoseCheckerTask(this);
		globalEventBus.registerGlobalListener(loseCheckTask);
		loseCheckTask.start();
		
		DefaultConfig config = getConfiguration(ConfigType.DEFAULT_CONFIG);
		GeneralSection generalSection = config.getGeneralSection();
		UpdateSection updateSection = config.getUpdateSection();
		
		CommandManagerService service = commandManager.getService();
		DefaultCommandExecution execution = service.getExecution();
		spleefPrefix = generalSection.getSpleefPrefix() + " ";
		execution.setPrefix(spleefPrefix);
		
		//Only check for updates when the user hasn't
		//disabled it in the configuration
		if (updateSection.isUpdateChecking()) {
			//Check for updates
			updater = new Updater(plugin);
			updater.check(new FutureCallback<Updater.CheckResult>() {
	
				@Override
				public void onSuccess(CheckResult result) {
					if (result.isUpdateAvailable()) {
						Version version = result.getVersion();
						
						getLogger().log(Level.INFO, "Found a new update for HeavySpleef [v" + version + "]!");
						getLogger().log(Level.INFO, "Please remember to check for config & database compatibility issues which may occur when you update to the latest version");
						getLogger().log(Level.INFO, "Use '/spleef update' to update to the latest version of HeavySpleef");
					}
				}
	
				@Override
				public void onFailure(Throwable t) {
					getLogger().log(Level.WARNING, "Could not check for latest updates: " + t);
				}
			});
		}
		
		try {
			Metrics metrics = new Metrics(getPlugin());
			metrics.start();
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to start metrics: " + e);
		}
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
	
	private void loadConfigurations() {
		Map<ConfigType, Object[]> configArgs = new EnumMap<ConfigType, Object[]>(ConfigType.class);
		configArgs.put(ConfigType.DATABASE_CONFIG, new Object[] { getDataFolder() });
		
		prepareConfigurations(configArgs);
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
			
			ConfigurationObject obj = configurations.get(type);
			if (obj == null) {
				try {
					obj = type.newConfigInstance(config, args.get(type));
				} catch (UnsafeException ex) {
					Throwable cause = ex.getCause();
					
					logger.log(Level.SEVERE, "Could not create config structure for " + destinationFile.getPath() + ", except errors: ", cause);
					continue;
				}
				
				configurations.put(type, obj);
			} else {
				obj.inflate(config, args.get(type));
			}
			
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
	
	public void reload() {
		loadConfigurations();
		DefaultConfig config = getConfiguration(ConfigType.DEFAULT_CONFIG);
		Locale locale = config.getLocalization().getLocale();
		
		i18NManager.reloadAll(locale);
		moduleManager.reloadModules();
		
		GeneralSection generalSection = config.getGeneralSection();
		CommandManagerService service = commandManager.getService();
		DefaultCommandExecution execution = service.getExecution();
		spleefPrefix = generalSection.getSpleefPrefix() + " ";
		
		execution.setPrefix(spleefPrefix);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ConfigurationObject> T getConfiguration(ConfigType type) {
		return (T) configurations.get(type);
	}
	
	public File getDataFolder() {
		return plugin.getDataFolder();
	}
	
	public SpleefPlayer getSpleefPlayer(Object base) {
		if (base instanceof Player) {
			return playerManager.getSpleefPlayer((Player)base);
		} else if (base instanceof String) {
			return playerManager.getSpleefPlayer((String)base);
		} else if (base instanceof UUID) {
			return playerManager.getSpleefPlayer((UUID)base);
		}
		
		throw new IllegalArgumentException("base must be an instance of Player, String or UUID but is '" + base.getClass().getCanonicalName() + "'");
	}
	
	public void registerModule(Module module) {
		moduleManager.registerModule(module);
	}
	
	public void enableModules(Lifecycle lifecycle) {
		moduleManager.enableModules(lifecycle);
	}
	
	public void addGamesLoadCallback(GamesLoadCallback callback) {
		gamesLoadCallbacks.add(callback);
	}
	
	public interface GamesLoadCallback {
		
		public void onGamesLoaded(List<Game> games);
		
	}

}
