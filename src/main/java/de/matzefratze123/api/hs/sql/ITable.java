/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.api.hs.sql;

import java.sql.SQLException;
import java.util.Map;

public interface ITable {

	/**
	 * Selects a dataset with the specific selection
	 * 
	 * @param selection
	 * @return
	 * @throws SQLException
	 */
	public SQLResult select(String selection) throws SQLException;

	public SQLResult select(String selection, Map<String, Object> where) throws SQLException;

	public SQLResult selectAll() throws SQLException;

	public SQLResult selectAll(Map<String, Object> where) throws SQLException;

	public int insertOrUpdate(Map<String, Object> values, Map<String, Object> where) throws SQLException;

	public boolean hasRow(Map<String, Object> where) throws SQLException;

	public boolean hasColumn(String column) throws SQLException;

	public int addColumn(String column, Field field) throws SQLException;

	public String getName();

	public AbstractDatabase getDatabase();

}
