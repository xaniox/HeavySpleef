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

import java.text.ParseException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.layout.SignLayout;

public class SignLayoutConfiguration extends ThrowingConfigurationObject<ParseException> {
	
	private SignLayout layout;
	private ConfigurationSection options;
	
	public SignLayoutConfiguration(Configuration config) {
		super(config);
	}

	@Override
	public void inflateUnsafe(Configuration config, Object[] args) throws ParseException {
		List<String> lines = Lists.newArrayList();
		
		ConfigurationSection layoutSection = config.getConfigurationSection("layout");
		
		for (int i = 1; i <= SignLayout.LINE_COUNT; i++) {
			String line = layoutSection.getString(String.valueOf(i));
			lines.add(line);
		}
		
		layout = new SignLayout(lines);
		
		if (config.contains("options")) {
			options = config.getConfigurationSection("options");
		}
	}
	
	public SignLayout getLayout() {
		return layout;
	}
	
	public String getOption(String key) {
		return getOption(key, null);
	}
	
	public String getOption(String key, String def) {
		if (options == null) {
			return def;
		}
		
		return ChatColor.translateAlternateColorCodes('&', options.getString(key));
	}

	@Override
	protected Class<? extends ParseException> getExceptionClass() {
		return ParseException.class;
	}

}
