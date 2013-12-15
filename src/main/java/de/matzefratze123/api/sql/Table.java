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
package de.matzefratze123.api.sql;

import static de.matzefratze123.api.sql.SQLUtils.TICK;
import static de.matzefratze123.api.sql.SQLUtils.parseWhereClause;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;

import de.matzefratze123.heavyspleef.util.Util;

public class Table {
	
	private AbstractDatabase database;
	private String name;
	
	Table(AbstractDatabase database, String name) {
		this.database = database;
		this.name = name;
	}
	
	/**
	 * Selects a data-set of this table
	 * 
	 * @param selection The selection
	 * @param where A "where" clause where the key is the column and the value the row data-set
	 * 
	 * @return A ResultSet containing all datasets of this selection
	 */
	public SQLResult select(String selection, Map<String, Object> where) {
		String whereClause = parseWhereClause(where);
		Statement statement = null;
		
		try {
			Connection conn = database.getConnection();
			statement = conn.createStatement();
			
			if (selection.trim().equalsIgnoreCase("*") || selection.trim().equalsIgnoreCase("all")) {
				selection = "*";
			}
			
			ResultSet result = statement.executeQuery("SELECT " + selection + " FROM " + name + (whereClause == null ? "" : whereClause));
			return new SQLResult(statement, result);
		} catch (SQLException e) {
			Bukkit.getLogger().severe("SQL Exception occured while trying to select " + selection + " from table " + name + " in database: " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Selects a data-set of this table
	 * 
	 * @param selection The selection
	 * @return A ResultSet containing all datasets of this selection
	 * 
	 * @see #select(String, Map)
	 */
	public SQLResult select(String selection) {
		return select(selection, null);
	}
	
	public SQLResult selectAll() {
		return select("*");
	}
	
	public int insertOrUpdate(Map<String, Object> values, Map<String, Object> where) {
		int result = Statement.EXECUTE_FAILED;
		
		Statement statement = null;
		
		try {
			Connection conn = database.getConnection();
			statement = conn.createStatement();
			
			if (hasRow(where)) {
				//Update part syntax beginn
				String parts[] = new String[values.size()];
				Set<String> keys = values.keySet();
				
				int c = 0;
				for (String key : keys) {
					Object value = values.get(key);
					
					parts[c] = key + " = '" + value + "'";
					c++;
				}
				
				String update = Util.toFriendlyString(parts, ", ");
				//Update Part syntax end
				String whereClause = parseWhereClause(where);
				
				result = statement.executeUpdate("UPDATE " + name + " SET " + update + (whereClause == null ? "" : whereClause));
			} else {
				String friendlyKeySet = Util.toFriendlyString(values.keySet(), ", ");
				String friendlyValueSet = Util.toFriendlyString(values.values(), "', '");
				
				//Ticks am Ende und anfang hinzufügen
				friendlyValueSet += "'";
				friendlyValueSet = "'" + friendlyValueSet;
				
				result = statement.executeUpdate("INSERT INTO " + name + " (" + friendlyKeySet + ") VALUES (" + friendlyValueSet + ")");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {}
			}
		}
		
		return result;
	}
	
	public boolean hasRow(Map<String, Object> where) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> iter = where.keySet().iterator();
		builder.append(" WHERE ");
		
		while(iter.hasNext()) {
			String next = iter.next();
			if (!hasColumn(next)) {
				continue;
			}
			
			Object value = where.get(next);
			
			builder.append(next).append("=").append(TICK).append(value).append(TICK);
			if (iter.hasNext())
				builder.append(" AND ");
		}
		
		String whereClause = builder.toString();
		
		Statement statement = null;
		ResultSet set = null;
		
		try {
			Connection conn = database.getConnection();
			statement = conn.createStatement();
			
			set = statement.executeQuery("SELECT * FROM " + name + whereClause);
			boolean next = set.next();
			
			return next;
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
			} catch (SQLException e) {}
		}
		
		return false;
	}
	
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
				} catch (SQLException e) {}
			}
		}
		
		return false;
	}
	
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
				} catch (SQLException e) {}
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
