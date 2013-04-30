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
package me.matzefratze123.heavyspleef.stats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.matzefratze123.heavyspleef.HeavySpleef;

public class YamlStatisticDatabase implements IStatisticDatabase {

	private HeavySpleef plugin;
	private File databaseFile;
	private FileConfiguration db;
	
	public YamlStatisticDatabase() {
		this.plugin = HeavySpleef.instance;
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
	public void save() {
		List<StatisticModule> statistics = new ArrayList<StatisticModule>(StatisticManager.getStatistics());
		Collections.sort(statistics);
		
		for (StatisticModule stat : statistics) {
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
		
		try {
			db.save(databaseFile);
		} catch (Exception e) {
			plugin.getLogger().warning("Could not save stats to " + databaseFile.getName() + "! IOException?");
		}
	}

	@Override
	public void load() {
		int count = 0;
		
		for (String owner : db.getKeys(false)) {
			ConfigurationSection section = db.getConfigurationSection(owner);
			
			int wins = section.getInt("wins");
			int loses = section.getInt("loses");
			int knockouts = section.getInt("knockouts");
			int games = section.getInt("games");
			
			StatisticModule stat = new StatisticModule(owner, loses, wins, knockouts, games);
			StatisticManager.addExistingStatistic(stat);
			count++;
		}
		
		HeavySpleef.instance.getLogger().info("Loaded " + count + " statistics!");
	}

}
