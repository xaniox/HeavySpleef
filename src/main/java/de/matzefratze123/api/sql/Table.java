/**
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013 matzefratze123
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
package de.matzefratze123.api.sql;

import static de.matzefratze123.api.sql.SQLUtils.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Table {

	private Plugin				plugin;
	private AbstractDatabase	database;
	private String				name;

	Table(Plugin plugin, AbstractDatabase database, String name) {
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
	public SQLResult select(String selection, Map<String, Object> where) {
		String parameterizedClause = createParameterizedWhereClause(where.keySet());
		PreparedStatement statement;
		//Statement statement = null;

		try {
			Connection conn = database.getConnection();

			if (selection.trim().equalsIgnoreCase("*") || selection.trim().equalsIgnoreCase("all")) {
				selection = "*";
			}
			
			statement = conn.prepareStatement("SELECT " + selection + " FROM " + name + (parameterizedClause == null ? "" : parameterizedClause));
			int index = 1;
			for (Object o : where.values()) {
				statement.setObject(index, o);
				
				index++;
			}
			
			ResultSet result = statement.executeQuery();
			return new SQLResult(plugin, statement, result);
		} catch (SQLException e) {
			Bukkit.getLogger().severe("SQL Exception occured while trying to select " + selection + " from table " + name + " in database: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Selects a data-set of this table
	 * 
	 * @param selection
	 *            The selection
	 * @return A SQLResult containing all datasets of this selection
	 * @see #select(String, Map)
	 */
	public SQLResult select(String selection) {
		return select(selection, null);
	}

	/**
	 * Selects all of the database
	 * 
	 * @return A SQLResult containing all datasets of the table
	 */
	public SQLResult selectAll() {
		return select("*");
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
	public int insertOrUpdate(Map<String, Object> values, Map<String, Object> where) {
		int result = Statement.EXECUTE_FAILED;

		PreparedStatement statement = null;

		try {
			Connection conn = database.getConnection();
			
			if (hasRow(where)) {
				// Update part syntax beginn
				String parts[] = new String[values.size()];
				Set<String> keys = values.keySet();

				int c = 0;
				for (String key : keys) {
					parts[c] = key + " = ?";
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
				String friendlyKeySet = SQLUtils.toFriendlyString(values.keySet(), ", ");
				
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

		} catch (SQLException e) {
			e.printStackTrace();
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
	 * Checks if the table contains a row
	 * 
	 * @param where
	 *            A where clause, key as the column name value as the field
	 *            value
	 * @return True if the row exists, false otherwise
	 */
	public boolean hasRow(Map<String, Object> where) {
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
		} catch (SQLException e) {
			Bukkit.getLogger().severe("SQLException while checking if row exists: " + e.getMessage());
			e.printStackTrace();
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

		return false;
	}

	/**
	 * Checks if this table contains a column with the given name
	 * 
	 * @return True if the table contains the column, false otherwise
	 */
	public boolean hasColumn(String column) {
		ResultSet set = null;

		try {
			Connection conn = database.getConnection();
			DatabaseMetaData meta = conn.getMetaData();
			set = meta.getColumns(null, null, name, column);

			boolean next = set.next();

			return next;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("Could not check column " + column + " in table " + name + ": " + e.getMessage());
		} finally {
			if (set != null) {
				try {
					set.close();
				} catch (SQLException e) {
				}
			}
		}

		return false;
	}

	/**
	 * Adds a column to this table
	 * 
	 * @param name
	 *            The name of the column
	 * @param field
	 *            The type of the column
	 */
	public int addColumn(String name, Field field) {
		int result = Statement.EXECUTE_FAILED;

		Statement statement = null;

		try {
			Connection conn = database.getConnection();

			statement = conn.createStatement();
			result = statement.executeUpdate("ALTER TABLE " + this.name + " ADD " + name + " " + field);

			statement.close();
		} catch (SQLException e) {
			Bukkit.getLogger().severe("Could not add column " + name + " to the table " + this.name + ": " + e.getMessage());
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
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the database of this table
	 * 
	 * @return The database of this table
	 */
	public AbstractDatabase getDatabase() {
		return this.database;
	}

}