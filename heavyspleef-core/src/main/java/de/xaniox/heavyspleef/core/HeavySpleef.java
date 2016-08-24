/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import de.xaniox.heavyspleef.commands.base.CommandManager;
import de.xaniox.heavyspleef.commands.base.CommandManagerService;
import de.xaniox.heavyspleef.commands.base.DefaultCommandExecution;
import de.xaniox.heavyspleef.core.config.*;
import de.xaniox.heavyspleef.core.event.GlobalEventBus;
import de.xaniox.heavyspleef.core.extension.*;
import de.xaniox.heavyspleef.core.flag.*;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.game.JoinRequester;
import de.xaniox.heavyspleef.core.game.LoseCheckerTask;
import de.xaniox.heavyspleef.core.hook.HookManager;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NBuilder;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.module.LoadPolicy;
import de.xaniox.heavyspleef.core.module.Module;
import de.xaniox.heavyspleef.core.module.ModuleManager;
import de.xaniox.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.xaniox.heavyspleef.core.player.PlayerManager;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.mcstats.Metrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HeavySpleef {
	
	private static final String I18N_CLASSPATH_FOLDER = "i18n/";
	
	private Map<ConfigType, ConfigurationObject> configurations;
	private ModuleManager moduleManager;
	private File localeDir;
	
	private final JavaPlugin plugin;
	private final Logger logger;
	
	private String spleefPrefix;
	private String vipPrefix;
	private FlagRegistry flagRegistry;
	private ExtensionRegistry extensionRegistry;
	private CommandManager commandManager;
	private AsyncReadWriteHandler databaseHandler;

	private HookManager hookManager;
	private GameManager gameManager;
	private PlayerManager playerManager;
	private BukkitListener bukkitListener;
	private RegionVisualizer regionVisualizer;
	private Updater updater;
	private PlayerPostActionHandler postActionHandler;
	private GlobalEventBus globalEventBus;
	private I18NManager i18NManager;
	private boolean gamesLoaded;
	private JoinRequester.PvPTimerManager pvpTimerManager;
	private Metrics metrics;
	private Set<GamesLoadCallback> gamesLoadCallbacks;
	
	public HeavySpleef(JavaPlugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.moduleManager = new ModuleManager(logger);
	}
	
	public void load() {
		File dataFolder = getDataFolder();
		
		localeDir = new File(dataFolder, "locale");
		localeDir.mkdirs();
		File layoutDir = new File(dataFolder, "layout");
		layoutDir.mkdirs();
		
		MinecraftVersion.initialize(logger);
		
		gamesLoadCallbacks = Sets.newLinkedHashSet();
		flagRegistry = new FlagRegistry(this);
		
		File configFile = new File(dataFolder, ConfigType.DEFAULT_CONFIG.getDestinationFileName());
		if (configFile.exists()) {
			Configuration prevLoadConfig = YamlConfiguration.loadConfiguration(configFile);
			checkConfigVersions(prevLoadConfig, dataFolder.toPath());
		}
		
		configurations = new EnumMap<ConfigType, ConfigurationObject>(ConfigType.class);
		loadConfigurations();
		
		DefaultConfig defaultConfig = getConfiguration(ConfigType.DEFAULT_CONFIG);
		Locale locale = defaultConfig.getLocalization().getLocale();
		I18NBuilder builder = I18NBuilder.builder()
				.setLoadingMode(I18N.LoadingMode.FILE_SYSTEM)
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
		hookManager.registerHook(HookReference.PROTOCOLLIB);
		
		postActionHandler = new PlayerPostActionHandler(this);
		regionVisualizer = new RegionVisualizer(getPlugin());
		globalEventBus = new GlobalEventBus(logger);
		
		extensionRegistry = new ExtensionRegistry(this);
		
		try {
			metrics = new Metrics(getPlugin());
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Unable to create an instance of Metrics", e);
		}
	}
	
	public void enable() {
		gameManager = new GameManager(this);
		
		extensionRegistry.registerExtension(ExtensionLobbyWall.class);
		extensionRegistry.registerExtension(JoinSignExtension.class);
		extensionRegistry.registerExtension(LeaveSignExtension.class);
		extensionRegistry.registerExtension(StartSignExtension.class);
		
		flagRegistry.flushAndExecuteInitMethods();
		flagRegistry.setInitializationPolicy(FlagRegistry.InitializationPolicy.REGISTER);
		
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
		globalEventBus.registerListener(loseCheckTask);
		loseCheckTask.start();
		
		DefaultConfig config = getConfiguration(ConfigType.DEFAULT_CONFIG);
		GeneralSection generalSection = config.getGeneralSection();
		UpdateSection updateSection = config.getUpdateSection();
		
		CommandManagerService service = commandManager.getService();
		DefaultCommandExecution execution = service.getExecution();
		spleefPrefix = generalSection.getSpleefPrefix();
		execution.setPrefix(spleefPrefix);
		vipPrefix = generalSection.getVipPrefix();
		
		pvpTimerManager = new JoinRequester.PvPTimerManager(this);
		pvpTimerManager.setTicksNeeded(generalSection.getPvpTimer() * 20L);
		
		//Only check for updates when the user hasn't
		//disabled it in the configuration
		if (updateSection.isUpdateChecking()) {
			//Check for updates
			updater = new Updater(plugin);
			updater.check(new FutureCallback<Updater.CheckResult>() {
	
				@Override
				public void onSuccess(Updater.CheckResult result) {
					if (result.isUpdateAvailable()) {
						Updater.Version version = result.getVersion();
						
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
			metrics.start();
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to start metrics: " + e);
		}
	}
	
	public void disable() {
		gameManager.shutdown();
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
		
		HandlerList.unregisterAll(plugin);
		moduleManager.disableModules();
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
				} catch (ThrowingConfigurationObject.UnsafeException ex) {
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
		} else {
            destination.delete();
        }

        destination.createNewFile();
		
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
	
	private void checkConfigVersions(Configuration config, Path dataFolder) {
		if (config.getInt("config-version") < DefaultConfig.CURRENT_CONFIG_VERSION) {
			Path configSource = dataFolder.resolve(ConfigType.DEFAULT_CONFIG.getDestinationFileName());
			Path configTarget = dataFolder.resolve("config_old.yml");
			
			try {
				Files.move(configSource, configTarget, StandardCopyOption.REPLACE_EXISTING);
				URL configResource = getClass().getResource(ConfigType.DEFAULT_CONFIG.getClasspathResourceName());
				
				copyResource(configResource, configSource.toFile());
				
				ConsoleCommandSender sender = Bukkit.getConsoleSender();
				sender.sendMessage(ChatColor.RED + "Due to a HeavySpleef update your old configuration has been renamed");
				sender.sendMessage(ChatColor.RED + "to config_old.yml and a new one has been generated. Make sure to");
				sender.sendMessage(ChatColor.RED + "apply your old changes to the new config");
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not create updated configuration due to an IOException", e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
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
		vipPrefix = generalSection.getVipPrefix();
		
		execution.setPrefix(spleefPrefix);
		
		int pvpTimer = generalSection.getPvpTimer();
		pvpTimerManager.setTicksNeeded(pvpTimer * 20L);
		
		for (Game game : gameManager.getGames()) {
			JoinRequester requester = game.getJoinRequester();
			requester.setPvpTimerMode(pvpTimer > 0);
			
			List<AbstractFlag<?>> loadedFlags = Lists.newArrayList();
			Iterator<AbstractFlag<?>> iterator = game.getFlagManager().getFlags().iterator();
			while (iterator.hasNext()) {
				AbstractFlag<?> flag = iterator.next();
				
				if (flag instanceof UnloadedFlag) {
					UnloadedFlag unloaded = (UnloadedFlag) flag;
					if (!unloaded.validateLoad(flagRegistry)) {
						//This is not ready for load yet
						continue;
					}
					
					AbstractFlag<?> loaded = unloaded.loadFlag(flagRegistry);
					loadedFlags.add(loaded);
					game.removeFlag(unloaded.getClass());
					game.addFlag(loaded, false);
				} else {
					Class<? extends AbstractFlag<?>> clazz = (Class<? extends AbstractFlag<?>>) flag.getClass();
					if (flagRegistry.isFlagPresent(clazz)) {
						continue;
					}
					
					//This flag has been deactivated
					game.removeFlag(clazz);
					
					/* Generate a path */
					StringBuilder pathBuilder = new StringBuilder();
					Flag parentFlagData = clazz.getAnnotation(Flag.class);
					
					do {
						pathBuilder.insert(0, parentFlagData.name());
						
						Class<? extends AbstractFlag<?>> parentFlagClass = parentFlagData.parent();
						parentFlagData = parentFlagClass.getAnnotation(Flag.class);
						
						if (parentFlagData != null && parentFlagClass != NullFlag.class) {
							pathBuilder.insert(0, FlagRegistry.FLAG_PATH_SEPERATOR);
						}
					} while (parentFlagData != null);
					
					String path = pathBuilder.toString();
					
					Element element = DocumentHelper.createElement("flag");
					element.addAttribute("name", path);
					flag.marshal(element);
					
					UnloadedFlag unloaded = new UnloadedFlag();
					unloaded.setXmlElement(element);
					game.addFlag(unloaded, false);
				}
			}
			
			for (AbstractFlag<?> loaded : loadedFlags) {
				loaded.onFlagAdd(game);
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
	
	public void enableModules(LoadPolicy.Lifecycle lifecycle) {
		moduleManager.enableModules(lifecycle);
	}
	
	public void addGamesLoadCallback(GamesLoadCallback callback) {
		gamesLoadCallbacks.add(callback);
	}
	
	public ExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

	public void setExtensionRegistry(ExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public void setCommandManager(CommandManager commandManager) {
		this.commandManager = commandManager;
	}

	public AsyncReadWriteHandler getDatabaseHandler() {
		return databaseHandler;
	}

	public void setDatabaseHandler(AsyncReadWriteHandler databaseHandler) {
		this.databaseHandler = databaseHandler;
	}

	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}

	public Logger getLogger() {
		return logger;
	}

	public String getSpleefPrefix() {
		return spleefPrefix;
	}

	public String getVipPrefix() {
		return vipPrefix;
	}

	public FlagRegistry getFlagRegistry() {
		return flagRegistry;
	}

	public HookManager getHookManager() {
		return hookManager;
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	public BukkitListener getBukkitListener() {
		return bukkitListener;
	}

	public RegionVisualizer getRegionVisualizer() {
		return regionVisualizer;
	}

	public Updater getUpdater() {
		return updater;
	}

	public PlayerPostActionHandler getPostActionHandler() {
		return postActionHandler;
	}

	public GlobalEventBus getGlobalEventBus() {
		return globalEventBus;
	}

	public I18NManager getI18NManager() {
		return i18NManager;
	}

	public boolean isGamesLoaded() {
		return gamesLoaded;
	}

	public JoinRequester.PvPTimerManager getPvpTimerManager() {
		return pvpTimerManager;
	}

	public Metrics getMetrics() {
		return metrics;
	}
	
	public interface GamesLoadCallback {
		
		public void onGamesLoaded(List<Game> games);
		
	}

}