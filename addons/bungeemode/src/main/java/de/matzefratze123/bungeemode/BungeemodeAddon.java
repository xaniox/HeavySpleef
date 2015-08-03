/*
 * This file is part of addons.
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
package de.matzefratze123.bungeemode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.HeavySpleef.GamesLoadCallback;
import de.matzefratze123.heavyspleef.core.MinecraftVersion;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.flag.UnloadedFlag;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class BungeemodeAddon extends BasicAddOn {
	
	public static final String BUNGEECORD_CHANNEL = "BungeeCord";
	private static final String CONFIG_FILE_NAME = "config.yml";
	private static final String CONFIG_RESOURCE_NAME = "default_config.yml";
	
	private BungeemodeConfig config;
	private BungeemodeListener listener;
	private Set<SpleefPlayer> sendBackExceptions;
	private GamesLoadCallback callback = new GamesLoadCallback() {
		
		@Override
		public void onGamesLoaded(List<Game> games) {
			registerGameListener();
		}
	};
	
	@Override
	public void enable() {
		if (!MinecraftVersion.isSpigot()) {
			getLogger().warning("Bungeemode requires a Spigot server to operate!");
			return;
		}
		
		HeavySpleef heavySpleef = getHeavySpleef();
		
		try {
			File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);
			
			if (checkCopyConfig()) {
				copyResource(CONFIG_RESOURCE_NAME, configFile);
			}
			
			Configuration yamlConfig = YamlConfiguration.loadConfiguration(configFile);
			config = new BungeemodeConfig(yamlConfig);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to generate/load config", e);
		}
		
		if (!config.isEnabled()) {
			return;
		}
	
		Bukkit.getMessenger().registerOutgoingPluginChannel(heavySpleef.getPlugin(), BUNGEECORD_CHANNEL);
	
		this.sendBackExceptions = Sets.newHashSet();
		listener = new BungeemodeListener(this); 
		Bukkit.getPluginManager().registerEvents(listener, heavySpleef.getPlugin());

		if (heavySpleef.isGamesLoaded()) {
			callback.onGamesLoaded(null);
			
			//Fix for HeavySpleef version 2.1 or below
			//that doesn't load unloaded flags on add-on enable
			FlagRegistry reg = getHeavySpleef().getFlagRegistry();
			String path = reg.getFlagPath(FlagTeleportAll.class);
			
			for (Game game : heavySpleef.getGameManager().getGames()) {
				for (AbstractFlag<?> flag : game.getFlagManager().getFlags()) {
					if (!(flag instanceof UnloadedFlag)) {
						continue;
					}
					
					UnloadedFlag unloaded = (UnloadedFlag) flag;
					
					
					if (!unloaded.getFlagName().equals(path)) {
						continue;
					}
					
					game.removeFlag(path);
					
					FlagTeleportAll newFlag = reg.newFlagInstance(path, FlagTeleportAll.class, game);
					newFlag.unmarshal(unloaded.getXmlElement());
					
					game.addFlag(newFlag);
				}
			}
		} else {
			heavySpleef.addGamesLoadCallback(callback);
		} 
	}
	
	private void registerGameListener() {
		GameManager manager = getHeavySpleef().getGameManager();
		String gameName = config.getGame();
		
		if (manager.hasGame(gameName)) {
			Game game = manager.getGame(gameName);
			game.getEventBus().registerListener(listener);
		} else {
			getLogger().log(Level.WARNING, "Game " + gameName + " as specified in the add-on config does not exist!");
		}
	}
	
	@Override
	public void disable() {
		String gameName = config.getGame();
		GameManager manager = getHeavySpleef().getGameManager();
		if (manager.hasGame(gameName)) {
			Game game = manager.getGame(gameName);
			game.getEventBus().unregister(listener);
		}
		
		Bukkit.getMessenger().unregisterOutgoingPluginChannel(getHeavySpleef().getPlugin(), BUNGEECORD_CHANNEL);
		HandlerList.unregisterAll(listener);
		
		//Fix for HeavySpleef version 2.1 or below
		//that doesn't unload flags on add-on disable
		FlagRegistry reg = getHeavySpleef().getFlagRegistry();
		
		for (Game game : manager.getGames()) {
			if (!game.isFlagPresent(FlagTeleportAll.class)) {
				continue;
			}
			
			FlagTeleportAll flag = game.getFlag(FlagTeleportAll.class);
			game.removeFlag(flag.getClass());
			
			Element element = DocumentHelper.createElement("flag");
			element.addAttribute("name", reg.getFlagPath(FlagTeleportAll.class));
			flag.marshal(element);
			
			UnloadedFlag unloaded = new UnloadedFlag();
			unloaded.setXmlElement(element);
			game.addFlag(unloaded, false);
		}
	}
	
	private boolean checkCopyConfig() throws IOException {
		File file = new File(getDataFolder(), CONFIG_FILE_NAME);
		if (file.exists()) {
			Configuration config = YamlConfiguration.loadConfiguration(file);
			int version = config.getInt("config-version");
			
			if (version < BungeemodeConfig.CURRENT_CONFIG_VERSION) {
				Path dataFolderPath = getDataFolder().toPath();
				Files.move(file.toPath(), dataFolderPath.resolve("config_old.yml"), StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
		} else {
			return true;
		}
		
		return false;
	}

	public BungeemodeConfig getConfig() {
		return config;
	}

	public BungeemodeListener getListener() {
		return listener;
	}
	
	public Set<SpleefPlayer> getSendBackExceptions() {
		return sendBackExceptions;
	}
	
}
