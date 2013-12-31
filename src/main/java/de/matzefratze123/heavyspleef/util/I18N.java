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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import de.matzefratze123.heavyspleef.HeavySpleef;

/**
 * Contains methods and messages for language handling
 * 
 * @author matzefratze123
 */
public class I18N {
	
	/**
	 * This map contains all messages with the specified key
	 */
	private static Map<String, String> messages = new HashMap<String, String>();
	
	/**
	 * Loads the language file from the given language in the config.yml
	 */
	public static void loadLanguageFiles() {
		boolean fromFile = HeavySpleef.getSystemConfig().getLanguageSection().isEditableFiles();
		if (fromFile)
			copyLanguageFiles();
		File languageFolder = new File(HeavySpleef.getInstance().getDataFolder().getPath() + "/language");
		languageFolder.mkdirs();
		
		Language language = HeavySpleef.getSystemConfig().getLanguageSection().getLanguage();
		setLocale(language, fromFile);
	}
	
	private static void setLocale(Language locale, boolean fromFile) {
		File langFile = null;
		
		if (fromFile) {
			langFile = new File("plugins/HeavySpleef/language/" + locale.getLanguageCode() + ".lang");
			if (!langFile.exists())
				langFile = null;
		}
		
		try {
			InputStream stream;
			if (langFile == null || !fromFile) {
				stream = HeavySpleef.class.getResourceAsStream("/resource/" + locale.getLanguageCode() + ".lang");
			} else {
				stream = new FileInputStream(langFile);
			}
			
			if (stream == null) {
				stream = I18N.class.getResourceAsStream("/resource/" + Language.ENGLISH.getCountryCode() + ".lang");
			}
			
			InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(streamReader);
			
			String read;
			
			while ((read = reader.readLine()) != null) {
				read = read.trim();
				if (read.isEmpty())
					continue;
				if (read.startsWith("#"))
					continue;
				String[] split = read.split(": ", 2);
				if (split.length < 2)
					continue;
				
				split[1] = ChatColor.translateAlternateColorCodes('&', split[1]);
				
				split[1] = split[1].replace("\\n", "\n");
				
				split[1] = split[1].replace("\\u00E4", "ä");
				split[1] = split[1].replace("\\u00F6", "ö");
				split[1] = split[1].replace("\\u00FC", "ü");
				
				split[1] = new String(split[1].getBytes(Charset.forName("UTF-16")), "UTF-16");
						
				messages.put(split[0], split[1]);
			}
			reader.close();
			streamReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Copy's all language files to the plugin folder
	 * (It doesn't copy if the files are existing!)
	 */
	private static void copyLanguageFiles() {
		String[] languageFiles = new String[] {"de", "en", "fr", "ru"};
		File dataFolder = new File(HeavySpleef.getInstance().getDataFolder().getPath() + "/language");
		dataFolder.mkdirs();
		
		for (String lFile : languageFiles) {
			try {
				File outFile = new File(dataFolder.getPath() + "/" + lFile + ".lang");
				if (outFile.exists())
					continue;
				
				outFile.createNewFile();
				final InputStream inStream = HeavySpleef.class.getResourceAsStream("/resource/" + lFile + ".lang");
				final FileOutputStream outStream = new FileOutputStream(outFile);
				
				int read;
				byte[] buffer = new byte[1024];
				
				while((read = inStream.read(buffer)) > 0)
					outStream.write(buffer, 0, read);
				
				outStream.flush();
				inStream.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {}
		}
	}
	
	public static String _(String... parts) {
		if (parts.length == 0) {
			return null;
		}
		
		String message = messages.get(parts[0]);
		
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].contains("$")) {
				parts[i] = parts[i].replace("$", "\\$");
			}
			
			message = message.replaceFirst("%a", parts[i]);
		}
		return HeavySpleef.PREFIX + ChatColor.RESET + " " + message;
	}

	public static String __(String... parts) {
		if (parts.length == 0) {
			return null;
		}
		
		String message = messages.get(parts[0]);
		
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].contains("$")) {
				parts[i] = parts[i].replace("$", "\\$");
			}
			
			message = message.replaceFirst("%a", parts[i]);
		}
		return message;
	}
	
	public enum Language {
		
		ENGLISH("en", "US"),
		GERMAN("de", "DE"),
		FRANCE("fr", "FR"),
		RUSSIAN("ru", "RU"),
		CUSTOM("", "");
		
		private String languageCode;
		private String countryCode;
		
		private Language(String languageCode, String countryCode) {
			this.languageCode = languageCode;
			this.countryCode = countryCode;
		}
		
		public void setLanguageCode(String languageCode) {
			this.languageCode = languageCode;
		}
		
		public String getLanguageCode() {
			return languageCode;
		}
		
		public String getCountryCode() {
			return countryCode;
		}
		
		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}
		
		public static Language byLanguageCode(String code) {
			for (Language lang : values()) {
				if (lang.getLanguageCode().equalsIgnoreCase(code)) {
					return lang;
				}
			}
			
			return null;
		}
		
	}

}