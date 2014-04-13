/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
package de.matzefratze123.api.hs.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Represents a sql result
 * 
 * @author matzefratze123
 */
public class SQLResult {

	private Statement	statement;
	private ResultSet	result;
	private Logger		logger;

	/**
	 * Constructs a new sql result
	 */
	public SQLResult(Logger logger, Statement statement, ResultSet result) {
		this.logger = logger;
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
			logger.severe("Failed to close result: " + e.getMessage());
			e.printStackTrace();
		}
	}

}