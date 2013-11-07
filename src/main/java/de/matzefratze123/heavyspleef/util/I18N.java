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
package de.matzefratze123.heavyspleef.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.matzefratze123.heavyspleef.HeavySpleef;

/**
 * This class manages the dynamic loading of a language file.
 * 
 * @author matzefratze123
 */
public class I18N {

	private static Map<String, String> messages = new HashMap<String, String>();
	private static String locale;
	private static boolean dynamic;

	private static final File langFolder = new File(HeavySpleef.getInstance().getDataFolder(), "language");
	private static final String[] languages = new String[] { "en", "de", "fr", "ru" };

	static {
		if (!langFolder.exists()) {
			langFolder.mkdir();
		}
	}

	public static void setupLocale() {
		dynamic = HeavySpleef.getSystemConfig().getBoolean("language.editable");
		locale = HeavySpleef.getSystemConfig().getString("language.language", "en");

		if (dynamic) {
			copyLangFiles();
		}
		
		loadLanguage(dynamic);
		Logger.info("Loaded " + locale + " language file.");
	}
	
	public static void setLocale(String locale) {
		I18N.locale = locale;
		
		loadLanguage(dynamic);
		Logger.info("Set the language to " + locale + "!");
	}

	private static void loadLanguage(boolean dynamic) {
		InputStream inStream;
		try {
			File dynamicFile = new File(langFolder, locale + ".yml");

			if (dynamic && dynamicFile.exists()) {
				inStream = new FileInputStream(dynamicFile);
			} else {
				inStream = I18N.class.getResourceAsStream("/resource/" + locale + ".yml");
			}
			
			if (inStream == null) {
				//File is null; there is no language resource; try to load english language file
				inStream = I18N.class.getResourceAsStream("/resource/en.yml");
			}
			
			if (inStream == null) {
				throw new IOException("Completely failed to load language files. Could not get filestream. !!! EXPECT ERRORS !!!");
			}
			
			FileConfiguration yml = YamlConfiguration.loadConfiguration(inStream);
			
			for (String key : yml.getKeys(true)) {
				if (yml.isConfigurationSection(key))
					continue;
				
				String value = yml.getString(key);
				messages.put(key, value);
			}
			
			inStream.close();
		} catch (IOException e) {
			Logger.severe("[I18N] Could not load language file " + locale + ": " + e.getMessage());
		}
	}

	private static void copyLangFiles() {
		for (String lang : languages) {

			InputStream inStream = null;
			OutputStream outStream = null;

			try {
				final String realFilename = lang + ".yml";

				inStream = I18N.class.getResourceAsStream("/resource/"
						+ realFilename);

				if (inStream == null) {
					continue;
				}

				File outFile = new File(langFolder, realFilename);
				if (!outFile.exists()) {
					outFile.createNewFile();
				}

				outStream = new FileOutputStream(outFile);

				int read;
				byte[] buffer = new byte[1024];

				while ((read = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, read);
				}
			} catch (IOException e) {
				Logger.severe("[I18N] Could not load language files: "
						+ e.getMessage());
			} finally {
				try {
					if (outStream != null) {
						outStream.flush();
						outStream.close();
					}

					if (inStream != null) {
						inStream.close();
					}
				} catch (IOException e) {}
			}
		}
	}
	
	public static String getMessage(String key, String... replacements) {
		if (key == null) {
			return null;
		}
		
		String msg = messages.get(key);
		if (msg == null) {
			return null;
		}
		
		if (replacements != null) {
			for (String replacement : replacements) {
				if (replacement == null) {
					continue;
				}
				
				msg = msg.replaceFirst("%a", replacement);
			}
		}
		
		return msg;
	}
	
	public static String getMessage(String key) {
		return getMessage(key, (String[])null);
	}

}
