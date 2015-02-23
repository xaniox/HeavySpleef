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

/**
 * This class represents a field type for a sql table
 * 
 * @author matzefratze123
 */
public class Field {

	/**
	 * An enum which contains raw field types
	 * 
	 * @author matzefratze123
	 */
	public static enum Type {

		// General
		INT, VARCHAR, TEXT, DATE,

		// Numeric
		TINYINT, SMALLINT, MEDIUMINT, BIGINT,

		DECIMAL, FLOAT, DOUBLE, REAL,

		BIT, BOOLEAN, SERIAL,

		// Date
		DATETIME, TIMESTAMP, TIME, YEAR,

		// String
		CHAR,

		TINYTEXT, MEDIUMTEXT, LONGTEXT,

		BINARY, VARBINARY,

		TINYBLOB, MEDIUMBLOB, BLOB, LONGBLOB,

		ENUM, SET;

	}

	private int		length	= -1;
	private Type	type;
	private boolean	notNull;
	private Object	defaulte;

	/**
	 * Creates a new field with the given type
	 * 
	 * @param type
	 *            The type of this field
	 */
	public Field(Type type) {
		this.type = type;
	}

	/**
	 * Creates a new field with the given type and length (the CHAR type for
	 * example needs the length of chars that can be stored at maximum)
	 * 
	 * @param type
	 *            The type of this field
	 * @param length
	 *            The length of this field (as described above)
	 */
	public Field(Type type, int length) {
		this(type);

		this.length = length;
	}

	/**
	 * Creates a new field with the given type, length and a notnull option
	 * 
	 * @param type
	 *            The type of this field
	 * @param length
	 *            The length of this field
	 * @param notNull
	 *            Wether this field is a non-null field
	 */
	public Field(Type type, int length, boolean notNull) {
		this(type, length);

		this.notNull = notNull;
	}

	public Field(Type type, int length, boolean notNull, Object defaulte) {
		this(type, length, notNull);

		this.defaulte = defaulte;
	}

	public Field(Type type, int length, Object defaulte) {
		this(type, length);

		this.defaulte = defaulte;
	}

	public Field(Type type, Object defaulte) {
		this(type);

		this.defaulte = defaulte;
	}

	public Field(Type type, boolean notNull, Object defaulte) {
		this(type, notNull);

		this.defaulte = defaulte;
	}

	/**
	 * Creates a new field with the given type and a notnull option
	 * 
	 * @param type
	 *            The type of this field
	 * @param notNull
	 *            Wether this field is a non-null field
	 */
	public Field(Type type, boolean notNull) {
		this(type);

		this.notNull = notNull;
	}

	/**
	 * Turns this field into a string which can be used in a sql statement
	 */
	@Override
	public String toString() {
		return type.name() + (length < 0 ? "" : "(" + length + ")") + (notNull ? " NOT NULL" : "") + (defaulte == null ? "" : " DEFAULT '" + defaulte + "'");
	}

}
