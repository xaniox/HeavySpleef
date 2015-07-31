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
package de.matzefratze123.leaderboardextensions;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dom4j.DocumentException;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.extension.ExtensionManager;
import de.matzefratze123.heavyspleef.core.extension.GameExtension;

public class LeaderboardAddOn extends BasicAddOn {

	public static final String I18N_REFERENCE = "LeaderboardExtensions";
	
	private ExtensionManager globalExtensionManager;
	private ExtensionXmlHandler xmlHandler;
	
	private SignLayoutConfiguration podiumConfig;
	private SignLayoutConfiguration wallConfig;
	
	private File podiumFile;
	private File wallFile;
	
	@Override
	public void enable() {
		File dataFolder = getDataFolder();
		dataFolder.mkdir();
		
		File xmlFile = new File(dataFolder, "extensions.xml");
		xmlHandler = new ExtensionXmlHandler(xmlFile, getHeavySpleef().getExtensionRegistry());
		globalExtensionManager = new GlobalExtensionManager(getHeavySpleef());
		
		podiumFile = new File(getDataFolder(), "layout-podium.yml");
		wallFile = new File(getDataFolder(), "layout-wall.yml");
		copyLayouts();
		
		Configuration podiumYml = YamlConfiguration.loadConfiguration(podiumFile);
		podiumConfig = new SignLayoutConfiguration(podiumYml);
		
		Configuration wallYml = YamlConfiguration.loadConfiguration(wallFile);
		wallConfig = new SignLayoutConfiguration(wallYml);
		
		Set<GameExtension> set = Sets.newHashSet();
		
		try {
			xmlHandler.loadExtensions(set);
		} catch (IOException | DocumentException e) {
			getLogger().log(Level.SEVERE, "Failed to load extensions from xml", e);
		}
		
		for (GameExtension extension : set) {
			globalExtensionManager.addExtension(extension);
			
			if (extension instanceof ExtensionLeaderboardPodium) {
				ExtensionLeaderboardPodium podium = (ExtensionLeaderboardPodium) extension;
				podium.setLayoutConfig(podiumConfig);
				podium.update(false);
			} else if (extension instanceof ExtensionLeaderboardWall) {
				ExtensionLeaderboardWall wall = (ExtensionLeaderboardWall) extension;
				wall.setLayoutConfig(wallConfig);
				wall.update();
			}
		}
	}
	
	private void copyLayouts() {
		if (!podiumFile.exists()) {
			try {
				podiumFile.createNewFile();
				copyResource("layout-podium.yml", podiumFile);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not copy layout configuration for podiums", e);
			}
		}
		
		if (!wallFile.exists()) {
			try {
				wallFile.createNewFile();
				copyResource("layout-wall.yml", wallFile);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not copy layout configuration for podiums", e);
			}
		}
	}
	
	@Override
	public void disable() {
		saveExtensions();
	}
	
	public ExtensionManager getGlobalExtensionManager() {
		return globalExtensionManager;
	}
	
	public SignLayoutConfiguration getPodiumConfig() {
		return podiumConfig;
	}
	
	public SignLayoutConfiguration getWallConfig() {
		return wallConfig;
	}

	public void saveExtensions() {
		Set<GameExtension> extensions = globalExtensionManager.getExtensions();
		
		try {
			xmlHandler.saveExtensions(extensions);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to save extensions to xml", e);
		}
	}
	
}
