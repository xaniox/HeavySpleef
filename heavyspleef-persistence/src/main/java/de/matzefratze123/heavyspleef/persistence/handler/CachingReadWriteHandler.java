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
package de.matzefratze123.heavyspleef.persistence.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Statistic;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.persistence.ReadWriteHandler;
import de.matzefratze123.heavyspleef.core.uuid.GameProfile;
import de.matzefratze123.heavyspleef.core.uuid.UUIDManager;
import de.matzefratze123.heavyspleef.persistence.schematic.FloorAccessor;
import de.matzefratze123.heavyspleef.persistence.schematic.SchematicContext;
import de.matzefratze123.heavyspleef.persistence.sql.DatabaseUpgrader;
import de.matzefratze123.heavyspleef.persistence.sql.SQLDatabaseContext;
import de.matzefratze123.heavyspleef.persistence.sql.SQLDatabaseContext.SQLImplementation;
import de.matzefratze123.heavyspleef.persistence.sql.SQLQueryOptionsBuilder;
import de.matzefratze123.heavyspleef.persistence.sql.SQLQueryOptionsBuilder.ExpressionList;
import de.matzefratze123.heavyspleef.persistence.sql.StatisticAccessor;
import de.matzefratze123.heavyspleef.persistence.xml.GameAccessor;
import de.matzefratze123.heavyspleef.persistence.xml.XMLContext;

public class CachingReadWriteHandler implements ReadWriteHandler {
		
	private static final long STATISTIC_CACHE_EXPIRE = 10 * 60 * 1000L;
	private static final FilenameFilter FLOOR_SCHEMATIC_FILTER = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".floor");
		}
	};
	private static final FilenameFilter XML_GAME_FILTER = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".xml");
		}
	};
	
	private final File xmlFolder;
	private final File schematicFolder;
	
	private final Logger logger;
	private UUIDManager uuidManager;
	private final SAXReader saxReader = new SAXReader();
	private final OutputFormat xmlOutputFormat = OutputFormat.createPrettyPrint();
	
	private SQLDatabaseContext sqlContext;
	private SchematicContext schematicContext;
	private XMLContext xmlContext;
	
	private ReentrantLock rankLock = new ReentrantLock();
	private ReentrantLock renameLock = new ReentrantLock(true);
	
	private LoadingCache<UUID, Statistic> statisticCache;
	private final CacheLoader<UUID, Statistic> statisticCacheLoader = new CacheLoader<UUID, Statistic>() {
		
		@Override
		public Statistic load(UUID uuid) throws Exception {
			if (sqlContext == null) {
				throw new IllegalStateException("SQL database has not been initialized");
			}
			
			SQLQueryOptionsBuilder optionsBuilder = SQLQueryOptionsBuilder.newBuilder()
				.limit(1)
				.where()
					.eq(StatisticAccessor.ColumnContract.UUID, uuid)
					.back();
			
			List<Statistic> statisticResult = sqlContext.readSql(Statistic.class, optionsBuilder);
			return !statisticResult.isEmpty() ? statisticResult.get(0) : new Statistic(uuid);
		}
	};
	
	public CachingReadWriteHandler(HeavySpleef heavySpleef, Properties properties, Map<UUID, Statistic> initStatistics, UUIDManager uuidManager)
			throws IOException, Exception {
		this.logger = heavySpleef.getLogger();
		
		this.uuidManager = uuidManager != null ? uuidManager : new UUIDManager();
		this.xmlFolder = (File) properties.get("xml.dir");
		this.schematicFolder = (File) properties.get("schematic.dir");
		
		GameAccessor gameAccessor = new GameAccessor(heavySpleef);
		xmlContext = new XMLContext(gameAccessor);
		
		FloorAccessor floorAccessor = new FloorAccessor();
		schematicContext = new SchematicContext(floorAccessor);
		
		boolean statisticsEnabled = (boolean) properties.get("statistic.enabled");
		if (statisticsEnabled) {
			StatisticAccessor statisticAccessor = new StatisticAccessor();
			DatabaseUpgrader upgrader = new HeavySpleefDatabaseUpgrader();
			sqlContext = new SQLDatabaseContext(properties, upgrader, statisticAccessor);
			
			int maxCacheSize = (int) properties.get("statistic.max_cache_size");
			
			//Create a cache for fast data access
			statisticCache = CacheBuilder.newBuilder()
					.expireAfterAccess(STATISTIC_CACHE_EXPIRE, TimeUnit.MILLISECONDS)
					.maximumSize(maxCacheSize)
					.removalListener(new RemovalListener<UUID, Statistic>() {

						@Override
						public void onRemoval(RemovalNotification<UUID, Statistic> notification) {
							//Save the statistic on remove
							Statistic statistic = notification.getValue();
							
							if (!statistic.isEmpty()) {
								try {
									saveStatistic(statistic);
								} catch (SQLException e) {
									logger.log(Level.SEVERE, "Could not save statistic for player with uuid " + statistic.getUniqueIdentifier() + ": ", e);
								}
							}
						}
					})
					.build(statisticCacheLoader);
		}
		
		if (initStatistics != null && !initStatistics.isEmpty()) {
			for (Entry<UUID, Statistic> entry : initStatistics.entrySet()) {
				statisticCache.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void saveGames(Iterable<Game> iterable) throws IOException {
		for (Game game : iterable) {
			saveGame(game);
		}
	}

	@Override
	public void saveGame(Game game) throws IOException {
		File gameFile = new File(xmlFolder, game.getName() + ".xml");
		if (!gameFile.exists()) {
			gameFile.createNewFile();
		}
		
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("game");
		
		xmlContext.write(game, rootElement);
		
		File gameSchematicFolder = new File(schematicFolder, game.getName());
		if (!gameSchematicFolder.exists()) {
			gameSchematicFolder.mkdir();
		}
		
		XMLWriter writer = null;
		
		try {
			FileOutputStream out = new FileOutputStream(gameFile);
			
			writer = new XMLWriter(out, xmlOutputFormat);
			writer.write(document);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		for (Floor floor : game.getFloors()) {	
			File floorFile = new File(gameSchematicFolder, getFloorFileName(floor));
			if (!floorFile.exists()) {
				floorFile.createNewFile();
			}
			
			schematicContext.write(floorFile, floor);
		}
	}
	
	private String getFloorFileName(Floor floor) {
		return "r." + floor.getName() + ".floor";
	}

	@Override
	public Game getGame(String name) throws IOException, DocumentException {
		File gameFile = new File(xmlFolder, name + ".xml");
		if (!gameFile.exists()) {
			return null;
		}
		
		return getGame(gameFile);
	}
	
	private Game getGame(File file) throws IOException, DocumentException {
		Document document = saxReader.read(file);
		
		if (!document.hasContent()) {
			return null;
		}
		
		Element rootElement = document.getRootElement();
		
		Game game = xmlContext.read(rootElement, Game.class);
		
		File gameFloorFolder = new File(schematicFolder, game.getName());
		if (gameFloorFolder.exists()) {
			for (File floorSchematicFile : gameFloorFolder.listFiles(FLOOR_SCHEMATIC_FILTER)) {
				Floor floor = schematicContext.read(floorSchematicFile, Floor.class);
				
				game.addFloor(floor);
			}
		}
		
		return game;
	}
	
	@Override
	public List<Game> getGames() throws IOException, DocumentException {
		List<Game> result = Lists.newArrayList();
		
		for (File gameFile : xmlFolder.listFiles(XML_GAME_FILTER)) {
			Game game = getGame(gameFile);
			if (game == null) {
				return null;
			}
			
			result.add(game);
		}
		
		return result;
	}
	
	@Override
	public void renameGame(Game game, String from, String to) throws IOException {
		renameLock.lock();
		
		try {
			File xmlFile = new File(xmlFolder, from + ".xml");
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
			
			File gameSchematicFolder = new File(schematicFolder, from);
			File newGameSchematicFolder = new File(schematicFolder, to);
			
			if (gameSchematicFolder.exists()) {
				gameSchematicFolder.renameTo(newGameSchematicFolder);
			}
			
			saveGame(game);
		} finally {
			renameLock.unlock();
		}
	}
	
	@Override
	public void deleteGame(Game game) {
		File gameFile = new File(xmlFolder, game.getName() + ".xml");
		if (gameFile.exists()) {
			gameFile.delete();
		}
		
		File floorDir = new File(schematicFolder, game.getName());
		for (File schematicFile : floorDir.listFiles(FLOOR_SCHEMATIC_FILTER)) {
			if (schematicFile.isFile()) {
				schematicFile.delete();
			}
		}
		
		if (floorDir.listFiles().length == 0) {
			floorDir.delete();
		}
	}

	@Override
	public void saveStatistics(Iterable<Statistic> iterable) throws SQLException {
		validateSqlDatabaseSetup();
		
		for (Statistic statistic : iterable) {
			saveStatistic(statistic);
		}
	}

	@Override
	public void saveStatistic(Statistic statistic) throws SQLException {
		validateSqlDatabaseSetup();
		
		sqlContext.writeObject(statistic);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Statistic getStatistic(String playerName) throws Exception {
		GameProfile profile;
		
		try {
			profile = uuidManager.getProfile(playerName);
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, "Could not retrieve player uuid from mojang api, using OfflinePlayer#getUniqueId()", e);
			OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
			profile = new GameProfile(player.getUniqueId(), player.getName());
		}
		
		Statistic statistic = getStatistic(profile.getUniqueIdentifier());
		statistic.setLastName(playerName);
		return statistic;
	}
	
	@Override
	public Statistic getStatistic(UUID uuid) throws Exception {
		validateSqlDatabaseSetup();
		
		Statistic statistic = statisticCache.get(uuid);
		return statistic;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Integer getStatisticRank(String playerName) throws Exception {
		GameProfile profile;
		
		try {
			profile = uuidManager.getProfile(playerName);
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, "Could not receive player uuid from mojang api, using OfflinePlayer#getUniqueId()", e);
			OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
			profile = new GameProfile(player.getUniqueId(), player.getName());
		}
		
		return getStatisticRank(profile.getUniqueIdentifier());
	}

	@Override
	public Integer getStatisticRank(UUID uuid) throws Exception {
		validateSqlDatabaseSetup();
		
		rankLock.lock();
		Connection connection = null;
		
		int resultRank = 0;
		
		try {
			connection = sqlContext.getConnectionFromPool();
			
			final String uuidColumn = StatisticAccessor.ColumnContract.UUID;
			final String ratingColumn = StatisticAccessor.ColumnContract.RATING;
			final String tableName = StatisticAccessor.ColumnContract.TABLE_NAME;
			final String rankColumn = "rank";
			
			PreparedStatement rankStatement;
			SQLImplementation implementation = sqlContext.getImplementationType();
			
			if (implementation == SQLImplementation.MYSQL) {
				rankStatement = connection.prepareStatement("SELECT " + rankColumn + " FROM "
						+ "(SELECT @rn:=@rn+1 AS " + rankColumn + ", " + uuidColumn + " FROM "
								+ tableName + " ORDER BY " + ratingColumn + " DESC"
						+ ") AS t1, "
						+ "(SELECT @rn:=0) t2 "
							+ "WHERE " + uuidColumn + " = ?");
			} else if (implementation == SQLImplementation.SQLITE) {
				rankStatement = connection.prepareStatement("SELECT "
						+ "(SELECT COUNT() + 1 FROM"
							+ "(SELECT DISTINCT " + ratingColumn + " FROM "
							+ tableName + " AS t WHERE " + ratingColumn + " > " + tableName + "." + ratingColumn + ")"
						+ ") "
						+ "AS " + rankColumn + " "
						+ "FROM " + tableName + " "
						+ "WHERE " + uuidColumn + "=?");
			} else {
				throw new IllegalStateException("Unknown sql implementation " + (implementation == null ? null : implementation.name()));
			}
			
			rankStatement.setString(1, uuid.toString());
			ResultSet result = rankStatement.executeQuery();
			
			if (result.next()) {
				resultRank = result.getInt(rankColumn);
			}
		} finally {
			rankLock.unlock();
			
			if (connection != null) {
				connection.close();
			}
		}
		
		return resultRank;
	}
	
	@Override
	public Map<String, Statistic> getStatistics(String[] players) throws Exception {
		validateSqlDatabaseSetup();
		
		List<GameProfile> profiles = uuidManager.getProfiles(players);
		
		if (sqlContext == null) {
			throw new IllegalStateException("SQL database has not been initialized");
		}
		
		SQLQueryOptionsBuilder optionsBuilder = SQLQueryOptionsBuilder.newBuilder();
		ExpressionList where = optionsBuilder.where();
		
		for (int i = 0; i < profiles.size(); i++) {
			GameProfile profile = profiles.get(i);
			where.eq(StatisticAccessor.ColumnContract.UUID, profile.getUniqueIdentifier());
			
			if (i + 1 < profiles.size()) {
				where.or();
			}
		}
		
		List<Statistic> statisticResult = sqlContext.readSql(Statistic.class, optionsBuilder);
		Map<String, Statistic> statisticsMap = Maps.newHashMap();
		
		for (GameProfile profile : profiles) {
			Statistic found = null;
			
			for (Statistic statistic : statisticResult) {
				if (profile.getUniqueIdentifier().equals(statistic.getUniqueIdentifier())) {
					found = statistic;
				}
			}
			
			if (found == null) {
				found = new Statistic(profile.getUniqueIdentifier());
			}
			
			found.setLastName(profile.getName());
			
			String name = null;
			for (String playerName : players) {
				if (playerName.equalsIgnoreCase(profile.getName())) {
					name = playerName;
				}
			}
			
			if (name == null) {
				throw new IllegalStateException("Could not find name for " + profile.getName() + ": " + profile.getUniqueIdentifier());
			}
			
			statisticsMap.put(name, found);
			statisticCache.put(profile.getUniqueIdentifier(), found);
		}
		
		return statisticsMap;
	}

	@Override
	public Map<String, Statistic> getTopStatistics(int offset, int limit) throws SQLException, ExecutionException {
		validateSqlDatabaseSetup();
		
		String limitStr = offset == 0 ? String.valueOf(limit) : offset + "," + limit;
		
		SQLQueryOptionsBuilder optionsBuilder = SQLQueryOptionsBuilder.newBuilder()
				.limit(limitStr)
				.sortBy(StatisticAccessor.ColumnContract.RATING + " DESC");
		List<Statistic> result = sqlContext.readSql(Statistic.class, optionsBuilder);
		
		Map<String, Statistic> statisticMapping = Maps.newLinkedHashMap();
		for (Statistic statistic : result) {
			UUID uuid = statistic.getUniqueIdentifier();
			GameProfile profile;
			
			profile = uuidManager.getProfile(uuid);
			String name = profile.getName();
			if (name == null) {
				name = statistic.getLastName();
			}
			
			statisticMapping.put(name, statistic);
		}
		
		return statisticMapping;
	}
	
	@Override
	public void clearCache() {
		if (statisticCache != null) {
			statisticCache.asMap().clear();
		}
	}
	
	@Override
	public void forceCacheSave() throws SQLException {
		Collection<Statistic> cachedStatistics = statisticCache.asMap().values();
		saveStatistics(cachedStatistics);
	}
	
	@Override
	public void shutdownGracefully() {
		Collection<Statistic> statistics = statisticCache.asMap().values();
		
		try {
			saveStatistics(statistics);
		} catch (SQLException e) {
			throw new RuntimeException("Could not save cached statistics on reload: ", e);
		}
		
		release();
	}
	
	@Override
	public void release() {
		if (sqlContext != null) {
			sqlContext.release();
		}
		
		uuidManager = null;
		statisticCache = null;
	}
	
	private void validateSqlDatabaseSetup() {
		if (sqlContext == null) {
			throw new IllegalStateException("No statistic-database has been setup");
		}
	}

	public Map<UUID, Statistic> getCachedStatistics() {
		return ImmutableMap.copyOf(statisticCache.asMap());
	}

	public UUIDManager getUUIDManager() {
		return uuidManager;
	}

}