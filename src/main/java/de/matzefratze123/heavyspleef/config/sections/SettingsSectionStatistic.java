/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.config.sections;

import org.bukkit.configuration.ConfigurationSection;

import de.matzefratze123.heavyspleef.config.SpleefConfig;

public class SettingsSectionStatistic implements SettingsSection {
	
	private static final String SECTION_NAME = "statistic";
	
	private SpleefConfig configuration;
	private ConfigurationSection section;
	
	private boolean enabled;
	private String databaseType;
	private String host;
	private int port;
	private String dbName;
	private String dbUser;
	private String dbPassword;
	
	public SettingsSectionStatistic(SpleefConfig config) {
		this.configuration = config;
		this.section = config.getFileConfiguration().getConfigurationSection(SECTION_NAME);
		
		reload();
	}
	
	@Override
	public SpleefConfig getConfig() {
		return configuration;
	}

	@Override
	public ConfigurationSection getSection() {
		return section;
	}

	@Override
	public Object getValue(String path) {
		return section.get(path);
	}
	
	@Override
	public void reload() {
		enabled = section.getBoolean("enabled", true);
		databaseType = section.getString("dbType", "sqlite");
		host = section.getString("host");
		port = section.getInt("port");
		dbName = section.getString("databaseName");
		dbUser = section.getString("user");
		dbPassword = section.getString("password");
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}
	
}