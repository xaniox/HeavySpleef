package me.matzefratze123.heavyspleef.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import me.matzefratze123.heavyspleef.HeavySpleef;

public class UpdateChecker {
	
	public static String checkURL = "https://dl.dropbox.com/s/50ada21795qq4q8/UpdateCheck.txt";
	
	public static void check() {
		if (!HeavySpleef.instance.getConfig().getBoolean("updateCheck"))
			return;
		String[] updateAvaible = updateAvaible();
		if (updateAvaible.length == 1 && updateAvaible[0].isEmpty())
			return;
		for (String updatePart : updateAvaible)
			HeavySpleef.instance.getLogger().info(updatePart);
	}
	
	public static String[] updateAvaible() {
		try {
			List<String> updateOutput = new ArrayList<String>();
			URL url = new URL(checkURL);
			URLConnection conn = url.openConnection();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String read = "";
			
			while ((read = reader.readLine()) != null) {
				read = read.trim();
				String[] split = read.split("=");
				
				String key = split[0];
				String value = split[1];
				
				if (key.equalsIgnoreCase("version")) {
					double newVersion = Double.parseDouble(value);
					double thisVersion = Double.parseDouble(HeavySpleef.instance.getDescription().getVersion());
					
					if (newVersion <= thisVersion)
						return new String[] {""};
					updateOutput.add("An update is avaible: v" + newVersion);
					updateOutput.add("Changes and Updates:");
				} else if (key.equalsIgnoreCase("description")) {
					String[] updates = value.split("~");
					for (String update : updates)
						updateOutput.add("- " + update);
				} else if (key.equalsIgnoreCase("download")) {
					updateOutput.add("Download: " + value);
				}
			}
			
			return updateOutput.toArray(new String[updateOutput.size()]);
		} catch (MalformedURLException e) {
			return new String[] {"Couldn't check updates!"};
		} catch (IOException e) {
			e.printStackTrace();
			return new String[] {"Couldn't check updates! IOException?!"};
		} catch (Exception e) {
			return new String[] {""};
		}
	}
	
}
