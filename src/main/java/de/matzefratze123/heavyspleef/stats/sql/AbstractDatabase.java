package de.matzefratze123.heavyspleef.stats.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.Logger;
import de.matzefratze123.heavyspleef.util.Util;

public abstract class AbstractDatabase {
	
	protected Connection connection;
	protected DatabaseState state;
	
	static AbstractDatabase database;
	static final File SQLITE_FILE = new File(HeavySpleef.getInstance().getDataFolder(), "statistic/statistic.db");
	
	public abstract void connect();
	
	public Connection getConnection() {
		return connection;
	}
	
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
			connect();
			
			Statement statement = getConnection().createStatement();
			String update = "CREATE TABLE IF NOT EXISTS " + name + " (" + columnsString + ")";
			statement.executeUpdate(update);
			return getTable(name);
		} catch (SQLException e) {
			Logger.severe("Cannot create table " + name + " on " + getHost() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			close();
		}
		
		return null;
	}
	
	public abstract boolean hasTable(String name);
	
	public Table getTable(String name) {
		if (!hasTable(name)) {
			return null;
		}
		
		return new Table(this, name);
	}
	
	public void deleteTable(String name) {
		name = name.toLowerCase();
		
		try {
			connect();
			
			Statement statement = getConnection().createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS " + name);
		} catch (SQLException e) {
			Logger.severe("Cannot delete table " + name + " on " + getHost() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	public abstract String getHost();
	
	public static AbstractDatabase getInstance() {
		return database;
	}
	
	public static boolean isEnabled() {
		return database != null;
	}
	
	public static void setupDatabase() {
		ConfigurationSection section = HeavySpleef.getSystemConfig().getConfigurationSection("statistic");
		
		if (section != null) {
			if (!section.getBoolean("enabled", true)) {
				database = null;
				return;
			}
			
			String dbType = section.getString("dbType");
			
			//Convert old deprecated yaml
			if (dbType.equalsIgnoreCase("sqlite") || dbType.equalsIgnoreCase("yaml")) {
				database = new SQLiteDatabase(SQLITE_FILE);
			} else if (dbType.equalsIgnoreCase("mysql")) {
				database = new MySQLDatabase();
			} else {
				Logger.warning("Warning: Database type " + dbType + " is invalid. Disabling statistics...");
				database = null;
				return;
			}
			
			if (database.state != DatabaseState.SUCCESS) {
				Logger.warning("Failed to activate statistics: " + database.state.name());
				database = null;
			}
		}
	}
	
	public enum DatabaseState {
		
		NO_DRIVERS,
		FAILED_TO_CONNECT,
		SUCCESS;
		
	}
	
}
