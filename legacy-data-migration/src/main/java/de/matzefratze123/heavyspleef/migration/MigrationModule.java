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
package de.matzefratze123.heavyspleef.migration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.module.LoadPolicy;
import de.matzefratze123.heavyspleef.core.module.LoadPolicy.Lifecycle;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;

@LoadPolicy(Lifecycle.PRE_LOAD)
public class MigrationModule extends SimpleModule {

	private static final int NO_CONFIG_VERSION = -1;
	private static final int LEGACY_CONFIG_VERSION = 3;
	private static final String LEGACY_SQLITE_DATABASE_PATH = "statistic/statistic.db";
	
	private final Charset charset = Charset.forName("UTF-8");
	private final FloorMigrator floorMigrator = new FloorMigrator();
	private GameMigrator gameMigrator;
	private StatisticMigrator statisticMigrator;
	private final FileVisitor<Path> FILE_DELETER = new SimpleFileVisitor<Path>() {
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			if (exc == null) {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			} else {
				throw exc;
			}
		};
		
	};
	
	public MigrationModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		HeavySpleef heavySpleef = getHeavySpleef();
		File dataFolder = heavySpleef.getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}
		
		File configFile = new File(dataFolder, "config.yml");
		if (!configFile.exists()) {
			//This is a completely new installation of HeavySpleef
			return;
		}
		
		Configuration configuration = YamlConfiguration.loadConfiguration(configFile);
		int configVersion = configuration.getInt("config-version", NO_CONFIG_VERSION);
		if (configVersion > LEGACY_CONFIG_VERSION || configVersion == NO_CONFIG_VERSION) {
			//This is not a legacy installation
			return;
		}
		
		getLogger().info("Detected old configurations and databases!");
		getLogger().info("Migrating data to a newer version for use by HeavySpleef!");
		
		File persistenceFolder = new File(dataFolder, "persistence");
		if (!persistenceFolder.exists()) {
			persistenceFolder.mkdir();
		}
		
		getLogger().info("Migrating statistic data...");
		boolean statisticMigrationSuccess = false;
		
		try {
			//Migrate all legacy data
			migrateStatisticData(dataFolder, configuration);
			statisticMigrationSuccess = true;
			int statisticsMigrated = statisticMigrator.getCountMigrated();
			
			getLogger().info("Successfully migrated " + statisticsMigrated + " statistics");
		} catch (MigrationException e) {
			getLogger().log(Level.SEVERE, "Could not migrate statistic data", e);
		}
		
		File legacyGameFolder = new File(dataFolder, "games");
		File legacyGameYmlFile = new File(legacyGameFolder, "games.yml");
		
		File gameFolder = new File(persistenceFolder, "games");
		if (!gameFolder.exists()) {
			gameFolder.mkdir();
		}
		
		File xmlFolder = new File(gameFolder, "xml");
		if (!xmlFolder.exists()) {
			xmlFolder.mkdir();
		}
		
		List<Game> games = Lists.newArrayList();
		
		getLogger().info("Migrating game data...");
		boolean gamesMigrationSuccess = false;
		if (legacyGameYmlFile.exists()) {
			gameMigrator = new GameMigrator(heavySpleef);
			
			try {
				Configuration legacyGameConfig = YamlConfiguration.loadConfiguration(legacyGameYmlFile);
				gameMigrator.migrate(legacyGameConfig, xmlFolder, games);
				gamesMigrationSuccess = true;
				
				int gamesMigrated = gameMigrator.getCountMigrated();
				getLogger().info("Successfully migrated " + gamesMigrated + " games");
			} catch (MigrationException e) {
				getLogger().log(Level.SEVERE, "Could not migrate games", e);
			}
		}
		
		File schematicFolder = new File(gameFolder, "schematic");
		if (!schematicFolder.exists()) {
			schematicFolder.mkdir();
		}
		
		if (gamesMigrationSuccess) {
			getLogger().info("Migrating floor data...");
			int count = 0;
			
			for (File legacyFloorFolder : legacyGameFolder.listFiles()) {
				if (!legacyFloorFolder.isDirectory()) {
					continue;
				}
				
				String gameName = legacyFloorFolder.getName();
				Game game = null;
				for (Game listGame : games) {
					if (listGame.getName().equalsIgnoreCase(gameName)) {
						game = listGame;
						break;
					}
				}
				
				File floorFolder = new File(schematicFolder, gameName);
				if (!floorFolder.exists()) {
					floorFolder.mkdir();
				}
				
				for (File legacyFloorFile : legacyFloorFolder.listFiles()) {
					String floorName = legacyFloorFile.getName();
					
					if (!floorName.toLowerCase().endsWith(".schematic")) {
						continue;
					}
					
					File floorFile = new File(floorFolder, "r.floor_" + floorName.substring(0, floorName.lastIndexOf('.')) + ".floor");
					
					try {
						if (!floorFile.exists()) {
							floorFile.createNewFile();
						}
						
						OutputStream out = new FileOutputStream(floorFile);
						floorMigrator.migrate(legacyFloorFile, out, game);
						++count;
					} catch (IOException e) {
						getLogger().log(Level.SEVERE, "Could not create floor file \"" + floorFile.getPath() + "\"", e);
					} catch (MigrationException e) {
						getLogger().log(Level.SEVERE, "Could not migrate legacy floor to file \"" + floorFile.getPath() + "\"", e);
					}
				}
			}
			
			getLogger().info("Migrated " + count + " floors");
		}
		
		//Delete the entire legacy games directory
		getLogger().info("Deleting old data...");
		Path legacyGameFolderPath = legacyGameFolder.toPath();
		Path languageFolderPath = dataFolder.toPath().resolve("language");
		Path statisticFolderPath = dataFolder.toPath().resolve("statistic");
		
		try {
			if (gamesMigrationSuccess) {
				Files.walkFileTree(legacyGameFolderPath, FILE_DELETER);
			}
			
			if (statisticMigrationSuccess) {
				Files.walkFileTree(statisticFolderPath, FILE_DELETER);
			}
			
			Files.walkFileTree(languageFolderPath, FILE_DELETER);
			Files.delete(configFile.toPath());
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not delete legacy folders and files", e);
		}
		
		getLogger().info("Migration successfully finished!");
		getLogger().info("Starting new version of HeavySpleef...");
	}
	
	private void migrateStatisticData(File dataFolder, Configuration legacyConfig) throws MigrationException {
		//Check statistic database details
		ConfigurationSection statisticSection = legacyConfig.getConfigurationSection("statistic");
		String dbType = statisticSection.getString("dbType");
		
		String inputUrl;
		String outputUrl;
		String user = null;
		String password = null;
		String driver;
		
		InputStream databaseConfigIn = getClass().getResourceAsStream("/database-config.yml");
		Reader reader = new InputStreamReader(databaseConfigIn, charset);
		
		Configuration databaseConfig = YamlConfiguration.loadConfiguration(reader);
		
		if (dbType.equalsIgnoreCase("mysql")) {
			//MySQL
			String host = statisticSection.getString("host", "localhost");
			int port = statisticSection.getInt("port", 3306);
			String databaseName = statisticSection.getString("databaseName");
			
			user = statisticSection.getString("user");
			password = statisticSection.getString("password");
			
			String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
			
			inputUrl = url;
			outputUrl = url;
			driver = "com.mysql.jdbc.Driver";
		} else {
			//SQLite
			String baseUrl = "jdbc:sqlite:" + dataFolder.getPath();
			
			inputUrl = baseUrl + "/" + LEGACY_SQLITE_DATABASE_PATH;
			outputUrl = databaseConfig.getString("persistence-connection.sql.url").replace("{basedir}", dataFolder.getPath());
			driver = "org.sqlite.JDBC";
		}
		
		Connection inputConnection = null;
		Connection outputConnection = null;
		
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new MigrationException(e);
		}
		
		try {
			inputConnection = DriverManager.getConnection(inputUrl, user, password);
			outputConnection = inputUrl.equals(outputUrl) ? inputConnection : DriverManager.getConnection(outputUrl, user, password);
			
			statisticMigrator = new StatisticMigrator(dbType);
			statisticMigrator.migrate(inputConnection, outputConnection, null);
		} catch (SQLException e) {
			throw new MigrationException(e);
		} finally {
			try {
				if (inputConnection != null) {
					inputConnection.close();
				}
				
				if (outputConnection != null) {
					outputConnection.close();
				}
			} catch (SQLException e) {}
		}
		
		if (dbType.equalsIgnoreCase("mysql")) {
			// Copy the database config to the data folder and 
			// change/transfer the connection details to mysql
			Map<String, String> variables = Maps.newHashMap();
			variables.put("driver", driver);
			variables.put("url", inputUrl);
			variables.put("authenticate", String.valueOf(user != null && !user.isEmpty()));
			variables.put("user", user);
			variables.put("password", password);
			
			try {
				InputStream templateIn = getClass().getResourceAsStream("/database-config-template.yml");
				Reader templateReader = new InputStreamReader(templateIn, charset);
				TemplatedDocument document = new TemplatedDocument(templateReader, variables);
				
				File outFile = new File(dataFolder, "database-config.yml");
				if (!outFile.exists()) {
					outFile.createNewFile();
				}
				
				OutputStream out = new FileOutputStream(outFile);
				Writer writer = new OutputStreamWriter(out, charset);
				
				document.writeDocument(writer);
			} catch (IOException e) {
				throw new MigrationException(e);
			}
		}
	}

	@Override
	public void reload() {}

	@Override
	public void disable() {}

}
