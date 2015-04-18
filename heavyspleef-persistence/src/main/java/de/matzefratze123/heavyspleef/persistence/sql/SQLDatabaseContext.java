/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.persistence.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import snaq.db.DBPoolDataSource;
import de.matzefratze123.heavyspleef.persistence.DatabaseContext;
import de.matzefratze123.heavyspleef.persistence.sql.SQLAccessor.Field;

public class SQLDatabaseContext extends DatabaseContext<SQLAccessor<?, ?>> {
	
	private SQLImplementation implementationType;
	private DBPoolDataSource dataSource;
	
	public SQLDatabaseContext(Properties properties, SQLAccessor<?, ?>... accessors) {
		super(accessors);
		
		setupConnection(properties);
	}
	
	public SQLDatabaseContext(Properties properties, Set<SQLAccessor<?, ?>> accessors) {
		super(accessors);
		
		setupConnection(properties);
	}
	
	private void setupConnection(Properties properties) {
		this.implementationType = SQLImplementation.forClassName(properties.getProperty("driver"));
		
		if (implementationType == null) {
			throw new IllegalArgumentException("SQL Driver " + properties.getProperty("driver") + " is not supported");
		}
		
		for (SQLAccessor<?, ?> accessor : getAccessors()) {
			accessor.setSqlImplementation(implementationType);
		}
		
		String url = properties.getProperty("url");
		if (implementationType == SQLImplementation.SQLITE) {
			File file = extractSQLiteFileFromURL(url);
			file.getAbsoluteFile().getParentFile().mkdirs();
			
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		dataSource = new DBPoolDataSource();
		dataSource.setName(properties.getProperty("pool-name"));
		dataSource.setDescription("SQLDatabaseContext for accessing objects through SQL");
		dataSource.setDriverClassName(properties.getProperty("driver"));
		dataSource.setUrl(url);
		
		Object useAuthenticationObj = properties.get("use-authentication");
		boolean useAuthentication = useAuthenticationObj != null && useAuthenticationObj instanceof Boolean && (Boolean)useAuthenticationObj;
		
		if (useAuthentication) {
			dataSource.setUser(properties.getProperty("user"));
			dataSource.setPassword(properties.getProperty("password"));
		}
		
		int maxSize = (int) properties.get("pool-size-max");
		
		dataSource.setMaxPool(maxSize);
		dataSource.setMaxSize(maxSize);
		dataSource.setIdleTimeout((int)properties.get("idle-timeout"));
	}
	
	public void release() {
		if (dataSource != null) {
			dataSource.release();
		}
	}
	
	private File extractSQLiteFileFromURL(String urlSpec) {
		final String sqliteJDBCProtocol = "jdbc:sqlite:";
		final String fileName = urlSpec.substring(sqliteJDBCProtocol.length());
		
		return new File(fileName);
	}
	
	public Connection getConnectionFromPool() throws SQLException {
		Connection connection = dataSource.getConnection();
		return ForwardingCompatConnection.wrap(connection, implementationType);
	}
	
	public SQLImplementation getImplementationType() {
		return implementationType;
	}
	
	@SuppressWarnings("unchecked")
	public <T> void writeObject(T object) throws SQLException {
		Class<T> clazz = (Class<T>) object.getClass();
		SQLAccessor<T, ?> accessor = (SQLAccessor<T, ?>) searchAccessor(clazz);
		
		try (Connection connection = getConnectionFromPool()) {
			checkAccessorTable(accessor, connection);
			
			accessor.write(object, connection);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T, K> T readObject(K key, Class<T> objectClass) throws SQLException {
		SQLAccessor<T, K> accessor = (SQLAccessor<T, K>) searchAccessor(objectClass);
		
		try (Connection connection = getConnectionFromPool()) {
			checkAccessorTable(accessor, connection);
			
			return accessor.fetch(key, connection);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T, K> List<T> readAll(Class<T> objectClass) throws SQLException {
		SQLAccessor<T, K> accessor = (SQLAccessor<T, K>) searchAccessor(objectClass);
		
		try (Connection connection = getConnectionFromPool()) {
			checkAccessorTable(accessor, connection);
			
			return accessor.fetchAll(connection);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T, K> List<T> readSql(Class<T> objectClass, SQLQueryOptionsBuilder optionsBuilder) throws SQLException {
		SQLAccessor<T, K> accessor = (SQLAccessor<T, K>) searchAccessor(objectClass);
		
		try (Connection connection = getConnectionFromPool()) {
			checkAccessorTable(accessor, connection);
			
			return accessor.fetch(optionsBuilder, connection);
		}
	}
	
	public synchronized void checkAccessorTable(SQLAccessor<?, ?> accessor, Connection connection) throws SQLException {
		String table = accessor.getTableName();
		
		// Check if the table exists
		DatabaseMetaData metadata = connection.getMetaData();
		boolean exists = false;
		
		try (ResultSet result = metadata.getTables(null, null, table, null)) {
			exists = result.next();
		}
		
		if (!exists) {
			StringBuilder schemaBuilder = new StringBuilder();
			Map<String, Field> schema = accessor.defineSchema();
			Iterator<Entry<String, Field>> iterator = schema.entrySet().iterator();
			
			while (iterator.hasNext()) {
				Entry<String, Field> entry = iterator.next();
				schemaBuilder.append(entry.getKey())
					.append(' ')
					.append(entry.getValue().toString(implementationType));
				
				if (iterator.hasNext()) {
					schemaBuilder.append(',');
				}
			}
			
			Statement statement = connection.createStatement();
			statement.execute("CREATE TABLE " + table + " (" + schemaBuilder.toString() + ");");
		}
	}
	
	public enum SQLImplementation {
		
		MYSQL("com.mysql.jdbc.Driver"),
		SQLITE("org.sqlite.JDBC");
		
		private String driverClassName;
		
		private SQLImplementation(String driverClassName) {
			this.driverClassName = driverClassName;
		}
		
		public static SQLImplementation forClassName(String className) {
			for (SQLImplementation driver : values()) {
				if (driver.driverClassName.equals(className)) {
					return driver;
				}
			}
			
			return null;
		}
		
	}
	
}
