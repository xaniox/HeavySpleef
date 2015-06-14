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
package de.matzefratze123.heavyspleef.core.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.GameProperty;

public class DefaultConfig extends ConfigurationObject {
	
	private static final List<Character> SKIP_CHARS = Lists.newArrayList('-', '_');
	private Map<GameProperty, Object> defaultGameProperties;
	private @Getter GeneralSection generalSection;
	private @Getter Localization localization;
	private @Getter FlagSection flagSection;
	private @Getter UpdateSection updateSection;
	
	public DefaultConfig(Configuration config) {
		super(config);
	}

	@Override
	public void inflate(Configuration config, Object... args) {
		ConfigurationSection generalSection = config.getConfigurationSection("general");
		this.generalSection = new GeneralSection(generalSection);
		
		defaultGameProperties = new EnumMap<GameProperty, Object>(GameProperty.class);
		ConfigurationSection propsSection = config.getConfigurationSection("default-game-properties");
		Set<String> keys = propsSection.getKeys(false);
		
		for (GameProperty property : GameProperty.values()) {
			for (String key : keys) {
				GameProperty mappedProperty = mapPropertyString(key);
				Object value;
				
				if (mappedProperty != null) {
					value = propsSection.get(key, mappedProperty.getDefaultValue());
				} else {
					value = property.getDefaultValue();
				}
				
				defaultGameProperties.put(mappedProperty, value);
			}
		}
		
		ConfigurationSection localizationSection = config.getConfigurationSection("localization");
		this.localization = new Localization(localizationSection);
		
		ConfigurationSection flagSection = config.getConfigurationSection("flags");
		this.flagSection = new FlagSection(flagSection);
		
		ConfigurationSection updateSection = config.getConfigurationSection("update");
		this.updateSection = new UpdateSection(updateSection);
	}
	
	public Object getGameProperty(GameProperty property) {
		return defaultGameProperties.get(property);
	}
	
	public Map<GameProperty, Object> getProperties() {
		return Collections.unmodifiableMap(defaultGameProperties);
	}
	
	private static GameProperty mapPropertyString(String configKey) {
		for (GameProperty property : GameProperty.values()) {
			String propertyName = property.name();
			
			int nameIndex = 0;
			int configIndex = 0;
			
			boolean isMatching = true;
			
			do {
				if (nameIndex >= propertyName.length() || configIndex >= configKey.length()) {
					break;
				}
				
				char nameChar = Character.toLowerCase(propertyName.charAt(nameIndex));
				char configChar = Character.toLowerCase(configKey.charAt(configIndex));
				boolean skip = false;
				
				if (SKIP_CHARS.contains(nameChar)) {
					nameIndex++;
					skip = true;
				}
				
				if (SKIP_CHARS.contains(configChar)) {
					configIndex++;
					skip = true;
				}
				
				if (skip) {
					continue;
				}
				
				isMatching = nameChar == configChar;
				configIndex++;
				nameIndex++;
			} while (isMatching);
			
			if (isMatching) {
				return property;
			}
		}
		
		return null;
	}
	
}
