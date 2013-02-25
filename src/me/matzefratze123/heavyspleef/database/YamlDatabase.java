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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Cuboid;
import me.matzefratze123.heavyspleef.core.Floor;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.GameState;
import me.matzefratze123.heavyspleef.core.LoseZone;
import me.matzefratze123.heavyspleef.utility.FloorLoader;
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
			ConfigurationSection section = getConfigurationSection(key);
			
			Location firstCorner = Parser.convertStringtoLocation(section.getString("firstCorner"));
			Location secondCorner = Parser.convertStringtoLocation(section.getString("secondCorner"));
			
			if (firstCorner == null)
				continue;
			
			List<String> floorsAsStrings = section.getStringList("floors");
			List<Floor> floors = new ArrayList<Floor>();
			if (floorsAsStrings != null) {
				for (String floor : floorsAsStrings)
					floors.add(Parser.convertStringToFloor(floor));
			}
			
			List<String> loseZonesAsString = section.getStringList("losezones");
			List<LoseZone> loseZones = new ArrayList<LoseZone>();
			if (loseZonesAsString != null) {
				for (String loseZone : loseZonesAsString)
					loseZones.add(Parser.convertStringToLosezone(loseZone));
			}
			
			Location winPoint = Parser.convertStringtoLocation(section.getString("winPoint"));
			Location losePoint = Parser.convertStringtoLocation(section.getString("losePoint"));
			Location preGamePoint = Parser.convertStringtoLocation(section.getString("preGamePoint"));
			
			int money = section.getInt("money");
			int minPlayers = section.getInt("minPlayers");
			int countdown = section.getInt("countdown");
			if (countdown <= 0)
				countdown = plugin.getConfig().getInt("general.countdownFrom");
			boolean startOnMinPlayers = section.getBoolean("startOnMinPlayers");
			boolean useShovels = section.getBoolean("shovels");
			
			Game game = GameManager.createGame(key, firstCorner, secondCorner, false);
			for (Floor floor : floors) {
				game.addFloor(floor, false);
				if (floor.useGivenFloor)
					FloorLoader.loadFloor(floor, key);
			}
			for (LoseZone loseZone : loseZones)
				game.addLoseZone(loseZone);
			
			game.setWinPoint(winPoint);
			game.setLosePoint(losePoint);
			game.setPreGamePoint(preGamePoint);
			game.setGameState(GameState.NOT_INGAME);
			game.setMoney(money);
			game.setCountdown(countdown);
			game.setStartOnMinPlayers(startOnMinPlayers);
			game.setShovels(useShovels);
			game.setNeededPlayers(minPlayers);
			
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
						if (plugin.getConfig().getBoolean("general.savePlayerState"))
							PlayerStateManager.restorePlayerState(p);
					}
				}
			}
			game.wereOffline.addAll(wereOffline);
			count++;
			section.set("wereOfflineAtShutdown", null);
		}
		
		plugin.getLogger().info("Loaded " + count + " games!");
		try {
			db.save(databaseFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not save database to " + databaseFile.getAbsolutePath() + "! IOException?");
			e.printStackTrace();
		}
	}

	public void save(boolean savePlayerStates) {
		for (Game game : GameManager.getGames()) {
			ConfigurationSection section = db.createSection(game.getName());
			
			section.set("firstCorner", Parser.convertLocationtoString(game.getFirstCorner()));
			section.set("secondCorner", Parser.convertLocationtoString(game.getSecondCorner()));
			
			List<String> floorsAsList = new ArrayList<String>();
			List<String> loseZonesAsList = new ArrayList<String>();
			List<String> wereOffline = game.players;
			
			for (Floor f : game.getFloors()) {
				floorsAsList.add(Parser.convertFloorToString(f));
				f.create();
				
				if (f.useGivenFloor)
					FloorLoader.saveFloor(f, game);
			}
			
			for (Cuboid c : game.getLoseZones())
				loseZonesAsList.add(Parser.convertCuboidToString(c));
			
			section.set("floors", floorsAsList);
			section.set("losezones", loseZonesAsList);
			section.set("wereOfflineAtShutdown", wereOffline);
			section.set("money", game.getMoney());
			section.set("minPlayers", game.getNeededPlayers());
			section.set("countdown", game.getCountdown());
			section.set("startOnMinPlayers", game.startsOnMinPlayers());
			section.set("shovels", game.isShovels());
			
			if (game.getWinPoint() != null)
				section.set("winPoint", Parser.convertLocationtoString(game.getWinPoint()));
			
			if (game.getLosePoint() != null)
				section.set("losePoint", Parser.convertLocationtoString(game.getLosePoint()));
			
			if (game.getPreGamePoint() != null)
				section.set("preGamePoint", Parser.convertLocationtoString(game.getPreGamePoint()));
			
		}
		
		if (savePlayerStates)
			savePlayerStates();
		
		try {
			db.save(databaseFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not save database to " + databaseFile.getAbsolutePath() + "! IOException?");
			e.printStackTrace();
		}
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
				potionEffects.add(Parser.convertPotionEffectToString(pe));
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
				pe.add(Parser.convertStringToPotionEffect(peString));
			
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
	
}
