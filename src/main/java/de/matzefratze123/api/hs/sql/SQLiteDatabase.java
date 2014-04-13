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
package de.matzefratze123.api.hs.sql;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a sqlite database
 * 
 * @author matzefratze123
 */
public class SQLiteDatabase extends AbstractDatabase {

	private static final String SQLITE_MASTER_TABLE = "sqlite_master";
	
	private File file;

	/**
	 * Constructs a connection to a sqlite database If the database doesn't
	 * exist, a new one will be created
	 */
	public SQLiteDatabase(Logger plugin, File file) {
		super(plugin);

		this.file = file;

		try {
			Class.forName("org.sqlite.JDBC");

			file.getParentFile().mkdirs();
			connection = DriverManager.getConnection(getHost());
		} catch (SQLException e) {
			plugin.warning("Failed to establish connection to sqlite database! Disabling statistics: " + e.getMessage());
			state = DatabaseState.FAILED_TO_CONNECT;
		} catch (ClassNotFoundException e) {
			plugin.warning("Failed to load drivers for sqlite database. Disabling statistics: " + e.getMessage());
			state = DatabaseState.NO_DRIVERS;
		} finally {
			close();
		}

		state = DatabaseState.SUCCESS;
	}

	/**
	 * Connects to the database
	 */
	@Override
	public void connect() throws SQLException {
		try {
			if (connection != null && !connection.isClosed()) {
				return;
			}

			connection = DriverManager.getConnection(getHost());
		} catch (SQLException e) {
			logger.warning("Failed to establish connection to sqlite database: " + e.getMessage());
			throw e;
		}
	}
	
	@Override
	public Table[] getTables() throws SQLException {
		SQLResult sqlr = executeQuery("SELECT * FROM " + SQLITE_MASTER_TABLE + " WHERE type = 'table'");
		ResultSet rs = sqlr.getResultSet();
		
		List<Table> list = new ArrayList<Table>();
		
		while (rs.next()) {
			list.add(new Table(logger, this, rs.getString(TABLE_NAME_COLUMN)));
		}
		
		return list.toArray(new Table[list.size()]);
	}

	/**
	 * Checks if the database has a table
	 */
	@Override
	public boolean hasTable(String name) {
		name = name.toLowerCase();

		try {
			connect();

			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM sqlite_master");

			while (result.next()) {
				String tableName = result.getString("name");

				if (tableName.equalsIgnoreCase(name)) {
					return true;
				}
			}
		} catch (SQLException e) {
			logger.severe("Failed to check table " + name + ": " + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Gets the url host of the database
	 */
	@Override
	public String getHost() {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}

	/**
	 * Gets the sqlite file
	 */
	public File getFile() {
		return file;
	}

	public void setConnectionData(File file) {
		
	}

}