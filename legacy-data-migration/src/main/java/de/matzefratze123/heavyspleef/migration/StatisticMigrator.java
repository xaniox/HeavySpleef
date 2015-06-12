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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutionException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import org.bukkit.Bukkit;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.uuid.GameProfile;
import de.matzefratze123.heavyspleef.core.uuid.UUIDManager;

public class StatisticMigrator implements Migrator<Connection, Connection> {

	private static final String TABLE_NAME = "heavyspleef_statistics";
	private static final String TEMP_TABLE_NAME = "heavyspleef_statistics_temp";
	private static final int RECORD_BUFFER_SIZE = 500;
	private static final int PROFILES_PER_REQUEST = 100;
	private static final int MAXIMUM_REQUESTS_PER_MINUTE = 600;
	private static final String CREATE_TABLE_SQL = "CREATE TABLE %s ("
			+ "id INTEGER NOT NULL PRIMARY KEY %s, "
			+ "uuid CHAR(36) UNIQUE, "
			+ "wins INTEGER, "
			+ "losses INTEGER, "
			+ "knockouts INTEGER, "
			+ "games_played INTEGER, "
			+ "blocks_broken INTEGER, "
			+ "time_played BIGINT, "
			+ "rating DOUBLE)";
	
	private final UUIDManager uuidManager = new UUIDManager();
	private final String db;
	private long watchdogTimeoutTime;
	private boolean watchdogRestart;
	private Method watchdogDoStartMethod;
	private @Getter int countMigrated;
	
	public StatisticMigrator(String db) {
		this.db = db;
	}
	
	@Override
	public void migrate(Connection inputSource, Connection outputSource, Object cookie) throws MigrationException {
		boolean sameConnection = inputSource == outputSource;
		String currentTableName = sameConnection ? TEMP_TABLE_NAME : TABLE_NAME;
		
		//Create the table
		try (Statement createStatement = outputSource.createStatement()) { 
			createStatement.executeUpdate(String.format(CREATE_TABLE_SQL, currentTableName, db.equalsIgnoreCase("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT"));
		} catch (SQLException e) {
			throw new MigrationException(e);
		}
		
		final String sizeSql = "SELECT count(*) AS count FROM " + TABLE_NAME;
		int size;
		
		try (Statement sizeStatement = inputSource.createStatement();
			ResultSet sizeResult = sizeStatement.executeQuery(sizeSql)) {
			
			sizeResult.next();
			size = sizeResult.getInt("count");
		} catch (SQLException e) {
			throw new MigrationException(e);
		}
		
		int requests = (int) Math.ceil((double)size / RECORD_BUFFER_SIZE);
		int requestsMade = 0;
		
		boolean watchdogThreadDeactivated = false;
		
		for (int i = 0; i < requests; i++) {
			int offset = i * RECORD_BUFFER_SIZE;
			int limit = i + 1 < requests ? RECORD_BUFFER_SIZE : size - ((requests - 1) * RECORD_BUFFER_SIZE);
			
			final String selectSql = "SELECT * FROM " + TABLE_NAME + " LIMIT " + offset + "," + limit;
			String[] names = new String[limit];
			List<LegacyStatisticProfile> profiles = Lists.newLinkedList();
			
			try (Statement selectStatement = inputSource.createStatement();
					ResultSet result = selectStatement.executeQuery(selectSql)) {
				int index = 0;
				
				while (result.next()) {
					String name = result.getString("owner");
					int wins = result.getInt("wins");
					int losses = result.getInt("loses");
					int knockouts = result.getInt("knockouts");
					int gamesPlayed = result.getInt("games");
					
					LegacyStatisticProfile profile = new LegacyStatisticProfile(name, wins, losses, knockouts, gamesPlayed);
					profiles.add(profile);
					names[index++] = name;
				}
			} catch (SQLException e) {
				throw new MigrationException(e);
			}
			
			List<GameProfile> gameProfiles;
			
			try {
				gameProfiles = uuidManager.getProfiles(names);
			} catch (ExecutionException e) {
				throw new MigrationException(e);
			}
			
			final String insertSql = "INSERT INTO " + currentTableName + " (uuid, wins, losses, knockouts, games_played, blocks_broken, time_played, rating)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			PreparedStatement insertStatement = null;
			
			try {
				outputSource.setAutoCommit(false);
				insertStatement = outputSource.prepareStatement(insertSql);
				
				for (LegacyStatisticProfile profile : profiles) {
					GameProfile foundGameProfile = null;
					for (GameProfile gameProfile : gameProfiles) {
						if (gameProfile.getName().equalsIgnoreCase(profile.getOwner())) {
							foundGameProfile = gameProfile;
							break;
						}
					}
					
					insertStatement.setString(1, foundGameProfile.getUniqueIdentifier().toString());
					insertStatement.setInt(2, profile.getWins());
					insertStatement.setInt(3, profile.getLosses());
					insertStatement.setInt(4, profile.getKnockouts());
					insertStatement.setInt(5, profile.getGamesPlayed());
					insertStatement.setInt(6, 0);
					insertStatement.setInt(7, 0);
					insertStatement.setDouble(8, 1000D);
					insertStatement.addBatch();
				}
				
				insertStatement.executeBatch();
				outputSource.commit();
			} catch (SQLException e) {
				try {
					outputSource.rollback();
				} catch (SQLException e1) {}
				
				throw new MigrationException(e);
			} finally {
				try {
					if (insertStatement != null) {
						insertStatement.close();
					}
					
					outputSource.setAutoCommit(true);
				} catch (SQLException e) {}
			}
			
			requestsMade += RECORD_BUFFER_SIZE / PROFILES_PER_REQUEST;
			
			//Check if we are going to exceed the maximum 
			//amount of requests we can make to the mojang api
			if (!Bukkit.getOnlineMode() && requestsMade + RECORD_BUFFER_SIZE / PROFILES_PER_REQUEST > MAXIMUM_REQUESTS_PER_MINUTE) {
				requestsMade = 0;
				
				if (!watchdogThreadDeactivated) {
					try {
						Class<?> clazz = Class.forName("org.spigotmc.WatchdogThread");
						Field instanceField = clazz.getDeclaredField("instance");
						Field timeoutTimeField = clazz.getDeclaredField("timeoutTime");
						Field restartField = clazz.getDeclaredField("restart");
						Method doStopMethod = clazz.getMethod("doStop");
						watchdogDoStartMethod = clazz.getMethod("doStart", int.class, boolean.class);
								
						instanceField.setAccessible(true);
						timeoutTimeField.setAccessible(true);
						restartField.setAccessible(true);
						
						Object watchdogThread = instanceField.get(null);
						watchdogTimeoutTime = (long) timeoutTimeField.get(watchdogThread);
						watchdogRestart = (boolean) restartField.get(watchdogThread);
						
						doStopMethod.invoke(null);
						watchdogThreadDeactivated = true;
					} catch (ClassNotFoundException e) {
						//Do nothing when the class does not exist
						//as it does not seem like we're running
						//on Spigot
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException
							| InvocationTargetException e) {
						//TODO: Handle
					}
				}
				
				try {
					//Waiting one minute before requesting again
					Thread.sleep(1000L * 60L);
				} catch (InterruptedException e) {
					throw new MigrationException(e);
				}
			}
		}
		
		if (sameConnection) {
			//Drop the old table and rename the temp table
			try (Statement statement = inputSource.createStatement()) {
				statement.addBatch("DROP TABLE " + TABLE_NAME);
				statement.addBatch("ALTER TABLE " + TEMP_TABLE_NAME
						+ " RENAME TO " + TABLE_NAME);
				
				statement.executeBatch();
			} catch (SQLException e) {
				throw new MigrationException(e);
			}
		}
		
		if (watchdogThreadDeactivated) {
			//Re-activate the watchdog thread
			try {
				watchdogDoStartMethod.invoke(null, watchdogTimeoutTime, watchdogRestart);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				//TODO: Handle
			}
		}
		
		countMigrated = size;
	}
	
	@Data
	@AllArgsConstructor
	private static class LegacyStatisticProfile {
		
		private String owner;
		private int wins;
		private int losses;
		private int knockouts;
		private int gamesPlayed;
		
	}

}