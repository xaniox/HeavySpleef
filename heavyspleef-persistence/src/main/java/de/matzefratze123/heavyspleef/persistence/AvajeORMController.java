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
package de.matzefratze123.heavyspleef.persistence;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.LogLevel;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;

public class AvajeORMController implements DatabaseController {

	public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DEFAULT_DRIVER = MYSQL_DRIVER;
	
	private List<Class<?>> beanClasses;
	private Logger logger;
	private Level loggerLevel;
	private ClassLoader pluginClassLoader;
	private ServerConfig serverConfig;
	private EbeanServer ebeanServer;
	private boolean usingSQLite;
	
	public AvajeORMController(JavaPlugin plugin, List<Class<?>> beanClasses, Properties serverProperties) throws Exception {
		Validate.notNull(plugin, "plugin cannot be null");
		Validate.notNull(beanClasses, "beanClasses cannot be null");
		Validate.notNull(serverProperties, "serverProperties cannot be null");
		Validate.isTrue(!this.beanClasses.isEmpty(), "beanClasses cannot be empty (length = 0)");
		
		this.beanClasses = beanClasses;
		this.logger = plugin.getLogger();
		
		boolean accessible = false;
		Method method = null;
		
		try {
			method = JavaPlugin.class.getDeclaredMethod("getClassLoader");
			accessible = method.isAccessible();
			method.setAccessible(true);
			
			pluginClassLoader = (ClassLoader) method.invoke(plugin);
		} catch (Exception e) {
			throw new Exception("Could not get plugin classloader", e);
		} finally {
			if (method != null) {
				method.setAccessible(accessible);
			}
		}
		
		setupDatabase(serverProperties);
	}
	
	@Override
	public void update(Object object) {
		update(object, null);
	}
	
	@Override
	public void update(Object object, Object cookie) {
		Validate.notNull(object);
		
		Class<?> clazz = object.getClass();
		
		//Check if class is registered
		Validate.isTrue(beanClasses.contains(clazz), "object.getClass() must be a registered class");
		
		Transaction transaction = null;
		
		try {
			//Create a transaction
			transaction = ebeanServer.beginTransaction();
			transaction.setLogLevel(LogLevel.NONE);
			
			//Submit the object
			ebeanServer.save(object);
			
			//Commit transaction
			transaction.commit();
		} finally {
			if (transaction != null) {
				//End transaction
				transaction.end();
			}
		}
	}
	
	@Override
	public void update(Object[] objects) {
		update(objects, null);
	}
	
	@Override
	public void update(Object[] objects, Object cookie) {
		update(Arrays.asList(objects), cookie);
	}
	
	@Override
	public void update(Iterable<?> iterable) {
		update(iterable, null);
	}
	
	/* Method for bulk inserts */
	@Override
	public void update(Iterable<?> iterable, Object cookie) {
		Validate.notNull(iterable);
		Iterator<?> iterator = iterable.iterator();
		
		Transaction transaction = null;
		boolean classValidated = false;
		
		try {
			//Begin a new transaction
			transaction = ebeanServer.beginTransaction();
			transaction.setLogLevel(LogLevel.NONE);
			
			while (iterator.hasNext()) {
				Object nextObj = iterator.next();
				
				if (!classValidated) {
					//Lazily validate class
					Validate.isTrue(beanClasses.contains(nextObj.getClass()), "object.getClass() must be a registered class");
				}
				
				//Save the object
				ebeanServer.save(nextObj);
			}
			
			//Commit transaction
			transaction.commit();
		} finally {
			if (transaction != null) {
				//End transaction
				transaction.end();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> query(String key, Object value, Object cookie, String orderBy, int limit) {
		Validate.notNull(cookie, "cookie cannot be null");
		Validate.isTrue(cookie instanceof Class<?>, "cookie must be an instance of " + Class.class.getCanonicalName());
		
		Class<?> clazz = (Class<?>) cookie;
		
		Transaction transaction = null;
		List<Object> result = new LinkedList<>();
		
		try {
			//Create a new transaction and disable logging
			transaction = ebeanServer.beginTransaction();
			transaction.setLogLevel(LogLevel.NONE);
			
			//Find the class
			Query<?> query = ebeanServer.find(clazz);
			
			if (key != null) {
				//Apply key and value where and fetch
				query = query.where().eq(key, value).query();
			}
			if (orderBy != null) {
				query = query.orderBy(orderBy);
			}
			if (limit != NO_LIMIT) {
				query = query.setMaxRows(limit);
			}
			
			//Just query all
			result = (List<Object>) query.findList();
			
			transaction.commit();
		} finally {
			if (transaction != null) {
				//End the transaction
				transaction.end();
			}
		}
		
		return result;
	}
	
	@Override
	public List<Object> query(String key, Object value, String orderBy, int limit) {
		return query(key, value, orderBy, limit);
	}
	
	@Override
	public Object queryUnique(String key, Object value) {
		return queryUnique(key, value, null);
	}
	
	@Override
	public Object queryUnique(String key, Object value, Object cookie) {
		List<?> result = query(key, value, cookie, null, 1);
		return !result.isEmpty() ? result.get(0) : null;
	}
	
	@Override
	public int delete(Object object) {
		Transaction transaction = null;
		
		try {
			transaction = ebeanServer.beginTransaction();
			transaction.setLogLevel(LogLevel.NONE);
			
			ebeanServer.delete(object);
			
			transaction.commit();
		} finally {
			if (transaction != null) {
				transaction.end();
			}
		}
		
		return 1;
	}
	
	private void setupDatabase(Properties props) throws Exception {
		try {
			// Disable ugly database logging
			disableDatabaseLogging();
			
			// Prepare the database
			prepareDatabase(props);
			
			// Load the database
			loadDatabase();
			
			// Install and create tables as needed
			installDatabase();
		} finally {
			//Enable logging again
			enableDatabaseLogging();
		}
	}
	
	private void prepareDatabase(Properties props) {
		disableDatabaseLogging();
		
		//Create a configuration for the data source
		DataSourceConfig dsc = new DataSourceConfig();
		dsc.setDriver(props.getProperty("driver", DEFAULT_DRIVER));
		dsc.setUrl(props.getProperty("url"));
		dsc.setUsername(props.getProperty("username"));
		dsc.setPassword(props.getProperty("password"));
		dsc.setIsolationLevel(TransactionIsolation.getLevel(props.getProperty("isolation")));
		
		//Create a general server configuration
		ServerConfig sc = new ServerConfig();
		sc.setDefaultServer(false);
		sc.setRegister(false);
		sc.setName(dsc.getUrl().replaceAll("[^a-zA-Z0-9]", ""));
		sc.setClasses(beanClasses);
		
		if (dsc.getDriver().equals(SQLITE_DRIVER)) {
			//Seems like we're using SQLite
			usingSQLite = true;
			
			sc.setDatabasePlatform(new SQLitePlatform());
			sc.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
		}
		
		//Associate DataSourceConfig with our ServerConfig
		sc.setDataSourceConfig(dsc);
		
		//Save the ServerConfig
		serverConfig = sc;
	}
	
	private void loadDatabase() throws Exception {
		// Declare a few local variables for later use
		ClassLoader currentClassLoader = null;
		Field cacheField = null;
		boolean cacheValue = true;
		
		try {
			// Store the current ClassLoader, so it can be reverted later
			currentClassLoader = Thread.currentThread().getContextClassLoader();
			
			// Set the ClassLoader to Plugin ClassLoader
			Thread.currentThread().setContextClassLoader(pluginClassLoader);
			
			// Get a reference to the private static "defaultUseCaches"-field in URLConnection
			cacheField = URLConnection.class.getDeclaredField("defaultUseCaches");
			
			// Make it accessible, store the default value and set it to false
			cacheField.setAccessible(true);
			cacheValue = cacheField.getBoolean(null);
			cacheField.setBoolean(null, false);
			
			// Setup Ebean based on the configuration
			ebeanServer = EbeanServerFactory.create(serverConfig);
		} finally {
			// Revert the ClassLoader back to its original value
			if (currentClassLoader != null) {
				Thread.currentThread().setContextClassLoader(currentClassLoader);
			}
			
			// Revert the "defaultUseCaches"-field in URLConnection back to its original value
			try {
				if (cacheField != null) {
					cacheField.setBoolean(null, cacheValue);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to revert the \"defaultUseCaches\"-field back to its original value, URLConnection-caching remains disabled.", e);
			}
		}
	}
	
	private void installDatabase() throws Exception {
		// Check if the database already (partially) exists
		boolean databaseExists = false;
		for (int i = 0; i < beanClasses.size(); i++) {
			try {
				// Do a simple query which only throws an exception if the table does not exist
				ebeanServer.find(beanClasses.get(i)).findRowCount();
				
				// Query passed without throwing an exception, a database
				// therefore already exists
				databaseExists = true;
				break;
			} catch (Exception ex) {
				// Do nothing
			}
		}
		
		// Check if the database has to be created
		if (databaseExists) {
			return;
		}
		
		// Create a DDL generator
		SpiEbeanServer serv = (SpiEbeanServer) ebeanServer;
		DdlGenerator gen = serv.getDdlGenerator();
		
		// Generate a DropDDL-script
		gen.runScript(true, gen.generateDropDdl());
		
		// If SQLite is being used, the database has to reloaded to release all
		// resources
		if (usingSQLite) {
			loadDatabase();
		}
		
		// Generate a CreateDDL-script
		if (usingSQLite) {
			// If SQLite is being used, the CreateDLL-script has to be validated
			// and potentially fixed to be valid
			gen.runScript(false, validateCreateDDLSqlite(gen.generateCreateDdl()));
		} else {
			gen.runScript(false, gen.generateCreateDdl());
		}
	}
	
	private String validateCreateDDLSqlite(String oldScript) {
		try {
			// Create a BufferedReader out of the potentially invalid script
			BufferedReader scriptReader = new BufferedReader(new StringReader(oldScript));

			// Create an array to store all the lines
			List<String> scriptLines = new ArrayList<String>();

			// Create some additional variables for keeping track of tables
			HashMap<String, Integer> foundTables = new HashMap<String, Integer>();
			String currentTable = null;
			int tableOffset = 0;

			// Loop through all lines
			String currentLine;
			while ((currentLine = scriptReader.readLine()) != null) {
				// Trim the current line to remove trailing spaces
				currentLine = currentLine.trim();

				// Add the current line to the rest of the lines
				scriptLines.add(currentLine.trim());

				// Check if the current line is of any use
				if (currentLine.startsWith("create table")) {
					// Found a table, so get its name and remember the line it
					// has been encountered on
					currentTable = currentLine.split(" ", 4)[2];
					foundTables.put(currentLine.split(" ", 3)[2], scriptLines.size() - 1);
				} else if (currentLine.startsWith(";") && currentTable != null && !currentTable.isEmpty()) {
					// Found the end of a table definition, so update the entry
					int index = scriptLines.size() - 1;
					foundTables.put(currentTable, index);

					// Remove the last ")" from the previous line
					String previousLine = scriptLines.get(index - 1);
					previousLine = previousLine.substring(0, previousLine.length() - 1);
					scriptLines.set(index - 1, previousLine);

					// Change ";" to ");" on the current line
					scriptLines.set(index, ");");

					// Reset the table-tracker
					currentTable = null;
				} else if (currentLine.startsWith("alter table")) {
					// Found a potentially unsupported action
					String[] alterTableLine = currentLine.split(" ", 4);

					if (alterTableLine[3].startsWith("add constraint")) {
						// Found an unsupported action: ALTER TABLE using ADD
						// CONSTRAINT
						String[] addConstraintLine = alterTableLine[3].split(" ", 4);

						// Check if this line can be fixed somehow
						if (addConstraintLine[3].startsWith("foreign key")) {
							// Calculate the index of last line of the current
							// table
							int tableLastLine = foundTables.get(alterTableLine[2]) + tableOffset;

							// Add a "," to the previous line
							scriptLines.set(tableLastLine - 1, scriptLines.get(tableLastLine - 1) + ",");

							// Add the constraint as a new line - Remove the ";"
							// on the end
							String constraintLine = String.format("%s %s %s", addConstraintLine[1], addConstraintLine[2], addConstraintLine[3]);
							scriptLines.add(tableLastLine, constraintLine.substring(0, constraintLine.length() - 1));

							// Remove this line and raise the table offset
							// because a line has been inserted
							scriptLines.remove(scriptLines.size() - 1);
							tableOffset++;
						} else {
							// Exception: This line cannot be fixed but is known
							// the be unsupported by SQLite
							throw new RuntimeException("Unsupported action encountered: ALTER TABLE using ADD CONSTRAINT with "
									+ addConstraintLine[3]);
						}
					}
				}
			}

			// Turn all the lines back into a single string
			StringBuilder newScriptBuilder = new StringBuilder();
			for (String line : scriptLines) {
				newScriptBuilder.append(line);
				newScriptBuilder.append("\n");
			}

			// Return the fixed script
			return newScriptBuilder.toString();
		} catch (Exception ex) {
			// Exception: Failed to fix the DDL or something just went plain
			// wrong
			throw new RuntimeException("Failed to validate the CreateDDL-script for SQLite", ex);
		}
	}
	
	private void disableDatabaseLogging() {
		// Retrieve the level of the root logger
		loggerLevel = Logger.getLogger("").getLevel();
		// Set the level of the root logger to OFF
		Logger.getLogger("").setLevel(Level.OFF);
	}

	private void enableDatabaseLogging() {
		// Don't set the level if it is null
		if (loggerLevel == null) {
			return;
		}
		
		// Set the level of the root logger back to the original value
		Logger.getLogger("").setLevel(loggerLevel);
	}
	
}
