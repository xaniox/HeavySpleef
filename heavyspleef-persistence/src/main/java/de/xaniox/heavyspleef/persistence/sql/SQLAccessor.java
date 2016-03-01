/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.xaniox.heavyspleef.persistence.sql;

import de.xaniox.heavyspleef.persistence.ObjectDatabaseAccessor;
import de.xaniox.heavyspleef.persistence.sql.SQLDatabaseContext.SQLImplementation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class SQLAccessor<T, K> implements ObjectDatabaseAccessor<T> {
	
	private SQLImplementation implementation;
	
	public SQLAccessor() {}
	
	public abstract String getTableName();
	
	public abstract Map<String, Field> defineSchema();
	
	public abstract void write(T object, Connection connection) throws SQLException;
	
	public abstract T fetch(K key, Connection connection) throws SQLException;
	
	public List<T> fetchAll(Connection connection) throws SQLException {
		return fetch((SQLQueryOptionsBuilder)null, connection);
	}
	
	public abstract List<T> fetch(SQLQueryOptionsBuilder optionsBuilder, Connection connection) throws SQLException;
	
	protected void setSqlImplementation(SQLImplementation implementation) {
		this.implementation = implementation;
	}
	
	protected SQLImplementation getSqlImplementation() {
		return implementation;
	}
	
	public static class Field {
		
		private Type type;
		private int length;
		private boolean notNull;
		private boolean primaryKey;
		private boolean autoIncrement;
		private boolean unique;
		
		public Field(Type type) {
			this.type = type;
		}
		
		public Field(Type type, int length) {
			this(type);
			this.length = length;
		}
		
		public Field length(int length) {
			this.length = length;
			return this;
		}
		
		public Field notNull() {
			notNull = true;
			return this;
		}
		
		public Field primaryKey() {
			primaryKey = true;
			return this;
		}
		
		public Field autoIncrement() {
			autoIncrement = true;
			return this;
		}
		
		public Field unique() {
			unique = true;
			return this;
		}
		
		public String toString(SQLImplementation impl) {
			return type.name()
					+ (length > 0 ? "(" + length + ")" : "")
					+ (notNull ? " NOT NULL" : "")
					+ (primaryKey ? " PRIMARY KEY" : "")
					+ (autoIncrement ? impl == SQLImplementation.MYSQL ? " AUTO_INCREMENT"
							: impl == SQLImplementation.SQLITE ? " AUTOINCREMENT" : "" : "") + (unique ? " UNIQUE" : "");
		}
		
		public enum Type {
			
			/* Numeric types */
			INTEGER,
			TINYINT,
			SMALLINT,
			MEDIUMINT,
			BIGINT,
			FLOAT,
			DOUBLE,
			
			/* Time types */
			DATE,
			DATETIME,
			TIMESTAMP,
			TIME,
			YEAR,
			
			/* String types */
			CHAR,
			VARCHAR,
			TEXT,
			TINYTEXT,
			MEDIUMTEXT,
			LONGTEXT,
			ENUM;
			
		}
		
	}
	
}