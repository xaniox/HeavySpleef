package de.matzefratze123.api.hs.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;

/**
 * Represents a sql result
 * 
 * @author matzefratze123
 */
public class SQLResult {

	private Statement	statement;
	private ResultSet	result;
	private Plugin		plugin;

	/**
	 * Constructs a new sql result
	 */
	public SQLResult(Plugin plugin, Statement statement, ResultSet result) {
		this.plugin = plugin;
		this.statement = statement;
		this.result = result;
	}

	/**
	 * Gets the ResultSet
	 */
	public ResultSet getResultSet() {
		return result;
	}

	/**
	 * Closes and releases any connection associated with the result set
	 */
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