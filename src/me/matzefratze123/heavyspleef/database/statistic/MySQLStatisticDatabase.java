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
package me.matzefratze123.heavyspleef.database.statistic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticModule;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticManager;

import org.bukkit.Bukkit;

public class MySQLStatisticDatabase implements IStatisticDatabase {

	private String dbHost;
	private String dbPort;
	private String databaseName;
	private String dbUser;
	private String dbPassword;
	
	private final String tableName = "HeavySpleef_Statistics";
	
	private HeavySpleef plugin;
	private Connection conn;
	
	public MySQLStatisticDatabase() {
		this.plugin = HeavySpleef.instance;
		this.dbHost = plugin.getConfig().getString("statistic.host");
		this.dbPort = plugin.getConfig().getString("statistic.port");
		this.databaseName = plugin.getConfig().getString("statistic.databaseName");
		this.dbUser = plugin.getConfig().getString("statistic.user");
		this.dbPassword = plugin.getConfig().getString("statistic.password");
		createConnection();
	}
	
	private Connection getInstance() {
		try {
			if (conn == null || conn.isClosed())
			
				createConnection();
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	private void createConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        String url = "jdbc:mysql://" + dbHost + ":"
	                + dbPort + "/" + databaseName + "?" + "user="
	        		+ dbUser + "&" + "password=" + dbPassword;
			
	        conn = DriverManager.getConnection(url);
	    } catch (ClassNotFoundException e) {
	        Bukkit.getLogger().severe("No drivers found for MySQL statistic database! Changing to YAML!");
	        plugin.statisticDatabase = new YamlStatisticDatabase();
	    } catch (SQLException e) {
	    	e.printStackTrace();
	        Bukkit.getLogger().severe("Could not connect to MySQL database! Bad username or password?");
	        Bukkit.getLogger().severe("Using YAML Database!");
	        plugin.statisticDatabase = new YamlStatisticDatabase();
	    }
	}
	
	public ResultSet executeQuery(String sql) throws SQLException {
		conn = getInstance();
		Statement statement = conn.createStatement();
		return statement.executeQuery(sql);
	}
	
	public void executeUpdate(String sql) throws SQLException {
		conn = getInstance();
		Statement statement = conn.createStatement();
		statement.executeUpdate(sql);
	}
	
	public boolean hasColumn(String columnName) throws SQLException {
		conn = getInstance();
		
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet set = meta.getColumns(null, null, this.tableName, columnName);
		
		return set.next();
	}
	
	public void checkColumns() throws SQLException {
		String[] columns = new String[] {"owner", "wins", "loses", "knockouts", "games", "score"};
		
		for (String col : columns) {
			if (!hasColumn(col))
				executeUpdate("ALTER TABLE " + this.tableName + " ADD score INT");
		}
	}


	@Override
	public void save() {
		conn = getInstance();
		
		try {
			executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (owner TEXT, wins INT, loses INT, knockouts INT, games INT, score INT)");
			checkColumns();
			
			for (StatisticModule stat : StatisticManager.getStatistics()) {
				
				int wins = stat.getWins();
				int loses = stat.getLoses();
				int knockouts = stat.getKnockouts();
				int games = stat.getGamesPlayed();
				int score = stat.getScore();
				
				String owner = stat.getName();
				
				executeUpdate("INSERT INTO " + tableName + " (owner, wins, loses, knockouts, games, score) VALUES ('" + owner + "', '" + wins + "', '" + loses + "', '" + knockouts + "', '" + games + "', '" + score + "')");
			}
			conn.close();
		} catch (SQLException e) {
			plugin.getLogger().severe("An SQL Error occured while saving statistic database! Look at the error below for more information...");
			e.printStackTrace();
		}
	}

	@Override
	public void load() {
		conn = getInstance();
		
		try {
			List<String> tables = new ArrayList<String>();
			
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet table = metaData.getTables(null, null, null, new String[] {"TABLE"});
			
			while (table.next()) {
				String tableName = table.getString("TABLE_NAME");
				tables.add(tableName);
			}
			
			if (!tables.contains(tableName)) {
				plugin.getLogger().warning("WARNING! Failed to load statistics!!! Could not find a MySQL TABLE!");
				plugin.getLogger().info("Creating a new table instead...");
				executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (owner, wins, loses, knockouts, games)");
				return;
			}
			
			ResultSet stats = executeQuery("SELECT * FROM " + tableName);
			int c = 0;
			
			while (stats.next()) {
				String owner = stats.getString("owner");
				
				int wins = stats.getInt("wins");
				int loses = stats.getInt("loses");
				int knockouts = stats.getInt("knockouts");
				int games = stats.getInt("games");
				
				StatisticModule s = new StatisticModule(owner, loses, wins, knockouts, games);
				StatisticManager.addExistingStatistic(s);
				c++;
			}
			
			plugin.getLogger().info("Loaded " + c + " statistics data sets!");
		} catch (SQLException e) {
			plugin.getLogger().severe("An SQL Error occured while loading statistic database! Look at the error below for more information...");
			e.printStackTrace();
		}
	}

}
