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

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import de.matzefratze123.heavyspleef.config.SpleefConfig;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Util;

public class SettingsSectionScoreboard implements SettingsSection {
	
	private static final String SECTION_PATH = "scoreboards";
	
	private SpleefConfig configuration;
	private ConfigurationSection section;
	
	private SimpleBlockData fontData;
	private SimpleBlockData canvasData;
	
	public SettingsSectionScoreboard(SpleefConfig config) {
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
		
		final byte redWoolData = (byte)14;
		final byte blackWoolData = (byte)15;
		
		fontData = Util.parseMaterial(section.getString("fontID"), true);
		canvasData = Util.parseMaterial(section.getString("baseID"), true);
		
		if (fontData == null)
			fontData = new SimpleBlockData(Material.WOOL, redWoolData);
		if (canvasData == null)
			canvasData = new SimpleBlockData(Material.WOOL, blackWoolData);
	}

	public SimpleBlockData getFontData() {
		return fontData;
	}

	public SimpleBlockData getCanvasData() {
		return canvasData;
	}

}
