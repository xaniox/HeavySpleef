package de.matzefratze123.api.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.matzefratze123.heavyspleef.util.Logger;

public class SQLResult {
	
	private Statement statement;
	private ResultSet result;
	
	public SQLResult(Statement statement, ResultSet result) {
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
			Logger.severe("Failed to close result: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
