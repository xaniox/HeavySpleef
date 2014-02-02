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
	
	/**
	 * Creates a new field with the given type
	 * 
	 * @param type The type of this field
	 */
	public Field(Type type) {
		this.type = type;
	}
	
	/**
	 * Creates a new field with the given type and length 
	 * (the CHAR type for example needs the length of chars that can be stored at maximum)
	 * 
	 * @param type The type of this field
	 * @param length The length of this field (as described above)
	 */
	public Field(Type type, int length) {
		this(type);
		
		this.length = length;
	}
	
	/**
	 * Turns this field into a string which can be used in a sql statement  
	 */
	@Override
	public String toString() {
		return type.name() + (length < 0 ? "" : "(" + length + ")");
	}
	
}
