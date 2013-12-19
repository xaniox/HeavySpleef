package de.matzefratze123.api.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.Plugin;

/**
 * This class represents an abstract database.
 * This class is extended by a specific database class (e.g. MySQL, SQLite...)
 * 
 * @author matzefratze123
 */
public abstract class AbstractDatabase {
	
	protected Connection connection;
	protected DatabaseState state;
	protected Plugin plugin;
	
	/**
	 * Creates a new database
	 * 
	 * @param plugin The plugin used for exception handling etc.
	 */
	public AbstractDatabase(Plugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Creates and tries to establish a connection to the sql server
	 */
	public abstract void connect();
	
	/**
	 * Gets the type of this database
	 */
	public abstract SQLType getDatabaseType();
	
	/**
	 * Returns the instance of the connection, created with {@link #connect()}
	 * 
	 * @see #connect()
	 * 
	 * @return A connection object which represents the connection to the database
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
	 * Creates a new table on the database. Make sure
	 * to call {@link #connect()} before calling this method
	 * 
	 * @param name The name of the method, defaults to lower-case
	 * @param columns A map which contains the columns for this table
	 * 
	 * @see Field
	 * 
	 * @return Creates and returns a new table object
	 */
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
		
		String columnsString = SQLUtils.toFriendlyString(parts, ", ");
		
		try {
			connect();
			
			Statement statement = getConnection().createStatement();
			String update = "CREATE TABLE IF NOT EXISTS " + name + " (" + columnsString + ")";
			statement.executeUpdate(update);
			return getTable(name);
		} catch (SQLException e) {
			plugin.getLogger().severe("Cannot create table " + name + " on " + getHost() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			close();
		}
		
		return null;
	}
	
	/**
	 * Checks if the current database has a table
	 * 
	 * @param name The name of the table to check
	 * @return True if this table exists, false otherwise
	 */
	public abstract boolean hasTable(String name);
	
	/**
	 * Gets a table on this database
	 * 
	 * @param name The name of the table to get
	 * @return A {@link Table} object which represents the table on the database
	 */
	public Table getTable(String name) {
		if (!hasTable(name)) {
			return null;
		}
		
		return new Table(plugin, this, name);
	}
	
	/**
	 * Deletes a table on the database
	 * 
	 * @param name The name of the table to delete
	 */
	public void deleteTable(String name) {
		name = name.toLowerCase();
		
		try {
			connect();
			
			Statement statement = getConnection().createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS " + name);
		} catch (SQLException e) {
			plugin.getLogger().severe("Cannot delete table " + name + " on " + getHost() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			close();
		}
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
		
		NO_DRIVERS,
		FAILED_TO_CONNECT,
		SUCCESS;
		
	}

	public static enum SQLType {
		
		SQ_LITE,
		MYSQL;
		
	}
	
}
