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
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.uuid.GameProfile;
import de.matzefratze123.heavyspleef.core.uuid.OriginalGameProfile;
import de.matzefratze123.heavyspleef.core.uuid.UUIDManager;

public class StatisticMigrator implements Migrator<Connection, Connection> {

	private static final String TABLE_NAME = "heavyspleef_statistics";
	private static final String TEMP_TABLE_NAME = "heavyspleef_statistics_temp";
	private static final int RECORD_BUFFER_SIZE = 1000;
	private static final double ESTIMATED_TIME_PER_STATISTIC = 0.25;
	private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS %s ("
			+ "id INTEGER NOT NULL PRIMARY KEY %s, "
			+ "uuid CHAR(36) UNIQUE, "
			+ "last_name CHAR(16), "
			+ "wins INTEGER, "
			+ "losses INTEGER, "
			+ "knockouts INTEGER, "
			+ "games_played INTEGER, "
			+ "blocks_broken INTEGER, "
			+ "time_played BIGINT, "
			+ "rating DOUBLE)";
	
	private final UUIDManager uuidManager = new UUIDManager();
	private final String db;
	private final Logger logger;
	private long watchdogTimeoutTime;
	private boolean watchdogRestart;
	private Method watchdogDoStartMethod;
	private int countMigrated;
	
	public StatisticMigrator(String db, Logger logger) {
		this.db = db;
		this.logger = logger;
	}
	
	public int getCountMigrated() {
		return countMigrated;
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
		
		int estimatedSeconds = (int) (ESTIMATED_TIME_PER_STATISTIC * size);
		int hours = (int) TimeUnit.SECONDS.toHours(estimatedSeconds);
		estimatedSeconds -= TimeUnit.HOURS.toSeconds(hours);
		int minutes = (int) TimeUnit.SECONDS.toMinutes(estimatedSeconds);
		
		logger.log(Level.INFO, "Estimated time for this statistic database upgrade: " + (hours != 0 ? hours + " hour(s) " : "") + minutes + " minute(s).");
		if (estimatedSeconds > 60L * 15L) {
			//Make sure the user has enough coffee to survive this...
			logger.log(Level.INFO, "Make sure you got a comfortable seat and enough coffee to survive this...");
		}
		
		int requests = (int) Math.ceil((double)size / RECORD_BUFFER_SIZE);
		
		boolean watchdogThreadDeactivated = false;
		List<UUID> uuidsMigrated = Lists.newArrayList();
		
		for (int i = 0; i < requests; i++) {
			int offset = i * RECORD_BUFFER_SIZE;
			int limit = i + 1 < requests ? RECORD_BUFFER_SIZE : size - ((requests - 1) * RECORD_BUFFER_SIZE);
			
			final String selectSql = "SELECT * FROM " + TABLE_NAME + " LIMIT " + offset + "," + limit;
			List<String> names = Lists.newArrayList();
			List<LegacyStatisticProfile> profiles = Lists.newLinkedList();
			
			try (Statement selectStatement = inputSource.createStatement();
					ResultSet result = selectStatement.executeQuery(selectSql)) {
				while (result.next()) {
					String name = result.getString("owner");
					int wins = result.getInt("wins");
					int losses = result.getInt("loses");
					int knockouts = result.getInt("knockouts");
					int gamesPlayed = result.getInt("games");
					
					if (name.contains(" ")) {
						//Some sort of an illegal name
						continue;
					}
					
					LegacyStatisticProfile profile = new LegacyStatisticProfile(name, wins, losses, knockouts, gamesPlayed);
					profiles.add(profile);
					names.add(name);
				}
			} catch (SQLException e) {
				throw new MigrationException(e);
			}
			
			List<GameProfile> gameProfiles;
			
			try {
				gameProfiles = uuidManager.getProfiles(names.toArray(new String[names.size()]), true, true);
			} catch (ExecutionException e) {
				throw new MigrationException(e);
			}
			
			final String insertSql = "INSERT INTO " + currentTableName + " (uuid, wins, losses, knockouts, games_played, blocks_broken, time_played, rating, last_name)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			
			PreparedStatement insertStatement = null;
			PreparedStatement mergeUpdateStatement = null;
			
			try {
				outputSource.setAutoCommit(false);
				insertStatement = outputSource.prepareStatement(insertSql);
				
				Map<GameProfile, LegacyStatisticProfile> mergingStatistics = Maps.newHashMap();
				
				for (LegacyStatisticProfile profile : profiles) {
					GameProfile foundGameProfile = null;
					for (GameProfile gameProfile : gameProfiles) {
						String profileName = gameProfile instanceof OriginalGameProfile ? ((OriginalGameProfile) gameProfile).getOriginalName()
								: gameProfile.getName();
						
						if (profileName.equalsIgnoreCase(profile.getOwner())) {
							foundGameProfile = gameProfile;
							break;
						}
					}
					
					if (foundGameProfile == null) {
						//No profile for this statistic set found, discard it
						continue;
					}
					
					UUID uuid = foundGameProfile.getUniqueIdentifier();
					
					if (uuidsMigrated.contains(uuid)) {
						mergingStatistics.put(foundGameProfile, profile);
					} else {
						uuidsMigrated.add(uuid);
						
						insertStatement.setString(1, uuid.toString());
						insertStatement.setInt(2, profile.getWins());
						insertStatement.setInt(3, profile.getLosses());
						insertStatement.setInt(4, profile.getKnockouts());
						insertStatement.setInt(5, profile.getGamesPlayed());
						insertStatement.setInt(6, 0);
						insertStatement.setInt(7, 0);
						insertStatement.setDouble(8, 1000D);
						insertStatement.setString(9, foundGameProfile.getName());
						insertStatement.addBatch();
					}
				}
				
				insertStatement.executeBatch();
				
				if (!mergingStatistics.isEmpty()) {
					String updateSql = "UPDATE " + currentTableName
							+ " SET wins = wins + ?, losses = losses + ?, knockouts = knockouts + ?, games_played = games_played + ? WHERE uuid = ?";  
					mergeUpdateStatement = outputSource.prepareStatement(updateSql);
					
					//Merge those statistics
					for (Entry<GameProfile, LegacyStatisticProfile> entry : mergingStatistics.entrySet()) {
						GameProfile gameProfile = entry.getKey();
						LegacyStatisticProfile statistic = entry.getValue();
						
						mergeUpdateStatement.setInt(1, statistic.getWins());
						mergeUpdateStatement.setInt(2, statistic.getLosses());
						mergeUpdateStatement.setInt(3, statistic.getKnockouts());
						mergeUpdateStatement.setInt(4, statistic.getGamesPlayed());
						mergeUpdateStatement.setString(5, gameProfile.getUniqueIdentifier().toString());
						mergeUpdateStatement.addBatch();
					}
					
					mergeUpdateStatement.executeBatch();
				}
				
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
					
					if (mergeUpdateStatement != null) {
						mergeUpdateStatement.close();
					}
					
					outputSource.setAutoCommit(true);
				} catch (SQLException e) {}
			}
			
			//Deactivate the Spigot watchdog thread, as it may cause the server to force stop
			//and interrupt the migration...
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
					logger.log(Level.SEVERE, "Failed to temporarily deactivate the Spigot Watchdog thread. Expecting Spigot to shut down the server in some seconds...");
					logger.log(Level.SEVERE, "Stacktrace: ", e);
				}
			}
			
			int percent = (int)((i + 1) * 100d / requests);
			logger.log(Level.INFO, percent + "%");
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
				watchdogDoStartMethod.invoke(null, (int)watchdogTimeoutTime, watchdogRestart);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.log(Level.SEVERE, "Failed to reactivate the Spigot Watchdog thread", e);
			}
		}
		
		countMigrated = size;
	}
	
	private static class LegacyStatisticProfile {
		
		private String owner;
		private int wins;
		private int losses;
		private int knockouts;
		private int gamesPlayed;
		
		public LegacyStatisticProfile(String owner, int wins, int losses, int knockouts, int gamesPlayed) {
			this.owner = owner;
			this.wins = wins;
			this.losses = losses;
			this.knockouts = knockouts;
			this.gamesPlayed = gamesPlayed;
		}

		public String getOwner() {
			return owner;
		}

		public int getWins() {
			return wins;
		}

		public int getLosses() {
			return losses;
		}

		public int getKnockouts() {
			return knockouts;
		}

		public int getGamesPlayed() {
			return gamesPlayed;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + gamesPlayed;
			result = prime * result + knockouts;
			result = prime * result + losses;
			result = prime * result + ((owner == null) ? 0 : owner.hashCode());
			result = prime * result + wins;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LegacyStatisticProfile other = (LegacyStatisticProfile) obj;
			if (gamesPlayed != other.gamesPlayed)
				return false;
			if (knockouts != other.knockouts)
				return false;
			if (losses != other.losses)
				return false;
			if (owner == null) {
				if (other.owner != null)
					return false;
			} else if (!owner.equals(other.owner))
				return false;
			if (wins != other.wins)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "LegacyStatisticProfile [owner=" + owner + ", wins=" + wins + ", losses=" + losses + ", knockouts=" + knockouts + ", gamesPlayed="
					+ gamesPlayed + "]";
		}
		
	}

}