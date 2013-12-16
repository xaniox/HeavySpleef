package de.matzefratze123.api.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;

public class SQLResult {
	
	private Statement statement;
	private ResultSet result;
	private Plugin plugin;
	
	public SQLResult(Plugin plugin, Statement statement, ResultSet result) {
		this.plugin = plugin;
		this.statement = statement;
		this.result = result;
	}
	
	public ResultSet getResultSet() {
		return result;
	}
	
	public void close() {
		try {
			statement.close();
			result.close();
		} catch (SQLException e) {
			plugin.getLogger().severe("Failed to close result: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
