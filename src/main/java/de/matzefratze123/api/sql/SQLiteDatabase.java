package de.matzefratze123.api.sql;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;

public class SQLiteDatabase extends AbstractDatabase {

	private File file;
	
	public SQLiteDatabase(Plugin plugin, File file) {
		super(plugin);
		
		this.file = file;
		
		try {
			Class.forName("org.sqlite.JDBC");
			
			file.getParentFile().mkdirs();
			connection = DriverManager.getConnection(getHost());
		} catch (SQLException e) {
			plugin.getLogger().warning("Failed to establish connection to sqlite database! Disabling statistics: " + e.getMessage());
			state = DatabaseState.FAILED_TO_CONNECT;
		} catch (ClassNotFoundException e) {
			plugin.getLogger().warning("Failed to load drivers for sqlite database. Disabling statistics: " + e.getMessage());
			state = DatabaseState.NO_DRIVERS;
		} finally {
			close();
		}
		
		state = DatabaseState.SUCCESS;
	}
	
	@Override
	public void connect() {
		try {
			if (connection != null && !connection.isClosed()) {
				return;
			}
			
			connection = DriverManager.getConnection(getHost());
		} catch (SQLException e) {
			plugin.getLogger().warning("Failed to establish connection to sqlite database: " + e.getMessage());
		}
	}
	
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
			plugin.getLogger().severe("Failed to check table " + name + ": " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public String getHost() {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}
	
	public File getFile() {
		return file;
	}

	@Override
	public SQLType getDatabaseType() {
		return SQLType.SQ_LITE;
	}

}
