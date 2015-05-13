/*
 * This file is part of addons.
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
package de.matzefratze123.joingui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Getter;
import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;

public class JoinGuiAddOn extends BasicAddOn {
	
	private static final String CONFIG_FILE_NAME = "gui-entry-inventory.yml";
	private static final String UTF_8 = "UTF-8";
	
	private @Getter InventoryEntryConfig inventoryEntryConfig;
	
	@Override
	public void enable() {
		final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);
		InputStream configIn = null;
		
		if (!configFile.exists()) {
			try {
				copyResource("/" + CONFIG_FILE_NAME, configFile);
				configIn = new FileInputStream(configFile);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not copy configuration for inventory entries", e);
				getLogger().log(Level.SEVERE, "Using default, built-in layout");
				configIn = getClass().getResourceAsStream("/" + CONFIG_FILE_NAME);
			}
		} else {
			try {
				configIn = new FileInputStream(configFile);
			} catch (FileNotFoundException e) {
				//We checked if this file exists
				e.printStackTrace();
			}
		}
		
		try {
			Reader reader = new InputStreamReader(configIn, UTF_8);
			Configuration config = YamlConfiguration.loadConfiguration(reader);
			
			inventoryEntryConfig = new InventoryEntryConfig(config);
		} catch (UnsupportedEncodingException e) {
			getLogger().log(Level.SEVERE, "It seems like your system does not support UTF8 encoding, unable to read inventory entry layout");
			getLogger().log(Level.SEVERE, "Shutting add-on down...");
			getAddOnManager().disableAddOn(this);
		}
	}
	
}
