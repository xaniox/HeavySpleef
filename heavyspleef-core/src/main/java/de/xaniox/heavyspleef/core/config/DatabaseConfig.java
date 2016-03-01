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
package de.xaniox.heavyspleef.core.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;

public class DatabaseConfig extends ConfigurationObject {
	
	private static final int DEFAULT_MAX_CACHE_SIZE = 1024;
	
	private boolean statisticsEnabled;
	private int maxStatisticCacheSize;
	private List<DatabaseConnection> connections;
	
	public DatabaseConfig(Configuration config, Object... args) {
		super(config, args);
	}
	
	@Override
	public void inflate(Configuration config, Object... args) {
		Validate.isTrue(args.length > 0, "args.length must be greater than 0");
		Validate.isTrue(args[0] instanceof File, "args[0] must be an instance of " + File.class.getCanonicalName());
		
		File baseDir = (File) args[0];
		
		ConfigurationSection moduleSection = config.getConfigurationSection("database-modules");
		this.statisticsEnabled = moduleSection.getBoolean("statistics.enabled");
		this.maxStatisticCacheSize = moduleSection.getInt("statistics.max-cache-size", DEFAULT_MAX_CACHE_SIZE);
		
		this.connections = Lists.newArrayList();
		ConfigurationSection connectionsSection = config.getConfigurationSection("persistence-connection");
		
		for (String key : connectionsSection.getKeys(false)) {
			connections.add(new DatabaseConnection(connectionsSection.getConfigurationSection(key), baseDir));
		}
	}
	
	public boolean isStatisticsModuleEnabled() {
		return statisticsEnabled;
	}
	
	public int getMaxStatisticCacheSize() {
		return maxStatisticCacheSize;
	}
	
	public List<DatabaseConnection> getDatabaseConnections() {
		return connections;
	}
	
	public DatabaseConnection getConnection(String name) {
		for (DatabaseConnection connection : connections) {
			if (connection.getIdentifier().equals(name)) {
				return connection;
			}
		}
		
		return null;
	}

}