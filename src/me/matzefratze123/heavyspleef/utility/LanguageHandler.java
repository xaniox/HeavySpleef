package me.matzefratze123.heavyspleef.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import me.matzefratze123.heavyspleef.HeavySpleef;

/**
 * Contains methods and messages for language handling
 * 
 * @author matzefratze123
 */
public class LanguageHandler {
	
	/**
	 * This map contains all messages with the specified key
	 */
	private static Map<String, String> messages = new HashMap<String, String>();
	
	/**
	 * Loads the language file from the given language in the config.yml
	 */
	public static void loadLanguageFiles() {
		copyLanguageFiles();
		File languageFolder = new File(HeavySpleef.instance.getDataFolder().getPath() + "/language");
		languageFolder.mkdirs();
		List<String> acceptedLanguages = new ArrayList<String>();
		
		acceptedLanguages.add("de");
		acceptedLanguages.add("en");
		acceptedLanguages.add("fr");
		
		String language = HeavySpleef.instance.getConfig().getString("general.language");
		if (!acceptedLanguages.contains(language)) {
			HeavySpleef.instance.getLogger().log(Level.WARNING, "Invalid language! Setting to English...");
			setLanguage("en");
			return;
		}
		setLanguage(language);
	}
	
	private static void setLanguage(String lang) {
		File langFile = new File(HeavySpleef.instance.getDataFolder() + "/language/" + lang + ".lang");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(langFile));
			String read;
			while ((read = reader.readLine()) != null) {
				read = read.trim();
				if (read.isEmpty())
					continue;
				if (read.startsWith("#"))
					continue;
				String[] split = read.split(": ", 2);
				if (split.length != 2)
					continue;
				split[1] = ChatColor.translateAlternateColorCodes('&', split[1]);
				
				split[1] = split[1].replaceAll("ä", "\u00E4");
				split[1] = split[1].replaceAll("ö", "\u00F6");
				split[1] = split[1].replaceAll("ü", "\u00FC");
						
				messages.put(split[0], split[1]);
			}
			reader.close();
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
		String[] languageFiles = new String[] {"de", "en", "fr"};
		File dataFolder = new File(HeavySpleef.instance.getDataFolder().getPath() + "/language");
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
				
				inStream.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {}
		}
	}
	
	public static String _(String... s) {
		if (s.length == 0) return null;
		String message = messages.get(s[0]);
		for (int i = 1; i < s.length; i++) {
			if (s[i].contains("$"))
				s[i] = s[i].replace("$", "\\$");
			message = message.replaceFirst("%a", s[i]);
		}
		return message;
	}

}