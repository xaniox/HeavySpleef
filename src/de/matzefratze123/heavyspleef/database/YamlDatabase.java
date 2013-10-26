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
package de.matzefratze123.heavyspleef.database;

import static de.matzefratze123.heavyspleef.core.flag.FlagType.*;
import static de.matzefratze123.heavyspleef.database.Parser.convertLocationtoString;
import static de.matzefratze123.heavyspleef.database.Parser.convertLoseZoneToString;
import static de.matzefratze123.heavyspleef.database.Parser.convertStringToLosezone;
import static de.matzefratze123.heavyspleef.database.Parser.convertStringtoLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameCuboid;
import de.matzefratze123.heavyspleef.core.GameCylinder;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.ScoreBoard;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.core.region.Floor;
import de.matzefratze123.heavyspleef.core.region.FloorCuboid;
import de.matzefratze123.heavyspleef.core.region.FloorCylinder;
import de.matzefratze123.heavyspleef.core.region.HUBPortal;
import de.matzefratze123.heavyspleef.core.region.LoseZone;

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
		File statsFolder = new File(plugin.getDataFolder(), File.separator + "stats");
		
		folder.mkdirs();
		statsFolder.mkdirs();
		
		this.databaseFile = new File(folder, "games.yml");
		this.globalDatabaseFile = new File(folder, "global-settings.yml");
		
		if (!databaseFile.exists())
			createDefaultDatabaseFile(databaseFile);
		if (!globalDatabaseFile.exists())
			createDefaultDatabaseFile(globalDatabaseFile);
		
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
			Bukkit.getLogger().severe("Could not create spleef database! IOException?");
			e.printStackTrace();
		}
	}

	/**
	 * Loads all games from the database into the system
	 */
	public void load() {
		int count = 0;
		
		for (String key : db.getKeys(false)) {
			ConfigurationSection section = db.getConfigurationSection(key);
			
			if (section.getString("type") == null || GameType.valueOf(section.getString("type")) == GameType.CUBOID)
				loadCuboid(section);
			else if (GameType.valueOf(section.getString("type")) == GameType.CYLINDER)
				loadCylinder(section);
			count++;
			
			HeavySpleef.debug("Loaded " + key + "!");
		}
		
		plugin.getLogger().info("Loaded " + count + " games!");
		loadGlobalSettings();
		saveConfig();
	}

	/**
	 * Saves all games from the system to the database
	 */
	public void save() {
		for (Game game : GameManager.getGames()) {
			ConfigurationSection section = db.createSection(game.getName());
			
			saveBasics(game, section);
			if (game.getType() == GameType.CUBOID)
				saveCuboid((GameCuboid) game, section);
			else if (game.getType() == GameType.CYLINDER)
				saveCylinder((GameCylinder) game, section);
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
			
			section.set("firstCorner", convertLocationtoString(portal.getFirstCorner()));
			section.set("secondCorner", convertLocationtoString(portal.getSecondCorner()));
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
					HeavySpleef.getInstance().getLogger().warning("Failed to load portal id for portal " + id + "! Ignoring portal...");
					continue;
				}
				
				HUBPortal portal = new HUBPortal(id, firstCorner, secondCorner);
				GameManager.addPortal(portal);
			}
		}
	}

	private void saveCuboid(GameCuboid game, ConfigurationSection section) {
		if (game.getType() != GameType.CUBOID)
			return;
		section.set("firstCorner", convertLocationtoString(game.getFirstCorner()));
		section.set("secondCorner", convertLocationtoString(game.getSecondCorner()));
		
		List<String> floorsAsList = new ArrayList<String>();
		List<FloorCuboid> floors = new ArrayList<FloorCuboid>();
		
		for (Floor f : game.getFloors())
			floors.add((FloorCuboid)f);
		
		for (FloorCuboid f : floors) {
			floorsAsList.add(f.toString());
			f.create();
			
			if (f.isGivenFloor())
				FloorLoader.saveFloor(f, game);
		}
		
		section.set("floors", floorsAsList);
	}
	
	
	private void saveCylinder(GameCylinder game, ConfigurationSection section) {
		if (game.getType() != GameType.CYLINDER)
			return;
		section.set("center", convertLocationtoString(game.getCenter()));
		section.set("radiusEastWest", game.getRadiusEastWest());
		section.set("radiusNorthSouth", game.getRadiusNorthSouth());
		section.set("minY", game.getMinY());
		section.set("maxY", game.getMaxY());
		
		List<String> floorsAsList = new ArrayList<String>();
		List<FloorCylinder> floors = new ArrayList<FloorCylinder>();
		
		for (Floor f : game.getFloors())
			floors.add((FloorCylinder)f);
		
		for (FloorCylinder c : floors) {
			floorsAsList.add(c.toString());
			c.create();
			
			if (c.isGivenFloor())
				FloorLoader.saveFloor(c, game);
		}
		
		section.set("floors", floorsAsList);
	}
	
	private void loadCuboid(ConfigurationSection section) {
		String name = section.getName();
		
		Location firstCorner = convertStringtoLocation(section.getString("firstCorner"));
		Location secondCorner = convertStringtoLocation(section.getString("secondCorner"));
		
		if (firstCorner == null)
			return;
		if (secondCorner == null)
			return;
		
		Game game = GameManager.createCuboidGame(name, firstCorner, secondCorner);
		
		List<String> floorsAsStrings = section.getStringList("floors");
		if (floorsAsStrings != null) {
			for (String floor : floorsAsStrings)
				game.addFloor(FloorCuboid.fromString(floor, game.getName()), true);
		}
		
		loadBasics(section, game);
	}
	
	private void loadCylinder(ConfigurationSection section) {
		String name = section.getName();
		
		Location center = convertStringtoLocation(section.getString("center"));
		
		int radiusEastWest = 0;
		int radiusNorthSouth = 0;
		
		if (section.getString("radius") != null) {
			radiusEastWest = section.getInt("radius");
			radiusNorthSouth = section.getInt("radius");
		} else {
			radiusEastWest = section.getInt("radiusEastWest");
			radiusNorthSouth = section.getInt("radiusNorthSouth");
		}
		
		int minY = section.getInt("minY");
		int maxY = section.getInt("maxY");
		
		Game game = GameManager.createCylinderGame(name, center, radiusEastWest, radiusNorthSouth, minY, maxY);
		if (game == null)//Just in case if no WorldEdit is installed
			return;
		
		List<String> floorsAsStrings = section.getStringList("floors");
		if (floorsAsStrings != null) {
			for (String floor : floorsAsStrings)
				game.addFloor(FloorCylinder.fromString(floor, game.getName()), true);
		}
		
		loadBasics(section, game);
	}
	
	private void loadBasics(ConfigurationSection section, Game game) {
		
		List<String> loseZonesAsString = section.getStringList("losezones");
		if (loseZonesAsString != null) {
			for (String loseZone : loseZonesAsString)
				game.addLoseZone(convertStringToLosezone(loseZone));
		}
		
		List<String> wallsAsList = section.getStringList("walls");
		if (wallsAsList != null) {
			for (String wall : wallsAsList)
				game.addWall(SignWall.fromString(wall, game));
		}
		
		//Old flag system
		if (section.getString("winPoint") != null)
			game.setFlag(WIN, convertStringtoLocation(section.getString("winPoint")));
		if (section.getString("losePoint") != null)
			game.setFlag(LOSE, convertStringtoLocation(section.getString("losePoint")));
		if (section.getString("preGamePoint") != null)
			game.setFlag(LOBBY, convertStringtoLocation(section.getString("preGamePoint")));
		if (section.getString("spawnPoint1") != null)
			game.setFlag(SPAWNPOINT1, convertStringtoLocation(section.getString("spawnPoint1")));
		if (section.getString("spawnPoint2") != null)
			game.setFlag(SPAWNPOINT2, convertStringtoLocation(section.getString("spawnPoint2")));
		if (section.getString("queuesPoint") != null)
			game.setFlag(QUEUELOBBY, convertStringtoLocation(section.getString("queuesPoint")));
		
		if (section.contains("reward"))
			game.setFlag(REWARD, section.getInt("reward"));
		if (section.contains("minPlayers"))
			game.setFlag(MINPLAYERS, section.getInt("minPlayers"));
		if (section.contains("maxPlayers"))
			game.setFlag(MAXPLAYERS, section.getInt("maxPlayers"));
		if (section.contains("chances"))
			game.setFlag(CHANCES, section.getInt("chances"));
		if (section.contains("autostart"))
			game.setFlag(AUTOSTART, section.getInt("autostart"));
		if (section.contains("countdown"))
			game.setFlag(COUNTDOWN, section.getInt("countdown"));
		if (section.contains("rounds"))
			game.setFlag(ROUNDS, section.getInt("rounds"));
		if (section.contains("timeout"))
			game.setFlag(MINPLAYERS, section.getInt("timeout"));
		if (section.contains("shovels"))
			game.setFlag(SHOVELS, section.getBoolean("shovels"));
		if (section.contains("1vs1"))
			game.setFlag(ONEVSONE, section.getBoolean("1vs1"));
		if (section.contains("timeout"))
			game.setFlag(TIMEOUT, section.getInt("timeout"));
		//Old flag system end
		
		loadFlags(game, section);
		
		if (section.contains("scoreboards")) {
			for (String board : section.getStringList("scoreboards")) {
				ScoreBoard scoreBoard = new ScoreBoard(board, game);
				game.addScoreBoard(scoreBoard);
			}
		}
		
		List<String> databaseTeams = section.getStringList("teams");
		
		for (String str : databaseTeams) {
			String parts[] = str.split(";");
			if (parts.length < 3)
				continue;
			
			ChatColor color = ChatColor.valueOf(parts[0]);
			int minplayers = Integer.parseInt(parts[1]);
			int maxplayers = Integer.parseInt(parts[2]);
			
			Team team = new Team(color, game);
			team.setMinPlayers(minplayers);
			team.setMaxPlayers(maxplayers);
			
			game.addTeam(team);
		}
	}
	
	private void saveBasics(Game game, ConfigurationSection section) {
		if (game.hasActivity())
			game.stop();
		section.set("type", game.getType().name());
		
		List<String> loseZonesAsList = new ArrayList<String>();
		
		for (LoseZone c : game.getLoseZones())
			loseZonesAsList.add(convertLoseZoneToString(c));
		
		List<String> wallsAsList = new ArrayList<String>();
		for (SignWall wall : game.getWalls())
			wallsAsList.add(wall.toString());
		
		section.set("walls", wallsAsList);
		section.set("losezones", loseZonesAsList);
		
		saveFlags(game, section);
		
		List<String> scoreBoardsAsList = new ArrayList<String>();
		for (ScoreBoard board : game.getScoreBoards()) {
			scoreBoardsAsList.add(board.toString());
		}
		
		section.set("scoreboards", scoreBoardsAsList);
		List<Team> teams = game.getTeams();
		List<String> databaseTeams = new ArrayList<String>();
		
		for (Team team : teams) {
			databaseTeams.add(team.getColor().name() + ";" + team.getMinPlayers() + ";" + team.getMaxPlayers());
		}
		
		section.set("teams", databaseTeams);
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
	
	private void saveFlags(Game game, ConfigurationSection section) {
		Map<Flag<?>, Object> flags = game.getFlags();
		List<String> flagsAsString = new ArrayList<String>();
		
		for (Flag<?> flag : flags.keySet()) {
			String serialized = flag.serialize(flags.get(flag));
			flagsAsString.add(serialized);
		}
		
		section.set("flags", flagsAsString);
	}
	
	private void loadFlags(Game game, ConfigurationSection section) {
		List<String> flagsAsString = section.getStringList("flags");
		
		if (flagsAsString == null)
			return;
		
		Map<Flag<?>, Object> flags = new HashMap<Flag<?>, Object>();
		
		for (String str : flagsAsString) {
			Flag<?> flag = FlagType.byDatabaseName(str);
			if (flag == null)
				continue;
			
			Object deserialized = flag.deserialize(str);
			flags.put(flag, deserialized);
		}
		
		game.setFlags(flags);
	}
	
}
