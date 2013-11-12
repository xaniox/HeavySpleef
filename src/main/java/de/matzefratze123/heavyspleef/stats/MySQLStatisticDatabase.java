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
package de.matzefratze123.heavyspleef.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.stats.sql.Database;
import de.matzefratze123.heavyspleef.stats.sql.Field;
import de.matzefratze123.heavyspleef.stats.sql.Field.Type;
import de.matzefratze123.heavyspleef.stats.sql.Table;
import de.matzefratze123.heavyspleef.util.Logger;


public class MySQLStatisticDatabase implements IStatisticDatabase {
	
	private static final String tableName = "HeavySpleef_Statistics";
	private static Map<String, Field> columns;
	
	static {
		if (columns == null) {
			columns = new HashMap<String, Field>();
			columns.put("owner", new Field(Type.CHAR, 16));
			columns.put("wins", new Field(Type.INT));
			columns.put("loses", new Field(Type.INT));
			columns.put("knockouts", new Field(Type.INT));
			columns.put("games", new Field(Type.INT));
			columns.put("score", new Field(Type.INT));
		}
	}
	
	@Override
	public void saveAccounts() {
		Database database = new Database(HeavySpleef.getInstance());
		if (!database.hasTable(tableName))
			database.createTable(tableName, columns);
		
		Table table = database.getTable(tableName);
		for (String columnName : columns.keySet()) {
			if (!table.hasColumn(columnName))
				table.addColumn(columnName, columns.get(columnName));
		}
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			StatisticModule stat = HeavySpleef.getInstance().getSpleefPlayer(player).getStatistic();
			
			int wins = stat.getWins();
			int loses = stat.getLoses();
			int knockouts = stat.getKnockouts();
			int games = stat.getGamesPlayed();
			int score = stat.getScore();
			
			String owner = stat.getName();
			
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
		
		database.close();
	}

	@Override
	public StatisticModule loadAccount(String holder) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(holder);
		if (player != null) {
			return player.getStatistic();
		}
		
		Database database = new Database(HeavySpleef.getInstance());
		if (!database.hasTable(tableName))
			return null;
		
		Table table = database.getTable(tableName);
		for (String columnName : columns.keySet()) {
			if (!table.hasColumn(columnName))
				table.addColumn(columnName, columns.get(columnName));
		}
		
		Map<String, Object> where = new HashMap<String, Object>();
		
		where.put("owner", holder);
		ResultSet result = table.select("*", where);
		
		StatisticModule module = null;
		
		try {
			
			if (!result.next()) {
				module = null;
			} else {
				String owner = result.getString("owner");
				int wins = result.getInt("wins");
				int loses = result.getInt("loses");
				int knockouts = result.getInt("knockouts");
				int games = result.getInt("games");
				
				module = new StatisticModule(owner, loses, wins, knockouts, games);
			}
			
		} catch (SQLException e) {
			Logger.severe("Failed to load statistics for user " + holder + ": " + e.getMessage());
			e.printStackTrace();
		}
		
		database.close();
		
		return module;
	}

	@Override
	public void unloadAccount(SpleefPlayer player) {
		StatisticModule module = player.getStatistic();
		
		Database database = new Database(HeavySpleef.getInstance());
		if (!database.hasTable(tableName))
			database.createTable(tableName, columns);
		
		Table table = database.getTable(tableName);
		for (String columnName : columns.keySet()) {
			if (!table.hasColumn(columnName))
				table.addColumn(columnName, columns.get(columnName));
		}
			
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
		
		database.close();
	}

	@Override
	public List<StatisticModule> loadAccounts() {
		Database database = new Database(HeavySpleef.getInstance());
		if (!database.hasTable(tableName))
			return null;
		
		Table table = database.getTable(tableName);
		for (String columnName : columns.keySet()) {
			if (!table.hasColumn(columnName))
				table.addColumn(columnName, columns.get(columnName));
		}
		
		List<StatisticModule> list = new ArrayList<StatisticModule>();
		ResultSet result = table.select("*");
		
		try {
			while (result.next()) {
				String owner = result.getString("owner");
				int wins = result.getInt("wins");
				int loses = result.getInt("loses");
				int knockouts = result.getInt("knockouts");
				int games = result.getInt("games");
				
				StatisticModule module = new StatisticModule(owner, loses, wins, knockouts, games);
				list.add(module);
			}
			
		} catch (SQLException e) {
			Logger.severe("Failed to load statistics: " + e.getMessage());
			e.printStackTrace();
		}
		
		database.close();
		
		return list;
	}

}
