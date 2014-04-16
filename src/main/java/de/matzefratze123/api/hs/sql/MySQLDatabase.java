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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.api.hs.sql;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a MySQL database
 * 
 * @author matzefratze123
 */
public class MySQLDatabase extends AbstractDatabase {

	private String	host;
	private int		port;
	private String	database;
	private String	user;
	private String	password;

	/**
	 * Constructs a new database with the specified authorization
	 */
	public MySQLDatabase(Logger logger, String host, int port, String database, String user, String password) {
		super(logger);

		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;

		tryConnect();
	}

	private void tryConnect() {
		// Try to connect
		try {
			Class.forName("com.mysql.jdbc.Driver");

			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
			state = DatabaseState.SUCCESS;
		} catch (SQLException e) {
			logger.warning("Failed to connect to the mysql database: " + e.getMessage());
			state = DatabaseState.FAILED_TO_CONNECT;
		} catch (ClassNotFoundException e) {
			logger.warning("Failed to load drivers for mysql database: " + e.getMessage());
			state = DatabaseState.NO_DRIVERS;
		} finally {
			close();
		}
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

			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
		} catch (SQLException e) {
			logger.severe("Failed to connect to database: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Gets all tables of this database
	 */
	@Override
	public Table[] getTables() throws SQLException {
		List<Table> list = new ArrayList<Table>();
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet rs = meta.getTables(null, null, "%", null);

		while (rs.next()) {
			String name = rs.getString(TABLE_NAME_COLUMN);
			list.add(new Table(logger, this, name));
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
			ResultSet result = statement.executeQuery("SHOW TABLES FROM " + database);

			final String column = "Tables_in_" + database;

			while (result.next()) {
				String tableName = result.getString(column);

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
	 * Gets the url host for this database
	 */
	@Override
	public String getHost() {
		return this.host;
	}

	/**
	 * Gets the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Gets the database name
	 */
	public String getDatabase() {
		return this.database;
	}

	/**
	 * Gets the database user
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * Gets the database password
	 */
	public String getPassword() {
		return this.password;
	}

	public void setConnectionData(String host, int port, String database, String user, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;
	}

}