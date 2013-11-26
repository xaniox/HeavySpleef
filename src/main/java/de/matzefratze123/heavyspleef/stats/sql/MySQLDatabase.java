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
package de.matzefratze123.heavyspleef.stats.sql;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.Logger;

public class MySQLDatabase extends AbstractDatabase {
	
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;
	
	public MySQLDatabase(String host, int port, String database, String user, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;
		
		tryConnect();
	}
	
	public MySQLDatabase() {
		Plugin plugin = HeavySpleef.getInstance();
		
		this.host = plugin.getConfig().getString("statistic.host");
		this.port = Integer.parseInt(plugin.getConfig().getString("statistic.port"));
		this.database = plugin.getConfig().getString("statistic.databaseName");
		this.user = plugin.getConfig().getString("statistic.user");
		this.password = plugin.getConfig().getString("statistic.password");
		
		tryConnect();
	}
	
	private void tryConnect() {
		//Try to connect
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
		} catch (SQLException e) {
			Logger.warning("Failed to connect to the mysql database! Disabling statistics: " + e.getMessage());
			AbstractDatabase.database = null;
		} catch (ClassNotFoundException e) {
			Logger.warning("Failed to load drivers for mysql database. Disabling statistics: " + e.getMessage());
			AbstractDatabase.database = null;
		} finally {
			close();
		}
	}
	
	@Override
	public void connect() {
		try {
			if (connection != null && !connection.isClosed()) {
				return;
			}
			
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
		} catch (SQLException e) {
			Logger.severe("Failed to connect to database: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean hasTable(String name) {
		name = name.toLowerCase();
		
		try {
			connect();
			
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("SHOW TABLES FROM " + database);
			
			final String column = "Tables_in_" + database;
			
			while (result.next()) {
				String tableName = result.getString(column);
				
				if (tableName.equalsIgnoreCase(name)) {
					return true;
				}
			}
		} catch (SQLException e) {
			Logger.severe("Failed to check table " + name + ": " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
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
	
	public String getPassword() {
		return this.password;
	}

	

}
