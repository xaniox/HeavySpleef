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

import de.xaniox.heavyspleef.persistence.DatabaseContext;
import snaq.db.ConnectionPool;
import snaq.db.DBPoolDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

public class SQLDatabaseContext extends DatabaseContext<SQLAccessor<?, ?>> {
	
	private static final int DATABASE_VERSION = 0;
	private static final String DATABASE_VERSION_TABLE = "heavyspleef_database_version";
    //Default connection timeout is 4 seconds
    private static final int DEFAULT_LOGIN_TIMEOUT = 4;
	
	private SQLImplementation implementationType;
	private DBPoolDataSource dataSource;
	private DatabaseUpgrader upgrader;
	
	public SQLDatabaseContext(Properties properties, DatabaseUpgrader upgrader, SQLAccessor<?, ?>... accessor) throws SQLException  {
		super(accessor);
		
		this.upgrader = upgrader;
		setupConnection(properties);
	}
	
	public SQLDatabaseContext(Properties properties, DatabaseUpgrader upgrader, Set<SQLAccessor<?, ?>> accessors) throws SQLException {
		this(properties, upgrader, accessors.toArray(new SQLAccessor[accessors.size()]));
	}
	
	private void setupConnection(Properties properties) throws SQLException {
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
			
			Object pass = properties.get("password");
			dataSource.setPassword(String.valueOf(pass));
		}
		
		int maxSize = (int) properties.get("pool-size-max");
		
		dataSource.setMaxPool(maxSize);
		dataSource.setMaxSize(maxSize);
		dataSource.setIdleTimeout((int)properties.get("idle-timeout"));

        int loginTimeout = DEFAULT_LOGIN_TIMEOUT;
        if (properties.containsKey("login-timeout")) {
            loginTimeout = (int) properties.get("login-timeout");
        }

        dataSource.setLoginTimeout(loginTimeout);
		checkDatabaseVersionTable();
	}
	
	private void checkDatabaseVersionTable() throws SQLException {
		try (Connection connection = getConnectionFromPool()) {
			DatabaseMetaData metadata = connection.getMetaData();
			boolean exists;
			
			try (ResultSet result = metadata.getTables(null, null, DATABASE_VERSION_TABLE, null)) {
				exists = result.next();
			}
			
			if (!exists) {
				final String createSql = "CREATE TABLE " + DATABASE_VERSION_TABLE + " ("
						+ "version INT NOT NULL)";
				
				try (Statement createStatement = connection.createStatement()) {
					createStatement.executeUpdate(createSql);
				}
			}
			
			try (Statement queryStatement = connection.createStatement();
					ResultSet result = queryStatement.executeQuery("SELECT version FROM " + DATABASE_VERSION_TABLE + " LIMIT 1")) {
				if (!result.next()) {
					//Just insert the id, no upgrade
					try (Statement insertStatement = connection.createStatement()) {
						insertStatement.executeUpdate("INSERT INTO " + DATABASE_VERSION_TABLE + " (version) VALUES (" + DATABASE_VERSION + ")");
					}
				} else {
					//There is an existing version saved
					int oldVersion = result.getInt("version");
					if (oldVersion < DATABASE_VERSION && upgrader != null) {
						upgrader.upgrade(connection, oldVersion, DATABASE_VERSION);
					}
					
					try (Statement updateStatement = connection.createStatement()) {
						updateStatement.executeUpdate("UPDATE " + DATABASE_VERSION_TABLE + " SET version = " + DATABASE_VERSION);
					}
				}
			}
		}
	}
	
	public void release() {
		//Fix for checking if pool has been initialized in the data source
		java.lang.reflect.Field poolField = null; 
		for (java.lang.reflect.Field field : dataSource.getClass().getDeclaredFields()) {
			if (field.getType() == ConnectionPool.class) {
				poolField = field;
			}
		}
		
		if (poolField != null) {
			poolField.setAccessible(true);
			ConnectionPool pool = null;
			
			try {
				pool = (ConnectionPool) poolField.get(dataSource);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Could not release pool: Cannot check pool for null: ", e);
			}
			
			if (pool == null) {
				//This pool is null
				return;
			}
		}
		
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
			Map<String, SQLAccessor.Field> schema = accessor.defineSchema();
			Iterator<Entry<String, SQLAccessor.Field>> iterator = schema.entrySet().iterator();
			
			while (iterator.hasNext()) {
				Entry<String, SQLAccessor.Field> entry = iterator.next();
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