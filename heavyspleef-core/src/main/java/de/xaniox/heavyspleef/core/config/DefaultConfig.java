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
import de.xaniox.heavyspleef.core.game.GameProperty;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DefaultConfig extends ConfigurationObject {
	
	private static final List<Character> SKIP_CHARS = Lists.newArrayList('-', '_');
	public static final int CURRENT_CONFIG_VERSION = 7;
	
	private Map<GameProperty, Object> defaultGameProperties;
	private GeneralSection generalSection;
	private QueueSection queueSection;
	private Localization localization;
	private FlagSection flagSection;
	private SignSection signSection;
    private SpectateSection spectateSection;
	private UpdateSection updateSection;
	private int configVersion;

	public DefaultConfig(Configuration config) {
		super(config);
	}

	@Override
	public void inflate(Configuration config, Object... args) {
		ConfigurationSection generalSection = config.getConfigurationSection("general");
		this.generalSection = new GeneralSection(generalSection);
		
		ConfigurationSection queueSection = config.getConfigurationSection("queues");
		this.queueSection = new QueueSection(queueSection);
		
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
		
		ConfigurationSection signSection = config.getConfigurationSection("signs");
		this.signSection = new SignSection(signSection);

        ConfigurationSection spectateSection = config.getConfigurationSection("spectate");
        this.spectateSection = new SpectateSection(spectateSection);
		
		ConfigurationSection updateSection = config.getConfigurationSection("update");
		this.updateSection = new UpdateSection(updateSection);
		
		this.configVersion = config.getInt("config-version", CURRENT_CONFIG_VERSION);
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
	
	public GeneralSection getGeneralSection() {
		return generalSection;
	}

	public QueueSection getQueueSection() {
		return queueSection;
	}

	public Localization getLocalization() {
		return localization;
	}

	public FlagSection getFlagSection() {
		return flagSection;
	}

	public SignSection getSignSection() {
		return signSection;
	}

    public SpectateSection getSpectateSection() {
        return spectateSection;
    }

    public UpdateSection getUpdateSection() {
		return updateSection;
	}

	public int getConfigVersion() {
		return configVersion;
	}
	
}