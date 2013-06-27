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
package me.matzefratze123.heavyspleef.stats.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.matzefratze123.heavyspleef.util.Util;

import org.bukkit.plugin.Plugin;

public class Database {
	
	protected Plugin connectionHolder;
	
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;
	private Connection conn;
	
	private boolean interrupted = false;
	
	public Database(String host, int port, String database, String user, String password, Plugin plugin) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;
		this.connectionHolder = plugin;
		
		this.conn = createConnection();
	}
	
	public Database(Plugin plugin) {
		this.connectionHolder = plugin;
		
		this.host = plugin.getConfig().getString("statistic.host");
		this.port = Integer.parseInt(plugin.getConfig().getString("statistic.port"));
		this.database = plugin.getConfig().getString("statistic.databaseName");
		this.user = plugin.getConfig().getString("statistic.user");
		this.password = plugin.getConfig().getString("statistic.password");
		
		this.conn = createConnection();
	}
	
	protected void refreshConnection() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
		} catch (SQLException e) {
			connectionHolder.getLogger().severe("Could not select to MySQL server " + host + " listening on port " + port + "! Are you sure that the user and the password is entered correct?");
			e.printStackTrace();
		}
	}
	
	protected Connection getConnection() {
		try {
			if (this.conn == null || this.conn.isClosed()) {
				conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
			}
			
			return conn;
		} catch (SQLException e) {
			connectionHolder.getLogger().severe("Could not select to MySQL server " + host + " listening on port " + port + "! Are you sure that the user and the password is entered correct?");
			e.printStackTrace();
		}
		
		interrupted = true;
		return null;
	}
	
	private Connection createConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
			return conn;
		} catch (ClassNotFoundException e) {
			connectionHolder.getLogger().severe("Could not find any drivers for MySQL! Cannot connect to SQL server " + host + "!");
			e.printStackTrace();
		} catch (SQLException e) {
			connectionHolder.getLogger().severe("Could not select to MySQL server " + host + " listening on port " + port + "! Are you sure that the user and the password is entered correct?");
			e.printStackTrace();
		}
		
		interrupted = true;
		return null;
	}

	public Table createTable(String name, Map<String, Field> columns) {
		name = name.toLowerCase();
		
		String parts[] = new String[columns.size()];
		
		Set<String> keys = columns.keySet();
		
		int c = 0;
		for (String key : keys) {
			Field field = columns.get(key);
			
			parts[c] = key + " " + field.toString();
			c++;
		}
		
		String columnsString = Util.toFriendlyString(parts, ", ");
		
		try {
			Statement statement = getConnection().createStatement();
			String update = "CREATE TABLE IF NOT EXISTS " + name + " (" + columnsString + ")";
			statement.executeUpdate(update);
			return getTable(name);
		} catch (SQLException e) {
			connectionHolder.getLogger().severe("Cannot create table " + name + " on database " + database + ": " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Table getTable(String name) {
		name = name.toLowerCase();
		
		if (!hasTable(name))
			return null;
		return new Table(this, name);
	}
	
	public void deleteTable(String name) {
		name = name.toLowerCase();
		
		try {
			Statement statement = getConnection().createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS " + name);
		} catch (SQLException e) {
			connectionHolder.getLogger().severe("Cannot delete table " + name + " from database " + database + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public boolean hasTable(String name) {
		name = name.toLowerCase();
		
		try {
			List<String> tables = new ArrayList<String>();
			
			DatabaseMetaData metaData = getConnection().getMetaData();
			ResultSet result = metaData.getTables(null, null, null, new String[] {"TABLE"});
			
			while (result.next()) {
				String tableName = result.getString("TABLE_NAME");
				tables.add(tableName);
			}
			
			return tables.contains(name);
		} catch (SQLException e) {
			connectionHolder.getLogger().severe("Cannot check if table " + name + " on database " + database + " exists: " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			connectionHolder.getLogger().warning("Could not close connection to " + host + "! SQLException?");
			e.printStackTrace();
		}
	}
	
	public boolean isInterrupted() {
		return this.interrupted;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getDatabase() {
		return this.database;
	}
	
	public String getUser() {
		return this.user;
	}
	
	String getPassword() {
		return this.password;
	}

}
