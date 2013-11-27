package de.matzefratze123.heavyspleef.stats.sql;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.matzefratze123.heavyspleef.util.Logger;

public class SQLiteDatabase extends AbstractDatabase {

	private File file;
	
	public SQLiteDatabase(File file) {
		this.file = file;
		
		try {
			Class.forName("org.sqlite.JDBC");
			
			file.getParentFile().mkdirs();
			connection = DriverManager.getConnection(getHost());
		} catch (SQLException e) {
			Logger.warning("Failed to establish connection to sqlite database! Disabling statistics: " + e.getMessage());
			state = DatabaseState.FAILED_CONNECT;
		} catch (ClassNotFoundException e) {
			Logger.warning("Failed to load drivers for sqlite database. Disabling statistics: " + e.getMessage());
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
			Logger.warning("Failed to establish connection to sqlite database: " + e.getMessage());
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
			Logger.severe("Failed to check table " + name + ": " + e.getMessage());
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

}
