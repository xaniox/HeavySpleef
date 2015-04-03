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
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

import com.google.common.collect.Lists;

import de.matzefratze123.guavacachecompat.CacheCompat;
import de.matzefratze123.guavacachecompat.CacheFactory;
import de.matzefratze123.guavacachecompat.CacheLoaderCompat;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Statistic;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.uuid.GameProfile;
import de.matzefratze123.heavyspleef.core.uuid.UUIDManager;
import de.matzefratze123.heavyspleef.persistence.schematic.FloorAccessor;
import de.matzefratze123.heavyspleef.persistence.schematic.SchematicContext;
import de.matzefratze123.heavyspleef.persistence.sql.SQLDatabaseContext;
import de.matzefratze123.heavyspleef.persistence.sql.SQLQueryOptionsBuilder;
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
	private final UUIDManager uuidManager = new UUIDManager();
	private final CacheFactory cacheFactory = new CacheFactory();
	private final SAXReader saxReader = new SAXReader();
	private final OutputFormat xmlOutputFormat = OutputFormat.createPrettyPrint();
	
	private SQLDatabaseContext sqlContext;
	private SchematicContext schematicContext;
	private XMLContext xmlContext;
	
	private CacheCompat<UUID, Statistic> statisticCache;
	private final CacheLoaderCompat<UUID, Statistic> statisticCacheLoader = new CacheLoaderCompat<UUID, Statistic>() {
		
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
			return !statisticResult.isEmpty() ? statisticResult.get(0) : null;
		}
	};
	
	public CachingReadWriteHandler(HeavySpleef heavySpleef, Properties properties) throws IOException, Exception {
		this.logger = heavySpleef.getLogger();
		
		this.xmlFolder = (File) properties.get("xml.dir");
		this.schematicFolder = (File) properties.get("schematic.dir");
		
		GameAccessor gameAccessor = new GameAccessor(heavySpleef);
		xmlContext = new XMLContext(gameAccessor);
		
		FloorAccessor floorAccessor = new FloorAccessor();
		schematicContext = new SchematicContext(floorAccessor);
		
		boolean statisticsEnabled = (boolean) properties.get("statistic.enabled");
		if (statisticsEnabled) {
			StatisticAccessor statisticAccessor = new StatisticAccessor();
			sqlContext = new SQLDatabaseContext(properties, statisticAccessor);
			
			//Create a cache for fast data access
			statisticCache = cacheFactory.newCacheBuilder()
					.expireAfterAccess(STATISTIC_CACHE_EXPIRE, TimeUnit.MILLISECONDS)
					.build(statisticCacheLoader);
		}
	}
	
	@Override
	public void saveGames(Iterable<Game> iterable) {
		for (Game game : iterable) {
			saveGame(game);
		}
	}

	@Override
	public void saveGame(Game game) {
		File gameFile = new File(xmlFolder, game.getName() + ".xml");
		if (!gameFile.exists()) {
			try {
				gameFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Could not create xml file for game \"" + game.getName() + "\"", e);
			}
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {}
			}
		}
		
		for (Floor floor : game.getFloors()) {	
			File floorFile = new File(gameSchematicFolder, getFloorFileName(floor));
			if (!floorFile.exists()) {
				try {
					floorFile.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("Could not create floor schematic file for game \"" + game.getName() + "\"", e);
				}
			}
			
			try {
				schematicContext.write(floorFile, floor);
			} catch (IOException e) {
				throw new RuntimeException("Could not write floor schematic file", e);
			}
		}
	}
	
	private String getFloorFileName(Floor floor) {
		return "r." + floor.getName() + ".floor";
	}

	@Override
	public Game getGame(String name) {
		File gameFile = new File(xmlFolder, name + ".xml");
		if (!gameFile.exists()) {
			return null;
		}
		
		return getGame(gameFile);
	}
	
	private Game getGame(File file) {
		Document document;
		try {
			document = saxReader.read(file);
		} catch (DocumentException e) {
			throw new RuntimeException("Could not parse xml game file " + file.getPath(), e);
		}
		
		if (!document.hasContent()) {
			return null;
		}
		
		Element rootElement = document.getRootElement();
		
		Game game = xmlContext.read(rootElement, Game.class);
		
		File gameFloorFolder = new File(schematicFolder, game.getName());
		if (gameFloorFolder.exists()) {
			for (File floorSchematicFile : gameFloorFolder.listFiles(FLOOR_SCHEMATIC_FILTER)) {
				Floor floor;
				
				try {
					floor = schematicContext.read(floorSchematicFile, Floor.class);
				} catch (IOException e) {
					throw new RuntimeException("Could not read floor schematic for game " + game.getName() + ": " + floorSchematicFile.getPath(), e);
				}
				
				game.addFloor(floor);
			}
		}
		
		return game;
	}
	
	@Override
	public List<Game> getGames() {
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
	public void saveStatistics(Iterable<Statistic> iterable) {
		validateSqlDatabaseSetup();
		
		for (Statistic statistic : iterable) {
			saveStatistic(statistic);
		}
	}

	@Override
	public void saveStatistic(Statistic statistic) {
		validateSqlDatabaseSetup();
		
		try {
			sqlContext.writeObject(statistic);
		} catch (SQLException e) {
			throw new RuntimeException("Could not save statistic for uuid {" + statistic.getUniqueIdentifier() + "}", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public Statistic getStatistic(String playerName) {
		GameProfile profile;
		
		try {
			profile = uuidManager.getProfile(playerName);
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, "Could not receive player uuid from mojang api, using OfflinePlayer#getUniqueId()", e);
			OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
			profile = new GameProfile(player.getUniqueId(), player.getName());
		}
		
		return getStatistic(profile.getUniqueIdentifier());
	}
	
	@Override
	public Statistic getStatistic(UUID uuid) {
		validateSqlDatabaseSetup();
		
		Statistic statistic;
		
		try {
			statistic = statisticCache.get(uuid);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
		
		return statistic;
	}

	@Override
	public List<Statistic> getTopStatistics(int offset, int limit) {
		validateSqlDatabaseSetup();
		
		String limitStr = offset == 0 ? String.valueOf(limit) : offset + "," + limit;
		
		SQLQueryOptionsBuilder optionsBuilder = SQLQueryOptionsBuilder.newBuilder()
				.limit(limitStr)
				.sortBy(StatisticAccessor.ColumnContract.RATING);
		List<Statistic> result;
		
		try {
			result = sqlContext.readSql(Statistic.class, optionsBuilder);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		Collections.sort(result);
		
		return Collections.unmodifiableList(result);
	}
	
	private void validateSqlDatabaseSetup() {
		if (sqlContext == null) {
			throw new IllegalStateException("No statistic-database has been setup");
		}
	}

}
