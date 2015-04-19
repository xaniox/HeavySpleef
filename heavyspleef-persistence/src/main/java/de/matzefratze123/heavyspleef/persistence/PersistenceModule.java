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

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Statistic;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DatabaseConfig;
import de.matzefratze123.heavyspleef.core.config.DatabaseConnection;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.uuid.UUIDManager;
import de.matzefratze123.heavyspleef.persistence.handler.CachingReadWriteHandler;
import de.matzefratze123.heavyspleef.persistence.handler.ForwardingAsyncReadWriteHandler;

public class PersistenceModule extends SimpleModule {

	private static final String XML_CONNECTION_IDENTIFIER = "xml";
	private static final String SCHEMATIC_CONNECTION_IDENTIFIER = "schematic";
	private static final String SQL_CONNECTION_IDENTIFIER = "sql";
	
	private final File defaultPersistenceDir;
	private final File defaultXmlDir;
	private final File defaultSchematicDir;
	
	private AsyncReadWriteHandler asyncHandler;
	private CachingReadWriteHandler cachingHandler;
	
	public PersistenceModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
		
		defaultPersistenceDir = new File(heavySpleef.getDataFolder(), "persistence");
		defaultXmlDir = new File(defaultPersistenceDir, "games/xml");
		defaultSchematicDir = new File(defaultPersistenceDir, "games/schematic");
	}

	@Override
	public void enable() {
		setupDatabase();
	}
	
	@Override
	public void reload() {
		setupDatabase();
	}
	
	private void setupDatabase() {
		Map<UUID, Statistic> cachedStatistics = null;
		UUIDManager uuidManager = null;
		
		if (asyncHandler != null) {
			//Get values out of the old handler
			if (cachingHandler != null) {
				cachedStatistics = cachingHandler.getCachedStatistics();
				uuidManager = cachingHandler.getUUIDManager();
			}
			
			asyncHandler.shutdownGracefully();
		}
		
		HeavySpleef heavySpleef = getHeavySpleef();
		
		DatabaseConfig config = heavySpleef.getConfiguration(ConfigType.DATABASE_CONFIG);
		Properties properties = new Properties();
		properties.put("statistic.enabled", config.isStatisticsModuleEnabled());
		properties.put("statistic.max_cache_size", config.getMaxStatisticCacheSize());
		
		DatabaseConnection xmlConn = config.getConnection(XML_CONNECTION_IDENTIFIER);
		String xmlDirString = xmlConn.getString("dir");
		File xmlDir = xmlDirString != null ? new File(xmlDirString) : defaultXmlDir;
		if (!xmlDir.exists()) {
			xmlDir.mkdirs();
		}
		properties.put("xml.dir", xmlDir);
		
		DatabaseConnection schematicConn = config.getConnection(SCHEMATIC_CONNECTION_IDENTIFIER);
		String schematicDirString = schematicConn.getString("dir");
		File schematicDir = schematicDirString != null ? new File(schematicConn.getString("dir")) : defaultSchematicDir;
		if (!schematicDir.exists()) {
			schematicDir.mkdirs();
		}
		properties.put("schematic.dir", schematicDir);
		
		DatabaseConnection sqlConn = config.getConnection(SQL_CONNECTION_IDENTIFIER);
		for (Entry<String, Object> sqlProperty : sqlConn.getProperties().entrySet()) {
			properties.put(sqlProperty.getKey(), sqlProperty.getValue());
		}
		
		try {
			cachingHandler = new CachingReadWriteHandler(heavySpleef, properties, cachedStatistics, uuidManager);
		} catch (Exception e) {
			throw new RuntimeException("Could not enable HeavySpleef persistence module", e);
		}
		
		asyncHandler = new ForwardingAsyncReadWriteHandler(cachingHandler, heavySpleef.getPlugin(), false);
		heavySpleef.setDatabaseHandler(asyncHandler);
	}

	@Override
	public void disable() {
		HeavySpleef heavySpleef = getHeavySpleef();
		AsyncReadWriteHandler handler;
		
		if ((handler = heavySpleef.getDatabaseHandler()) != null) {
			handler.release();
		}
	}
	
}
