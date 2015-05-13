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
