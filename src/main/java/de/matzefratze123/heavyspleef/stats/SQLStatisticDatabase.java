/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.stats;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import de.matzefratze123.api.hs.sql.AbstractDatabase;
import de.matzefratze123.api.hs.sql.Field;
import de.matzefratze123.api.hs.sql.Field.Type;
import de.matzefratze123.api.hs.sql.SQLResult;
import de.matzefratze123.api.hs.sql.Table;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.stats.StatisticModule.StatisticValue;
import de.matzefratze123.heavyspleef.util.Logger;

public class SQLStatisticDatabase implements IStatisticDatabase {

	public static final String			TABLE_NAME	= "heavyspleef_statistics";
	public static final File			SQLITE_FILE	= new File(HeavySpleef.getInstance().getDataFolder(), "statistic/statistic.db");

	private static Map<String, Field>	columns;

	// Database current cooldown (time to close connection etc. write-lock)
	private Cooldown					cooldown;
	private AbstractDatabase			database;

	public SQLStatisticDatabase(AbstractDatabase connectionDatabase) {
		this.cooldown = new Cooldown();
		this.database = connectionDatabase;

		checkTables();
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

	private void checkTables() {
		try {
			database.connect();

			if (!database.hasTable(TABLE_NAME)) {
				database.createTable(TABLE_NAME, columns);
			} else {
				Table table = database.getTable(TABLE_NAME);

				try {
					for (Entry<String, Field> entry : columns.entrySet()) {
						if (!table.hasColumn(entry.getKey())) {
							table.addColumn(entry.getKey(), entry.getValue());
						}
					}
				} catch (SQLException e) {
					Logger.severe("Warning: Failed to add missing columns to heavyspleef_statistics table: " + e);
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			Logger.severe("Failed to connect to the SQL database: " + e);
			e.printStackTrace();
		}
	}

	@Override
	public AbstractDatabase getRawDatabase() {
		return database;
	}

	@Override
	public void saveAccountsAsync(final ExceptionHandler exceptionHandler) {
		Bukkit.getScheduler().runTaskAsynchronously(HeavySpleef.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					saveAccounts();
				} catch (final AccountException e) {
					if (exceptionHandler != null) {
						Bukkit.getScheduler().runTask(HeavySpleef.getInstance(), new Runnable() {

							@Override
							public void run() {
								exceptionHandler.exceptionThrown(e);
							}
						});
					} else {
						Logger.severe("Failed to save statistic accounts: " + e.getMessage());
					}
				}
			}
		});
	}

	@Override
	public void saveAccountsAsync() {
		saveAccountsAsync(null);
	}

	@Override
	public synchronized void saveAccounts() throws AccountException {
		if (!cooldown.isExpired()) {
			return;
		}
		if (!isDatabaseEnabled()) {
			return;
		}

		if (!database.hasTable(TABLE_NAME)) {
			database.createTable(TABLE_NAME, columns);
		}

		try {
			Table table = database.getTable(TABLE_NAME);
			for (String columnName : columns.keySet()) {
				if (!table.hasColumn(columnName))
					table.addColumn(columnName, columns.get(columnName));
			}

			for (SpleefPlayer player : HeavySpleef.getInstance().getOnlineSpleefPlayers()) {
				if (!player.statisticsWereLoaded()) {
					continue;
				}
				if (!player.isOnline()) {
					continue;
				}

				StatisticModule stat = player.getStatistic();

				Map<StatisticValue, Integer> scores = stat.getScores();

				String owner = stat.getHolder();

				Map<String, Object> values = new HashMap<String, Object>();
				values.put("owner", stat.getHolder());

				for (StatisticValue v : StatisticValue.values()) {
					int score = 0;

					if (scores.containsKey(v)) {
						score = scores.get(v);
					}

					values.put(v.getColumnName(), score);
				}

				Map<String, Object> where = new HashMap<String, Object>();
				where.put("owner", owner);

				table.insertOrUpdate(values, where);
			}
		} catch (SQLException e) {
			throw new AccountException(e);
		} finally {
			database.close();
			cooldown.cooldown();
		}
	}

	@Override
	public synchronized StatisticModule loadAccount(String holder) throws AccountException {
		if (!isDatabaseEnabled()) {
			return null;
		}

		if (!database.hasTable(TABLE_NAME)) {
			throw new AccountException("Table heavyspleef_statistics doesn't exists");
		}

		Table table = database.getTable(TABLE_NAME);
		SQLResult result = null;

		try {
			for (String columnName : columns.keySet()) {
				if (!table.hasColumn(columnName))
					table.addColumn(columnName, columns.get(columnName));
			}

			Map<String, Object> where = new HashMap<String, Object>();

			where.put("owner", holder);
			result = table.select("*", where);
			if (result == null) {
				return null;
			}

			ResultSet set = result.getResultSet();

			StatisticModule module = null;

			if (set.next()) {
				String owner = set.getString("owner");
				int wins = set.getInt("wins");
				int loses = set.getInt("loses");
				int knockouts = set.getInt("knockouts");
				int games = set.getInt("games");

				module = new StatisticModule(owner, loses, wins, knockouts, games);
			}

			return module;
		} catch (SQLException e) {
			throw new AccountException(e);
		} finally {
			if (result != null) {
				result.close();
			}

			database.close();
		}
	}

	@Override
	public synchronized List<StatisticModule> loadAccounts() throws AccountException {
		if (!isDatabaseEnabled()) {
			return null;
		}

		if (!database.hasTable(TABLE_NAME))
			throw new AccountException("Table heavyspleef_statistics doesn't exists");

		SQLResult result = null;

		try {
			Table table = database.getTable(TABLE_NAME);
			for (String columnName : columns.keySet()) {
				if (!table.hasColumn(columnName))
					table.addColumn(columnName, columns.get(columnName));
			}

			List<StatisticModule> list = new ArrayList<StatisticModule>();
			result = table.selectAll();
			ResultSet set = result.getResultSet();

			while (set.next()) {
				String owner = set.getString("owner");
				int wins = set.getInt("wins");
				int loses = set.getInt("loses");
				int knockouts = set.getInt("knockouts");
				int games = set.getInt("games");

				StatisticModule module = new StatisticModule(owner, loses, wins, knockouts, games);
				list.add(module);
			}

			return list;
		} catch (SQLException e) {
			throw new AccountException(e);
		} finally {
			if (result != null) {
				result.close();
			}

			database.close();
		}
	}

	public static Map<String, Field> getColumns() {
		return columns;
	}

	public static boolean isDatabaseEnabled() {
		return HeavySpleef.getSystemConfig().getStatisticSection().isEnabled();
	}

	public static interface ExceptionHandler {

		public void exceptionThrown(AccountException exception);

	}

}
