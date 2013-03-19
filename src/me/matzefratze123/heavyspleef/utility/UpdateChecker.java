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
package me.matzefratze123.heavyspleef.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.matzefratze123.heavyspleef.HeavySpleef;

public class UpdateChecker implements Listener {
	
	public static String checkURL = "https://dl.dropbox.com/s/50ada21795qq4q8/UpdateCheck.txt";
	
	public static boolean updateAvaible = false;
	public static String newVersion = "";
	public static String downloadUrl = "";
	
	public static void check() {
		if (!HeavySpleef.instance.getConfig().getBoolean("updateCheck", true))
			return;
		String[] updateAvaible = updateAvaible();
		if (updateAvaible.length == 1 && updateAvaible[0].isEmpty())
			return;
		
		UpdateChecker.updateAvaible = true;
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
					boolean added = false;
					String[] thisVersion = HeavySpleef.instance.getDescription().getVersion().split("\\.");
					String[] newVersion = value.split("\\.");
					
					if (thisVersion.length < newVersion.length) {
						updateOutput.add("An update is avaible: v" + value);
						updateOutput.add("Changes and Updates:");
						UpdateChecker.newVersion = value;
						added = true;
					}
					
					for (int i = 0; i < newVersion.length || i < thisVersion.length; i++) {
						if (!added && Integer.parseInt(newVersion[i]) > Integer.parseInt(thisVersion[i])) {
							updateOutput.add("An update is avaible: v" + value);
							updateOutput.add("Changes and Updates:");
							UpdateChecker.newVersion = value;
							added = true;
						}
					}
				} else if (key.equalsIgnoreCase("description")) {
					String[] updates = value.split("~");
					for (String update : updates)
						updateOutput.add("- " + update);
				} else if (key.equalsIgnoreCase("download")) {
					updateOutput.add("Download: " + value);
					UpdateChecker.downloadUrl = value;
				}
			}
			
			return updateOutput.toArray(new String[updateOutput.size()]);
		} catch (MalformedURLException e) {
			return new String[] {"Couldn't check updates!"};
		} catch (IOException e) {
			return new String[] {"Couldn't check updates! IOException?!"};
		} catch (NumberFormatException e) {
			return new String[] {""};
		} catch (ArrayIndexOutOfBoundsException e) {
			return new String[] {""};
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		if (!updateAvaible)
			return;
		if (!player.hasPermission(Permissions.CREATE_GAME.getPerm()))
			return;
		
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Your version outdated! This version: " + HeavySpleef.instance.getDescription().getVersion() + 
				           " | Latest: " + newVersion + "\nDownload: " + downloadUrl);
	}
	
}
