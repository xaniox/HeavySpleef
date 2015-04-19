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
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import de.matzefratze123.heavyspleef.commands.base.CommandManager;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.ConfigurationObject;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.ThrowingConfigurationObject.UnsafeException;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall;
import de.matzefratze123.heavyspleef.core.extension.ExtensionRegistry;
import de.matzefratze123.heavyspleef.core.extension.JoinSignExtension;
import de.matzefratze123.heavyspleef.core.extension.LeaveSignExtension;
import de.matzefratze123.heavyspleef.core.extension.StartSignExtension;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.module.Module;
import de.matzefratze123.heavyspleef.core.module.ModuleManager;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.player.PlayerManager;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public final class HeavySpleef {
	
	public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Spleef" + ChatColor.DARK_GRAY + "] ";
	
	@Getter
	private final JavaPlugin plugin;
	@Getter
	private final Logger logger;
	
	private Map<ConfigType, ConfigurationObject> configurations;
	
	private ModuleManager moduleManager;
	
	@Getter
	private FlagRegistry flagRegistry;
	@Getter
	private ExtensionRegistry extensionRegistry;
	@Getter
	@Setter
	private CommandManager commandManager;
	@Getter
	@Setter
	private AsyncReadWriteHandler databaseHandler;

	@Getter
	private HookManager hookManager;
	@Getter
	private GameManager gameManager;
	@Getter
	private PlayerManager playerManager;
	@Getter
	private BukkitListener bukkitListener;
	@Getter
	private RegionVisualizer regionVisualizer;
	private File localeDir;
	
	@Getter
	private PlayerPostActionHandler postActionHandler;
	
	public HeavySpleef(JavaPlugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}
	
	public void load() {
		File dataFolder = getDataFolder();
		
		localeDir = new File(dataFolder, "locale");
		localeDir.mkdirs();
		File layoutDir = new File(dataFolder, "layout");
		layoutDir.mkdirs();
		File flagDir = new File(getDataFolder(), "flags");
		flagDir.mkdirs();
		
		flagRegistry = new FlagRegistry(this, flagDir);
		
		configurations = new EnumMap<ConfigType, ConfigurationObject>(ConfigType.class);
		loadConfigurations();
		
		moduleManager = new ModuleManager();
				
		DefaultConfig defaultConfig = getConfiguration(ConfigType.DEFAULT_CONFIG);
		I18N.initialize(defaultConfig.getLocalization().getLocale(), localeDir, logger);
		
		playerManager = new PlayerManager(plugin);
		hookManager = new HookManager();
		
		hookManager.registerHook(HookReference.VAULT);
		hookManager.registerHook(HookReference.WORLDEDIT);
		
		postActionHandler = new PlayerPostActionHandler(this);
		
		regionVisualizer = new RegionVisualizer(getPlugin());
	}
	
	public void enable() {
		gameManager = new GameManager(databaseHandler);
		
		extensionRegistry = new ExtensionRegistry(this);
		extensionRegistry.registerExtension(ExtensionLobbyWall.class);
		extensionRegistry.registerExtension(JoinSignExtension.class);
		extensionRegistry.registerExtension(LeaveSignExtension.class);
		extensionRegistry.registerExtension(StartSignExtension.class);
		
		flagRegistry.flushAndExecuteInitMethods();
		
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
		
		bukkitListener = new BukkitListener(playerManager, gameManager, plugin);
		BasicTask loseCheckTask = new LoseCheckerTask(plugin, gameManager);
		loseCheckTask.start();
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
			
			ConfigurationObject obj;
			
			try {
				obj = type.newConfigInstance(config, args.get(type));
			} catch (UnsafeException ex) {
				Throwable cause = ex.getCause();
				
				logger.log(Level.SEVERE, "Could not create config structure for " + destinationFile.getPath() + ", except errors: ", cause);
				continue;
			}
			
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
	
	public void reload() {
		loadConfigurations();
		DefaultConfig config = getConfiguration(ConfigType.DEFAULT_CONFIG);
		Locale locale = config.getLocalization().getLocale();
		
		I18N.setDefaultLocale(locale);
		I18N.getInstance().reload();
		
		moduleManager.reloadModules();
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
		
		throw new IllegalArgumentException("base must be an instance of Player, String or UUID");
	}
	
	public void registerModule(Module module) {
		moduleManager.registerModule(module);
	}

}
