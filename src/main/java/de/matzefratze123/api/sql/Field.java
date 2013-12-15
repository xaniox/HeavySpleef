/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
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
package de.matzefratze123.api.sql;

public class Field {
	
	public static enum Type {
		
		//General
		INT,
		VARCHAR,
		TEXT,
		DATE,
		
		//Numeric
		TINYINT,
		SMALLINT,
		MEDIUMINT,
		BIGINT,
		
		DECIMAL,
		FLOAT,
		DOUBLE,
		REAL,
		
		BIT,
		BOOLEAN,
		SERIAL,
		
		//Date
		DATETIME,
		TIMESTAMP,
		TIME,
		YEAR,
		
		//String
		CHAR,
		
		TINYTEXT,
		MEDIUMTEXT,
		LONGTEXT,
		
		BINARY,
		VARBINARY,
		
		TINYBLOB,
		MEDIUMBLOB,
		BLOB,
		LONGBLOB,
		
		ENUM,
		SET;
	
	}
	
	private int length = -1;
	private Type type;
	
	public Field(Type type) {
		this.type = type;
	}
	
	public Field(Type type, int length) {
		this(type);
		
		this.length = length;
	}
	
	public String toString() {
		return type.name() + (length < 0 ? "" : "(" + length + ")");
	}
	
}
