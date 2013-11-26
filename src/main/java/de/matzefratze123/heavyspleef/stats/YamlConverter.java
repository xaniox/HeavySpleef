package de.matzefratze123.heavyspleef.stats;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.stats.sql.AbstractDatabase;
import de.matzefratze123.heavyspleef.stats.sql.SQLiteDatabase;
import de.matzefratze123.heavyspleef.stats.sql.Table;
import de.matzefratze123.heavyspleef.util.Logger;

public class YamlConverter {

	/**
	 * Converts old yaml data stored statistics into sqlite data
	 */
	public static void convertYamlData() {
		File yamlFile = new File(HeavySpleef.getInstance().getDataFolder(), "statistic/statistics.yml");
		if (!yamlFile.exists()) {
			return;
		}
		
		try {
			FileConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
			
			int i = 0;
			
			for (String key : yaml.getKeys(false)) {
				ConfigurationSection section = yaml.getConfigurationSection(key);
				
				int wins, loses, knockouts, games;
				
				wins = section.getInt("wins");
				loses = section.getInt("loses");
				knockouts = section.getInt("knockouts");
				games = section.getInt("games");
				
				StatisticModule module = new StatisticModule(key, loses, wins, knockouts, games);
				
				writeToSQLite(module);
				i++;
			}
			
			//Converted successfully!
			Logger.info("Converted " + i + " statistics into sqlite successfully!");
			//Finally delete file
			yamlFile.delete();
		} catch (Exception e) {
			//Catch any exceptions as we may got corrupted files
			Logger.warning("Warning! Failed to convert old yaml data into sqlite data: " + e.getMessage());
		}
		
	}

	private static void writeToSQLite(StatisticModule module) {
		AbstractDatabase abstractDatabase = AbstractDatabase.getInstance();
		
		if (!(abstractDatabase instanceof SQLiteDatabase)) {
			return;
		}
		
		SQLiteDatabase database = (SQLiteDatabase) abstractDatabase;
		Table table = database.getTable(SQLStatisticDatabase.TABLE_NAME);
		
		int wins = module.getWins();
		int loses = module.getLoses();
		int knockouts = module.getKnockouts();
		int games = module.getGamesPlayed();
		int score = module.getScore();
		
		String owner = module.getName();
		
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("owner", owner);
		values.put("wins", wins);
		values.put("loses", loses);
		values.put("knockouts", knockouts);
		values.put("games", games);
		values.put("score", score);
		
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("owner", owner);
		
		table.insertOrUpdate(values, where);
	}

}
