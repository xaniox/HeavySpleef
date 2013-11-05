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
package de.matzefratze123.heavyspleef.stats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Logger;


public class YamlStatisticDatabase implements IStatisticDatabase {

	private HeavySpleef plugin;
	private File databaseFile;
	private FileConfiguration db;
	
	public YamlStatisticDatabase() {
		this.plugin = HeavySpleef.getInstance();
		new File(plugin.getDataFolder().getPath() + "/statistic").mkdirs();
		
		File dbFile = new File(plugin.getDataFolder().getPath() + "/statistic/statistics.yml");
		if (!dbFile.exists()) {
			try {
				dbFile.createNewFile();
			} catch (IOException e) {}
		}
		this.databaseFile = dbFile;
		this.db = YamlConfiguration.loadConfiguration(databaseFile);
	}
	
	@Override
	public void saveAccounts() {
		synchronized (db) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				StatisticModule stat = HeavySpleef.getInstance().getSpleefPlayer(player).getStatistic();
				
				ConfigurationSection section = null;
				
				if (!db.contains(stat.getName()))
					section = db.createSection(stat.getName());
				else
					section = db.getConfigurationSection(stat.getName());
				
				section.set("wins", stat.getWins());
				section.set("loses", stat.getLoses());
				section.set("knockouts", stat.getKnockouts());
				section.set("games", stat.getGamesPlayed());
				section.set("score", stat.getScore());
			}
		}
		
		try {
			db.save(databaseFile);
		} catch (Exception e) {
			Logger.warning("Could not save stats to " + databaseFile.getName() + "! IOException?");
		}
	}

	@Override
	public StatisticModule loadAccount(String holder) {
		StatisticModule module;
		
		synchronized (db) {
			ConfigurationSection section = db.getConfigurationSection(holder);
			
			if (section == null) {
				return null;
			}
			
			int wins = section.getInt("wins");
			int loses = section.getInt("loses");
			int knockouts = section.getInt("knockouts");
			int games = section.getInt("games");
			
			module = new StatisticModule(holder, loses, wins, knockouts, games);
		}
		
		return module;
	}

	@Override
	public void unloadAccount(SpleefPlayer player) {
		ConfigurationSection section = null;
		StatisticModule module = player.getStatistic();
		
		synchronized (db) {
			if (!db.contains(module.getName())) {
				section = db.createSection(module.getName());
			} else {
				section = db.getConfigurationSection(module.getName());
			}
			
			section.set("wins", module.getWins());
			section.set("loses", module.getLoses());
			section.set("knockouts", module.getKnockouts());
			section.set("games", module.getGamesPlayed());
			section.set("score", module.getScore());
		}
	}

	@Override
	public List<StatisticModule> loadAccounts() {
		List<StatisticModule> list = new ArrayList<StatisticModule>();
	
		synchronized (db) {
			for (String owner : db.getKeys(false)) {
				ConfigurationSection section = db.getConfigurationSection(owner);
				
				int wins = section.getInt("wins");
				int loses = section.getInt("loses");
				int knockouts = section.getInt("knockouts");
				int games = section.getInt("games");
				
				StatisticModule stat = new StatisticModule(owner, loses, wins, knockouts, games);
				list.add(stat);
			}
		}
		
		return list;
		
	}

}
