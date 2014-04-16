/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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

public class SettingsSectionRoot implements SettingsSection {

	private SpleefConfig	config;

	private boolean			autoUpdate;
	private int				configVersion;

	public SettingsSectionRoot(SpleefConfig config) {
		this.config = config;

		reload();
	}

	@Override
	public SpleefConfig getConfig() {
		return config;
	}

	@Override
	public ConfigurationSection getSection() {
		return config.getFileConfiguration();
	}

	@Override
	public Object getValue(String path) {
		return config.getFileConfiguration().get(path);
	}

	@Override
	public void reload() {
		autoUpdate = config.getFileConfiguration().getBoolean("auto-update");
		configVersion = config.getFileConfiguration().getInt("config-version");
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public int getConfigVersion() {
		return configVersion;
	}

}
