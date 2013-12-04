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
package de.matzefratze123.heavyspleef.database;

import static de.matzefratze123.heavyspleef.database.Parser.convertLocationtoString;
import static de.matzefratze123.heavyspleef.database.Parser.convertStringtoLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.StopCause;
import de.matzefratze123.heavyspleef.core.region.HUBPortal;
import de.matzefratze123.heavyspleef.util.Logger;

/**
 * Provides a database manager for the plugin
 * 
 * @author matzefratze123
 */
public class YamlDatabase {

	private HeavySpleef plugin;
	
	private File databaseFile;
	private File globalDatabaseFile;
	
	public FileConfiguration db;
	public FileConfiguration globalDb;
	
	/**
	 * Constructs a new YamlDatabase
	 */
	public YamlDatabase() {
		this.plugin = HeavySpleef.getInstance();
		
		File folder = new File(plugin.getDataFolder(), File.separator + "games");
		folder.mkdirs();
		
		this.databaseFile = new File(folder, "games.yml");
		this.globalDatabaseFile = new File(folder, "global-settings.yml");
		
		if (!databaseFile.exists()) {
			createDefaultDatabaseFile(databaseFile);
		}
		
		if (!globalDatabaseFile.exists()) {
			createDefaultDatabaseFile(globalDatabaseFile);
		}
		
		this.db = YamlConfiguration.loadConfiguration(databaseFile);
		this.globalDb = YamlConfiguration.loadConfiguration(globalDatabaseFile);
	}

	private void createDefaultDatabaseFile(File file) {
		try {
			file.createNewFile();
			InputStream inStream = HeavySpleef.class.getResourceAsStream("/default/defaultdatabase.yml");
			FileOutputStream outStream = new FileOutputStream(databaseFile);
			
			byte[] buffer = new byte[1024];
			int read;
			
			while((read = inStream.read(buffer)) > 0)
				outStream.write(buffer, 0, read);
			
			inStream.close();
			outStream.close();
		} catch (IOException e) {
			Logger.severe("Could not create spleef database! IOException?");
			e.printStackTrace();
		}
	}

	/**
	 * Loads all games from the database into the system
	 */
	public void load() {
		//Make sure to clear all other games to provide compatibility with PluginLoaders
		GameManager.getGames().clear();
		
		int count = 0;
		
		for (String key : db.getKeys(false)) {
			ConfigurationSection section = db.getConfigurationSection(key);
			
			Game game = Game.deserialize(section);
			GameManager.addGame(game);
			
			count++;
		}
		
		
		Logger.info("Loaded " + count + " games!");
		loadGlobalSettings();
		saveConfig();
	}

	/**
	 * Saves all games from the system to the database
	 */
	public void save() {
		for (Game game : GameManager.getGames()) {
			if (game.getGameState() == GameState.INGAME || game.getGameState() == GameState.COUNTING || game.getGameState() == GameState.LOBBY) {
				game.stop(StopCause.STOP);
			}
			
			ConfigurationSection serialized = game.serialize();
			db.createSection(game.getName(), serialized.getValues(true));
		}
		
		saveGlobalSettings();
		saveConfig();
	}
	
	private void saveGlobalSettings() {
		if (globalDb == null)
			return;
		//Save the spleef hub
		Location spleefHub = GameManager.getSpleefHub();
		if (spleefHub != null)
			globalDb.set("hub", convertLocationtoString(spleefHub));
		
		//Create a section for the portals if there isn't one
		ConfigurationSection portalsSection = globalDb.contains("portals") ? globalDb.getConfigurationSection("portals") : globalDb.createSection("portals");
		
		//Save every portal
		for (HUBPortal portal : GameManager.getPortals()) {
			ConfigurationSection section = portalsSection.createSection(String.valueOf(portal.getId()));
			
			section.set("firstCorner", convertLocationtoString(portal.getFirstPoint()));
			section.set("secondCorner", convertLocationtoString(portal.getSecondPoint()));
		}
	}
	
	private void loadGlobalSettings() {
		if (globalDb == null)
			return;
		
		if (globalDb.contains("hub"))
			GameManager.setSpleefHub(convertStringtoLocation(globalDb.getString("hub")));
		
		ConfigurationSection portalsSection = globalDb.getConfigurationSection("portals");
		if (portalsSection != null) {
			for (String key : portalsSection.getKeys(false)) {
				ConfigurationSection keySection = portalsSection.getConfigurationSection(key);
				
				Location firstCorner = null;
				Location secondCorner = null;
				int id = -1;
				
				if (keySection.contains("firstCorner"))
					firstCorner = convertStringtoLocation(keySection.getString("firstCorner"));
				if (keySection.contains("secondCorner"))
					secondCorner = convertStringtoLocation(keySection.getString("secondCorner"));
				
				try {
					id = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					Logger.warning("Failed to load portal id for portal " + id + "! Ignoring portal...");
					continue;
				}
				
				HUBPortal portal = new HUBPortal(id, firstCorner, secondCorner);
				GameManager.addPortal(portal);
			}
		}
	}

	/**
	 * Gets a configuration section from the database file
	 * 
	 * @param name The name of the configuration-section
	 */
	public ConfigurationSection getConfigurationSection(String name) {
		return db.getConfigurationSection(name);
	}
	
	/**
	 * Pushes all datas into the physical file
	 */
	public void saveConfig() {
		try {
			db.save(databaseFile);
			globalDb.save(globalDatabaseFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not save database to " + databaseFile.getAbsolutePath() + "! IOException?");
			e.printStackTrace();
		}
	}
	
}
