package me.matzefratze123.heavyspleef.database.statistic;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.statistic.Statistic;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticManager;

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
		for (Statistic stat : StatisticManager.getStatistics()) {
			ConfigurationSection section = null;
			
			if (!db.contains(stat.getName()))
				section = db.createSection(stat.getName());
			else
				section = db.getConfigurationSection(stat.getName());
			
			section.set("wins", stat.getWins());
			section.set("loses", stat.getLoses());
			section.set("knockouts", stat.getKnockouts());
			section.set("games", stat.getGamesPlayed());
		}
		
		try {
			db.save(databaseFile);
		} catch (Exception e) {
			plugin.getLogger().warning("Could not save stats to " + databaseFile.getName() + "! IOException?");
		}
	}

	@Override
	public void load() {
		for (String owner : db.getKeys(false)) {
			ConfigurationSection section = db.getConfigurationSection(owner);
			
			int wins = section.getInt("wins");
			int loses = section.getInt("loses");
			int knockouts = section.getInt("knockouts");
			int games = section.getInt("games");
			
			Statistic stat = new Statistic(owner, loses, wins, knockouts, games);
			StatisticManager.addExistingStatistic(stat);
		}
	}

}
