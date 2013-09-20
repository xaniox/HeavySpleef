/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;


import org.bukkit.configuration.file.YamlConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class FileConfig {

	private File configFile;
	private HeavySpleef t;

	public FileConfig(HeavySpleef instance) {	
		t = instance;
		final File dataFolder = t.getDataFolder();
		dataFolder.mkdirs();
		configFile = new File(dataFolder, "config.yml");
		if (!configFile.exists()) {
			this.t.getLogger().log(Level.INFO, "Could not find a config file! Creating a new...");
			this.createDefaultConfigFile();
			this.t.getConfig().setDefaults(YamlConfiguration.loadConfiguration(HeavySpleef.class.getResourceAsStream("/default/defaultconfig.yml")));
		}
	}

	private void createDefaultConfigFile() {
		try {
			configFile.createNewFile();

			final InputStream configIn = HeavySpleef.class.getResourceAsStream("/default/defaultconfig.yml");
			final FileOutputStream configOut = new FileOutputStream(configFile);
			final byte[] buffer = new byte[1024];
			int read;

			while ((read = configIn.read(buffer)) > 0)
				configOut.write(buffer, 0, read);

			configIn.close();
			configOut.close();
			this.t.getLogger().log(Level.INFO, "Config File successfully created!");
		} catch (final IOException ioe) {
			this.t.getLogger().log(Level.WARNING, "Could not create config file! IOException? Using normal values!", ioe);
			return;
		} catch (final NullPointerException npe) {
			this.t.getLogger().log(Level.WARNING, "Could not create config file! NullPointerException? Deleting config file and using normal values!", npe);
			configFile.delete();
		}
	}

}
