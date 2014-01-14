/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.matzefratze123.api.sql.AbstractDatabase;
import de.matzefratze123.api.sql.Field;
import de.matzefratze123.api.sql.Field.Type;
import de.matzefratze123.api.sql.SQLResult;
import de.matzefratze123.api.sql.Table;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Logger;

public class SQLStatisticDatabase implements IStatisticDatabase {
	
	public static final String TABLE_NAME = "HeavySpleef_Statistics";
	public static final File SQLITE_FILE = new File(HeavySpleef.getInstance().getDataFolder(), "statistic/statistic.db");
	
	private static Map<String, Field> columns;
	
	//Database current cooldown (time to close connection etc. write-lock)
	private Cooldown cooldown;
	private AbstractDatabase database;
	
	public SQLStatisticDatabase(AbstractDatabase connectionDatabase) {
		this.cooldown = new Cooldown();
		this.database = connectionDatabase;
	}
	
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
	public AbstractDatabase getRawDatabase() {
		return database;
	}
	
	@Override
	public synchronized void saveAccounts() {
		if (!cooldown.isExpired()) {
			return;
		}
		if (!isDatabaseEnabled()) {
			return;
		}
		
		if (!database.hasTable(TABLE_NAME))
			database.createTable(TABLE_NAME, columns);
		
		Table table = database.getTable(TABLE_NAME);
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
		cooldown.cooldown();
	}

	@Override
	public synchronized StatisticModule loadAccount(String holder) {
		if (!isDatabaseEnabled()) {
			return null;
		}
		
		if (!database.hasTable(TABLE_NAME)) {
			return null;
		}
		
		Table table = database.getTable(TABLE_NAME);
		for (String columnName : columns.keySet()) {
			if (!table.hasColumn(columnName))
				table.addColumn(columnName, columns.get(columnName));
		}
		
		Map<String, Object> where = new HashMap<String, Object>();
		
		where.put("owner", holder);
		SQLResult result = table.select("*", where);
		ResultSet set = result.getResultSet();
		
		StatisticModule module = null;
		
		try {
			
			if (!set.next()) {
				module = null;
			} else {
				String owner = set.getString("owner");
				int wins = set.getInt("wins");
				int loses = set.getInt("loses");
				int knockouts = set.getInt("knockouts");
				int games = set.getInt("games");
				
				module = new StatisticModule(owner, loses, wins, knockouts, games);
			}
			
		} catch (SQLException e) {
			Logger.severe("Failed to load statistics for user " + holder + ": " + e.getMessage());
			e.printStackTrace();
		}
		
		result.close();
		database.close();
		
		return module;
	}

	@Override
	public synchronized void unloadAccount(SpleefPlayer player) {
		if (!isDatabaseEnabled()) {
			return;
		}
		if (!cooldown.isExpired()) {
			return;
		}
		
		StatisticModule module = player.getStatistic();
		
		if (!database.hasTable(TABLE_NAME))
			database.createTable(TABLE_NAME, columns);
		
		Table table = database.getTable(TABLE_NAME);
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
		cooldown.cooldown();
	}

	@Override
	public synchronized List<StatisticModule> loadAccounts() {
		if (!isDatabaseEnabled()) {
			return null;
		}
		
		if (!database.hasTable(TABLE_NAME))
			return null;
		
		Table table = database.getTable(TABLE_NAME);
		for (String columnName : columns.keySet()) {
			if (!table.hasColumn(columnName))
				table.addColumn(columnName, columns.get(columnName));
		}
		
		List<StatisticModule> list = new ArrayList<StatisticModule>();
		SQLResult result = table.selectAll();
		ResultSet set = result.getResultSet();
		
		try {
			while (set.next()) {
				String owner = set.getString("owner");
				int wins = set.getInt("wins");
				int loses = set.getInt("loses");
				int knockouts = set.getInt("knockouts");
				int games = set.getInt("games");
				
				StatisticModule module = new StatisticModule(owner, loses, wins, knockouts, games);
				list.add(module);
			}
			
		} catch (SQLException e) {
			Logger.severe("Failed to load statistics: " + e.getMessage());
			e.printStackTrace();
		}
		
		result.close();
		database.close();
		
		return list;
	}
	
	public static Map<String, Field> getColumns() {
		return columns;
	}
	
	public static boolean isDatabaseEnabled() {
		return HeavySpleef.getSystemConfig().getStatisticSection().isEnabled();
	}
	
}
