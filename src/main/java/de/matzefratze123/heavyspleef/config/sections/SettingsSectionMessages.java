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
import de.matzefratze123.heavyspleef.core.BroadcastType;

public class SettingsSectionMessages implements SettingsSection {

	private static final String		SECTION_PATH	= "messages";

	private SpleefConfig			configuration;
	private ConfigurationSection	section;

	public SettingsSectionMessages(SpleefConfig config) {
		this.configuration = config;

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
		this.section = configuration.getFileConfiguration().getConfigurationSection(SECTION_PATH);
	}

	public BroadcastType getBroadcastType(MessageType messageType) {
		String str = section.getString(messageType.getConfigKey());
		if (str == null)
			return BroadcastType.RADIUS;

		BroadcastType type = BroadcastType.getBroadcastType(str);
		if (type == null)
			return BroadcastType.RADIUS;

		return type;
	}

	public enum MessageType {

		GAME_DISABLED("game-disable"), GAME_ENABLE("game-enable"), GAME_START_INFO("game-start-info"), GAME_COUNTDOWN("game-countdown"), GAME_STOP("game-stop"), PLAYER_JOIN("player-join"), PLAYER_LOSE("player-lose"), KNOCKOUTS("knockouts"), WIN("win"), TIMEOUT("timeout"), FLOOR_REGENERATION(
				"floor-regeneration");

		private String	configKey;

		private MessageType(String configKey) {
			this.configKey = configKey;
		}

		public String getConfigKey() {
			return configKey;
		}

	}

}
