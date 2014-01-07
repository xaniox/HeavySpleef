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
package de.matzefratze123.heavyspleef.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionAntiCamping;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionFlagDefaults;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionGeneral;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionLanguage;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionLeaderBoard;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionQueues;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionRoot;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionScoreboard;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionSounds;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionStatistic;
import de.matzefratze123.heavyspleef.util.Logger;

public class SpleefConfig {
	
	private static final File   CONFIG_FILE         = new File("plugins/" + HeavySpleef.PLUGIN_NAME + "/config.yml");
	private static final String RESOURCE_PATH       = "/default/defaultconfig.yml";
	private static final int    CONFIG_VERSION      = 2;
	private static final String CONFIG_VERSION_PATH = "config-version";
	
	private static final int    BUFFER_SIZE         = 1024;
	
	private FileConfiguration   configuration;
	
	//Declare sections
	private SettingsSectionRoot rootSection;
	private SettingsSectionGeneral generalSection;
	private SettingsSectionLeaderBoard leaderboardSection;
	private SettingsSectionMessages messagesSection;
	private SettingsSectionFlagDefaults flagDefaultsSection;
	private SettingsSectionQueues queuesSection;
	private SettingsSectionScoreboard scoreboardSection;
	private SettingsSectionLanguage languageSection;
	private SettingsSectionAntiCamping anticampingSection;
	private SettingsSectionSounds soundsSection;
	private SettingsSectionStatistic statisticSection;
	
	public SpleefConfig() {
		loadConfig();
		
		rootSection = new SettingsSectionRoot(this);
		generalSection = new SettingsSectionGeneral(this);
		leaderboardSection = new SettingsSectionLeaderBoard(this);
		messagesSection = new SettingsSectionMessages(this);
		flagDefaultsSection = new SettingsSectionFlagDefaults(this);
		queuesSection = new SettingsSectionQueues(this);
		scoreboardSection = new SettingsSectionScoreboard(this);
		languageSection = new SettingsSectionLanguage(this);
		anticampingSection = new SettingsSectionAntiCamping(this);
		soundsSection = new SettingsSectionSounds(this);
		statisticSection = new SettingsSectionStatistic(this);
	}
	
	private void loadConfig() {
		boolean copyFailed = false;
		
		if (!checkCopy()) {
			//Copy file first
			InputStream inStream = null;
			OutputStream outStream = null;
			
			try {
				if (!CONFIG_FILE.exists()) {
					CONFIG_FILE.createNewFile();
				}
				
				inStream = SpleefConfig.class.getResourceAsStream(RESOURCE_PATH);
				outStream = new FileOutputStream(CONFIG_FILE);
				
				byte[] buffer = new byte[BUFFER_SIZE];
				int read;
				
				while ((read = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, read);
				}
				
				outStream.flush();
			} catch (IOException copyException) {
				Logger.severe("Failed to copy default config: " + copyException);
				Logger.severe("Using default config values.");
				copyFailed = true;
			} finally {
				try {
					if (inStream != null) {
						inStream.close();
					}
					if (outStream != null) {
						outStream.close();
					}
				} catch (Exception e) {}
			}
		}
		
		InputStream inStream;
		
		//Ok, now try to load the config
		if (copyFailed || !CONFIG_FILE.exists()) {
			//Somehow we failed to load config by file
			inStream = SpleefConfig.class.getResourceAsStream(RESOURCE_PATH);
		} else {
			try {
				//Load config by file
				inStream = new FileInputStream(CONFIG_FILE);
			} catch (FileNotFoundException e) {
				//Should not be fired, but safety first
				inStream = SpleefConfig.class.getResourceAsStream(RESOURCE_PATH);
			}
		}
		
		configuration = YamlConfiguration.loadConfiguration(inStream);
	}
	
	//Returns true when no copy is needed, false otherwise
	private boolean checkCopy() {
		if (!CONFIG_FILE.exists()) {
			return false;
		}
		
		int version = YamlConfiguration.loadConfiguration(CONFIG_FILE).getInt(CONFIG_VERSION_PATH);
		return version == CONFIG_VERSION;
	}
	
	public FileConfiguration getFileConfiguration() {
		return configuration;
	}
	
	public void reload() {
		loadConfig();
		
		rootSection.reload();
		generalSection.reload();
		leaderboardSection.reload();
		messagesSection.reload();
		flagDefaultsSection.reload();
		queuesSection.reload();
		scoreboardSection.reload();
		languageSection.reload();
		anticampingSection.reload();
		soundsSection.reload();
		statisticSection.reload();
	}

	public FileConfiguration getConfiguration() {
		return configuration;
	}

	public SettingsSectionRoot getRootSection() {
		return rootSection;
	}

	public SettingsSectionGeneral getGeneralSection() {
		return generalSection;
	}

	public SettingsSectionLeaderBoard getLeaderboardSection() {
		return leaderboardSection;
	}

	public SettingsSectionMessages getMessagesSection() {
		return messagesSection;
	}

	public SettingsSectionFlagDefaults getFlagDefaultsSection() {
		return flagDefaultsSection;
	}

	public SettingsSectionQueues getQueuesSection() {
		return queuesSection;
	}

	public SettingsSectionScoreboard getScoreboardSection() {
		return scoreboardSection;
	}

	public SettingsSectionLanguage getLanguageSection() {
		return languageSection;
	}

	public SettingsSectionAntiCamping getAnticampingSection() {
		return anticampingSection;
	}

	public SettingsSectionSounds getSoundsSection() {
		return soundsSection;
	}

	public SettingsSectionStatistic getStatisticSection() {
		return statisticSection;
	}
	
}
 