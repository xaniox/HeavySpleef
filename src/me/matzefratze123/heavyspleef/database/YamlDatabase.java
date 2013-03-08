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
package me.matzefratze123.heavyspleef.database;

import static me.matzefratze123.heavyspleef.database.Parser.convertLocationtoString;
import static me.matzefratze123.heavyspleef.database.Parser.convertLoseZoneToString;
import static me.matzefratze123.heavyspleef.database.Parser.convertPotionEffectToString;
import static me.matzefratze123.heavyspleef.database.Parser.convertStringToLosezone;
import static me.matzefratze123.heavyspleef.database.Parser.convertStringToPotionEffect;
import static me.matzefratze123.heavyspleef.database.Parser.convertStringtoLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameCuboid;
import me.matzefratze123.heavyspleef.core.GameCylinder;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.GameState;
import me.matzefratze123.heavyspleef.core.Type;
import me.matzefratze123.heavyspleef.core.region.Floor;
import me.matzefratze123.heavyspleef.core.region.FloorCuboid;
import me.matzefratze123.heavyspleef.core.region.FloorCylinder;
import me.matzefratze123.heavyspleef.core.region.LoseZone;
import me.matzefratze123.heavyspleef.utility.PlayerState;
import me.matzefratze123.heavyspleef.utility.PlayerStateManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class YamlDatabase {

	private HeavySpleef plugin;
	private File databaseFile;
	private File statsDatabaseFile;
	
	private FileConfiguration db;
	private FileConfiguration statsdb;
	
	public YamlDatabase() {
		this.plugin = HeavySpleef.instance;
		File folder = new File(plugin.getDataFolder(), File.separator + "games");
		File statsFolder = new File(plugin.getDataFolder(), File.separator + "stats");
		folder.mkdirs();
		statsFolder.mkdirs();
		this.databaseFile = new File(folder, "games.yml");
		this.statsDatabaseFile = new File(statsFolder, "stats.yml");
		if (!databaseFile.exists())
			createDefaultDatabaseFile(databaseFile);
		if (!statsDatabaseFile.exists())
			createDefaultDatabaseFile(statsDatabaseFile);
		this.db = YamlConfiguration.loadConfiguration(databaseFile);
		this.statsdb = YamlConfiguration.loadConfiguration(statsDatabaseFile);
	}

	private void createDefaultDatabaseFile(File file) {
		try {
			file.createNewFile();
			InputStream inStream = HeavySpleef.class.getResourceAsStream("/defaultdatabase.yml");
			FileOutputStream outStream = new FileOutputStream(databaseFile);
			
			byte[] buffer = new byte[1024];
			int read;
			
			while((read = inStream.read(buffer)) > 0)
				outStream.write(buffer, 0, read);
			
			inStream.close();
			outStream.close();
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not create default spleef database! IOException?");
			e.printStackTrace();
		}
	}

	public void load() {
		loadPlayerStats();
		int count = 0;
		
		for (String key : db.getKeys(false)) {
			ConfigurationSection section = db.getConfigurationSection(key);
			
			if (section.getString("type") == null || Type.valueOf(section.getString("type")) == Type.CUBOID)
				loadCuboid(section);
			else if (Type.valueOf(section.getString("type")) == Type.CYLINDER)
				loadCylinder(section);
			count++;
		}
		
		plugin.getLogger().info("Loaded " + count + " games!");
		saveConfig();
	}

	public void save(boolean savePlayerStates) {
		for (Game game : GameManager.getGames()) {
			ConfigurationSection section = db.createSection(game.getName());
			
			saveBasics(game, section);
			if (game.getType() == Type.CUBOID)
				saveCuboid((GameCuboid) game, section);
			else if (game.getType() == Type.CYLINDER)
				saveCylinder((GameCylinder) game, section);
		}
		
		if (savePlayerStates)
			savePlayerStates();
		saveConfig();
	}
	
	private void saveCuboid(GameCuboid game, ConfigurationSection section) {
		if (game.getType() != Type.CUBOID)
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
	}
	
	
	private void saveCylinder(GameCylinder game, ConfigurationSection section) {
		if (game.getType() != Type.CYLINDER)
			return;
		section.set("center", convertLocationtoString(game.getCenter()));
		section.set("radius", game.getRadius());
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
		int radius = section.getInt("radius");
		int minY = section.getInt("minY");
		int maxY = section.getInt("maxY");
		
		Game game = GameManager.createCylinderGame(name, center, radius, minY, maxY);
		
		List<String> floorsAsStrings = section.getStringList("floors");
		if (floorsAsStrings != null) {
			for (String floor : floorsAsStrings)
				game.addFloor(FloorCylinder.fromString(floor, game.getName()), true);
		}
		
		loadBasics(section, game);
	}
	
	private void loadBasics(ConfigurationSection section, Game game) {
		
		List<String> loseZonesAsString = section.getStringList("losezones");
		List<LoseZone> loseZones = new ArrayList<LoseZone>();
		if (loseZonesAsString != null) {
			for (String loseZone : loseZonesAsString)
				loseZones.add(convertStringToLosezone(loseZone));
		}
		
		Location winPoint = convertStringtoLocation(section.getString("winPoint"));
		Location losePoint = convertStringtoLocation(section.getString("losePoint"));
		Location preGamePoint = convertStringtoLocation(section.getString("preGamePoint"));
		
		int money = section.getInt("money");
		int reward = section.getInt("reward");
		int minPlayers = section.getInt("minPlayers");
		int chances = section.getInt("chances");
		int countdown = section.getInt("countdown");
		if (countdown <= 0)
			countdown = plugin.getConfig().getInt("general.countdownFrom");
		
		boolean startOnMinPlayers = section.getBoolean("startOnMinPlayers");
		boolean useShovels = section.getBoolean("shovels");
		
		for (LoseZone loseZone : loseZones)
			game.addLoseZone(loseZone);
		
		game.setWinPoint(winPoint);
		game.setLosePoint(losePoint);
		game.setPreGamePoint(preGamePoint);
		game.setGameState(GameState.NOT_INGAME);
		game.setJackpotToPay(money);
		game.setReward(reward);
		game.setCountdown(countdown);
		game.setStartOnMinPlayers(startOnMinPlayers);
		game.setShovels(useShovels);
		game.setNeededPlayers(minPlayers);
		game.setChances(chances);
		
		List<String> wereOfflineConfigList = section.getStringList("wereOfflineAtShutdown");
		List<String> wereOffline = new ArrayList<String>();
		if (wereOfflineConfigList != null) {
			for (String offlinePlayer : wereOfflineConfigList) {
				Player p = Bukkit.getPlayer(offlinePlayer);
				if (p == null)
					wereOffline.add(offlinePlayer);
				else {
					p.teleport(game.getLosePoint());
					p.sendMessage(ChatColor.RED + "A reload of the server has stopped the game and you were teleported out of it!");
				}
			}
		}
		game.wereOffline.addAll(wereOffline);
		section.set("wereOfflineAtShutdown", null);
	}
	
	private void saveBasics(Game game, ConfigurationSection section) {
		section.set("type", game.getType().name());
		
		List<String> loseZonesAsList = new ArrayList<String>();
		List<String> wereOffline = game.players;
		
		for (LoseZone c : game.getLoseZones())
			loseZonesAsList.add(convertLoseZoneToString(c));
		
		section.set("losezones", loseZonesAsList);
		section.set("wereOfflineAtShutdown", wereOffline);
		section.set("money", game.getJackpotToPay());
		section.set("reward", game.getReward());
		section.set("minPlayers", game.getNeededPlayers());
		section.set("countdown", game.getCountdown());
		section.set("startOnMinPlayers", game.startsOnMinPlayers());
		section.set("shovels", game.isShovels());
		section.set("chances", game.getChances());
		
		if (game.getWinPoint() != null)
			section.set("winPoint", convertLocationtoString(game.getWinPoint()));
		
		if (game.getLosePoint() != null)
			section.set("losePoint", convertLocationtoString(game.getLosePoint()));
		
		if (game.getPreGamePoint() != null)
			section.set("preGamePoint", convertLocationtoString(game.getPreGamePoint()));
	}

	private void savePlayerStates() {
		Map<String, PlayerState> playerStates = PlayerStateManager.getPlayerStates();
		for (String player : playerStates.keySet()) {
			ConfigurationSection section = statsdb.createSection(player);
			PlayerState state = playerStates.get(player);
			
			ItemStack[] invContents = state.getContents();
			
			for (int i = 0; i < 36; i++) {
				ItemStack currentStack = invContents[i];
				if (currentStack == null)
					section.set("stack_" + i, "null");
				else
					section.set("stack_" + i, currentStack);
			}
			
			List<String> potionEffects = new ArrayList<String>();
			
			section.set("helmet", state.getHelmet());
			section.set("chestplate", state.getChestplate());
			section.set("leggings", state.getLeggings());
			section.set("boots", state.getBoots());
			
			section.set("health", state.getHealth());
			section.set("food", state.getFoodLevel());
			
			section.set("level", state.getLevel());
			section.set("exp", state.getExp());
			
			section.set("exhaustion", state.getExhaustion());
			section.set("saturation", state.getSaturation());
			
			section.set("gamemode", state.getGm().getValue());
			
			for (PotionEffect pe : state.getPotioneffects())
				potionEffects.add(convertPotionEffectToString(pe));
			section.set("potioneffects", potionEffects);
			
		}
		
		try {
			statsdb.save(statsDatabaseFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not save database to " + databaseFile.getAbsolutePath() + "! IOException?");
			e.printStackTrace();
		}
	}
	
	private void loadPlayerStats() {
		
		for (String key : statsdb.getKeys(false)) {
			ConfigurationSection section = statsdb.getConfigurationSection(key);
			
			ItemStack[] invContents = new ItemStack[36];
			ItemStack helmet = section.getItemStack("helmet");
			ItemStack chestplate = section.getItemStack("chestplate");
			ItemStack leggings = section.getItemStack("leggings");
			ItemStack boots = section.getItemStack("boots");
			
			for (int i = 0; i < 36; i++)
				if (section.getString("stack_" + i).equalsIgnoreCase("null"))
					invContents[i] = null;
				else
					invContents[i] = section.getItemStack("stack_" + i);
			
			int health = section.getInt("health");
			int foodLevel = section.getInt("food");
			
			int level = section.getInt("level");
			float exp = (float) section.getDouble("exp");
			
			float exhaustion = (float) section.getDouble("exhaustion");
			float saturation = (float) section.getDouble("saturation");
			
			GameMode gm = GameMode.getByValue(section.getInt("gamemode"));
			
			Collection<PotionEffect> pe = new ArrayList<PotionEffect>();
			for (String peString : section.getStringList("potioneffects"))
				pe.add(convertStringToPotionEffect(peString));
			
			PlayerStateManager.states.put(key, new PlayerState(invContents, helmet, chestplate, leggings, boots, saturation, exhaustion, foodLevel, health, gm, pe, exp, level));
			statsdb.set(key, null);
		}
		
		try {
			statsdb.save(statsDatabaseFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not save database to " + databaseFile.getAbsolutePath() + "! IOException?");
			e.printStackTrace();
		}
	}

	public ConfigurationSection getConfigurationSection(String name) {
		return db.getConfigurationSection(name);
	}
	
	public void saveConfig() {
		try {
			db.save(databaseFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not save database to " + databaseFile.getAbsolutePath() + "! IOException?");
			e.printStackTrace();
		}
	}
	
}
