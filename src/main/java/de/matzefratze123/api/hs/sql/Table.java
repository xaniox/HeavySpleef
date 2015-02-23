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
package de.matzefratze123.api.hs.sql;

import static de.matzefratze123.api.hs.sql.SQLUtils.createParameterizedWhereClause;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Table implements ITable {

	private Logger				plugin;
	private AbstractDatabase	database;
	private String				name;

	private static final char	HIGH_TICK	= '`';

	Table(Logger plugin, AbstractDatabase database, String name) {
		this.plugin = plugin;
		this.database = database;
		this.name = name;
	}

	/**
	 * Selects a data-set of this table
	 * 
	 * @param selection
	 *            The selection
	 * @param where
	 *            A "where" clause where the key is the column and the value the
	 *            row data-set
	 * @return A ResultSet containing all datasets of this selection
	 */
	@Override
	public SQLResult select(String selection, Map<String, Object> where) throws SQLException {
		String parameterizedClause = where == null ? null : createParameterizedWhereClause(where.keySet());
		PreparedStatement statement;

		Connection conn = database.getConnection();

		if (selection.trim().equalsIgnoreCase("*") || selection.trim().equalsIgnoreCase("all")) {
			selection = "*";
		}

		statement = conn.prepareStatement("SELECT " + selection + " FROM " + name + (parameterizedClause == null ? "" : parameterizedClause));

		if (where != null) {
			int index = 1;
			for (Object o : where.values()) {
				statement.setObject(index, o);

				index++;
			}
		}

		ResultSet result = statement.executeQuery();
		return new SQLResult(plugin, statement, result);
	}

	/**
	 * Selects a data-set of this table
	 * 
	 * @param selection
	 *            The selection
	 * @return A SQLResult containing all datasets of this selection
	 * @see #select(String, Map)
	 */
	@Override
	public SQLResult select(String selection) throws SQLException {
		return select(selection, null);
	}

	/**
	 * Selects all columns and all rows of the database
	 * 
	 * @return A SQLResult containing all datasets of the table
	 */
	@Override
	public SQLResult selectAll() throws SQLException {
		return select("*");
	}

	/**
	 * Selects all columns of this database with the specific where clause
	 * 
	 * @param where
	 *            A where clause in a map
	 */
	@Override
	public SQLResult selectAll(Map<String, Object> where) throws SQLException {
		return select("*", where);
	}

	/**
	 * Inserts or updates values in the table. If there is no entry yet, it will
	 * insert the data otherwise it updates the data
	 * 
	 * @param values
	 *            The values to insert
	 * @param where
	 *            A where clause, key as the column name value as the field
	 *            value
	 */
	@Override
	public int insertOrUpdate(Map<String, Object> values, Map<String, Object> where) throws SQLException {
		int result = Statement.EXECUTE_FAILED;

		PreparedStatement statement = null;

		synchronized (this) {
			try {
				Connection conn = database.getConnection();

				if (where != null && hasRow(where)) {
					// Update part syntax beginn
					String parts[] = new String[values.size()];
					Set<String> keys = values.keySet();

					int c = 0;
					for (String key : keys) {
						parts[c] = HIGH_TICK + key + HIGH_TICK + " = ?";
						c++;
					}

					String update = SQLUtils.toFriendlyString(parts, ", ");
					// Update Part syntax end
					String whereClause = createParameterizedWhereClause(where.keySet());

					statement = conn.prepareStatement("UPDATE " + name + " SET " + update + (whereClause == null ? "" : whereClause));
					int index = 1;
					for (Object o : values.values()) {
						statement.setObject(index, o);
						index++;
					}
					for (Object o : where.values()) {
						statement.setObject(index, o);
						index++;
					}

					result = statement.executeUpdate();
				} else {
					String friendlyKeySet = HIGH_TICK + SQLUtils.toFriendlyString(values.keySet(), HIGH_TICK + ", " + HIGH_TICK) + HIGH_TICK;

					StringBuilder builder = new StringBuilder();
					for (int i = 0; i < values.size(); i++) {
						builder.append("?");
						if (i + 1 < values.size()) {
							builder.append(", ");
						}
					}

					statement = conn.prepareStatement("INSERT INTO " + name + "(" + friendlyKeySet + ") VALUES (" + builder.toString() + ")");

					int index = 1;
					for (Object o : values.values()) {
						statement.setObject(index, o);
						index++;
					}

					result = statement.executeUpdate();
				}
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks if the table contains a row
	 * 
	 * @param where
	 *            A where clause, key as the column name value as the field
	 *            value
	 * @return True if the row exists, false otherwise
	 */
	@Override
	public boolean hasRow(Map<String, Object> where) throws SQLException {
		String parameterizedWhereClause = createParameterizedWhereClause(where.keySet());

		PreparedStatement statement = null;
		ResultSet set = null;

		try {
			Connection conn = database.getConnection();
			statement = conn.prepareStatement("SELECT * FROM " + name + parameterizedWhereClause);

			int index = 1;
			for (Object o : where.values()) {
				statement.setObject(index, o);
				index++;
			}

			set = statement.executeQuery();
			return set.next();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}

				if (set != null) {
					set.close();
				}
			} catch (SQLException e) {
			}
		}
	}

	public int deleteRow(Map<String, Object> where) throws SQLException {
		String paramterizedWhereClause = createParameterizedWhereClause(where.keySet());

		PreparedStatement statement = null;
		int returnCode = 0;

		try {
			Connection conn = database.getConnection();
			statement = conn.prepareStatement("DELETE FROM " + name + paramterizedWhereClause);

			int index = 1;
			for (Object o : where.values()) {
				statement.setObject(index, o);
				index++;
			}

			returnCode = statement.executeUpdate();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
			}
		}

		return returnCode;
	}

	/**
	 * Checks if this table contains a column with the given name
	 * 
	 * @return True if the table contains the column, false otherwise
	 */
	@Override
	public boolean hasColumn(String column) throws SQLException {
		ResultSet set = null;

		try {
			Connection conn = database.getConnection();
			DatabaseMetaData meta = conn.getMetaData();
			set = meta.getColumns(null, null, name, column);

			boolean next = set.next();

			return next;
		} finally {
			if (set != null) {
				try {
					set.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * Adds a column to this table
	 * 
	 * @param name
	 *            The name of the column
	 * @param field
	 *            The type of the column
	 */
	@Override
	public int addColumn(String name, Field field) throws SQLException {
		int result = Statement.EXECUTE_FAILED;

		Statement statement = null;

		try {
			Connection conn = database.getConnection();

			statement = conn.createStatement();
			result = statement.executeUpdate("ALTER TABLE " + this.name + " ADD " + HIGH_TICK + name + HIGH_TICK + " " + field);

			statement.close();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
		}

		return result;
	}

	public int rename(String newName) throws SQLException {
		int result = Statement.EXECUTE_FAILED;

		Statement statement = null;

		try {
			Connection conn = database.getConnection();

			statement = conn.createStatement();
			result = statement.executeUpdate("RENAME TABLE " + name + " TO " + newName);

			statement.close();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
		}

		return result;
	}

	/**
	 * Gets the name of this table
	 * 
	 * @return The name of this table
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the database of this table
	 * 
	 * @return The database of this table
	 */
	@Override
	public AbstractDatabase getDatabase() {
		return this.database;
	}

}