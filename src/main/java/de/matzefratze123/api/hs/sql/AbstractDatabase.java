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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class represents an abstract database
 * 
 * @author matzefratze123
 */
public abstract class AbstractDatabase {

	protected static final char	HIGH_TICK			= '`';
	protected static final int	TABLE_NAME_COLUMN	= 3;

	protected Connection		connection;
	protected DatabaseState		state;
	protected Logger			logger;

	/**
	 * Creates a new database
	 * 
	 * @param logger
	 *            The plugin used for exception handling etc.
	 */
	public AbstractDatabase(Logger logger) {
		this.logger = logger;

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				close();
			}
		}));
	}

	/**
	 * Creates and tries to establish a connection to the sql server
	 */
	public abstract void connect() throws SQLException;

	/**
	 * Returns the instance of the connection, created with {@link #connect()}
	 * 
	 * @see #connect()
	 * @return A connection object which represents the connection to the
	 *         database
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Closes and releases all resources associated with the current connection
	 */
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			connection = null;
		}
	}

	/**
	 * Creates a new table on the database. Make sure to call {@link #connect()}
	 * before calling this method
	 * 
	 * @param name
	 *            The name of the method, defaults to lower-case
	 * @param columns
	 *            A map which contains the columns for this table
	 * @see Field
	 * @return Creates and returns a new table object
	 */
	public Table createTable(String name, Map<String, Field> columns) {
		name = name.toLowerCase();

		String parts[] = new String[columns.size()];

		Set<String> keys = columns.keySet();

		int c = 0;
		for (String key : keys) {
			Field field = columns.get(key);

			parts[c] = HIGH_TICK + key + HIGH_TICK + " " + field.toString();
			c++;
		}

		String columnsString = SQLUtils.toFriendlyString(parts, ", ");

		try {
			connect();

			Statement statement = getConnection().createStatement();
			String update = "CREATE TABLE IF NOT EXISTS " + name + " (" + columnsString + ")";
			statement.executeUpdate(update);
			return getTable(name);
		} catch (SQLException e) {
			logger.severe("Cannot create table " + name + " on " + getHost() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			close();
		}

		return null;
	}

	/**
	 * Checks if the current database has a table
	 * 
	 * @param name
	 *            The name of the table to check
	 * @return True if this table exists, false otherwise
	 */
	public abstract boolean hasTable(String name);

	/**
	 * Gets a table on this database
	 * 
	 * @param name
	 *            The name of the table to get
	 * @return A {@link Table} object which represents the table on the database
	 */
	public Table getTable(String name) {
		if (!hasTable(name)) {
			return null;
		}

		return new Table(logger, this, name);
	}

	public abstract Table[] getTables() throws SQLException;

	/**
	 * Deletes a table on the database
	 * 
	 * @param name
	 *            The name of the table to delete
	 */
	public void deleteTable(String name) {
		name = name.toLowerCase();

		try {
			connect();

			Statement statement = getConnection().createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS " + name);
		} catch (SQLException e) {
			logger.severe("Cannot delete table " + name + " on " + getHost() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			close();
		}
	}

	/**
	 * Executes a query on this database server
	 * 
	 * @param sql
	 *            The sql sentence, mark user inputs as '?'
	 * @param params
	 *            An array of objects which should replace the '?' in the sql
	 *            sentence
	 * @return A sql-result containing the result of your query
	 * @throws SQLException
	 *             If an exception occured
	 */
	public SQLResult executeQuery(String sql, Object... params) throws SQLException {
		connect();

		PreparedStatement statement = connection.prepareStatement(sql);
		for (int i = 1; i <= params.length; i++) {
			statement.setObject(i, params[i - 1]);
		}

		ResultSet set = statement.executeQuery();
		SQLResult result = new SQLResult(logger, statement, set);

		return result;
	}

	/**
	 * Executes a update on this database server
	 * 
	 * @param sql
	 *            The sql sentence, mark user inputs as '?'
	 * @param params
	 *            An array of objects which should replace the '?' in the sql
	 *            sentence
	 * @return A return code
	 * @throws SQLException
	 *             If an exception occured
	 */
	public int executeUpdate(String sql, Object... params) throws SQLException {
		connect();

		PreparedStatement statement = connection.prepareStatement(sql);
		for (int i = 1; i <= params.length; i++) {
			statement.setObject(i, params[i - 1]);
		}

		return statement.executeUpdate();
	}

	/**
	 * Returns the raw host which points to this database
	 * 
	 * @return The host in form of a String
	 */
	public abstract String getHost();

	/**
	 * An enum which contains connection results
	 * 
	 * @author matzefratze123
	 */
	public enum DatabaseState {

		NO_DRIVERS, FAILED_TO_CONNECT, SUCCESS;

	}

}
