/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.matzefratze123.heavyspleef.HeavySpleef;

/**
 * Provides an updater class which associate with the curse server-mod api
 * 
 * @author matzefratze123
 */
public class Updater {

	private static final int	PROJECT_ID		= 51622;

	// Keys for extracting file information from JSON response
	private static final String	API_TITLE_VALUE	= "name";
	private static final String	API_LINK_VALUE	= "downloadUrl";
	private static final String	API_NAME_VALUE	= "fileName";

	// Static information for querying the API
	private static final String	API_QUERY		= "/servermods/files?projectIds=";
	private static final String	API_HOST		= "https://api.curseforge.com";

	private File				updateFolder;

	private Thread				thread;

	private boolean				updateAvailable;
	private String				fileTitle;
	private String				fileName;
	private String				downloadUrl;

	private boolean				done			= false;

	public Updater() {
		this.updateFolder = Bukkit.getServer().getUpdateFolderFile();

		if (!updateFolder.exists()) {
			updateFolder.mkdir();
		}

		thread = new Thread(new UpdaterRunnable());
		thread.start();

	}

	public void query() {
		URL url;

		try {
			url = new URL(API_HOST + API_QUERY + PROJECT_ID);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}

		try {
			URLConnection conn = url.openConnection();

			conn.setConnectTimeout(6000);
			conn.addRequestProperty("User-Agent", "HeavySpleef-Updater (by matzefratze123)");

			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String query = reader.readLine();

			JSONArray array = (JSONArray) JSONValue.parse(query);

			String version = null;

			if (array.size() > 0) {

				// Get the newest file's details
				JSONObject latest = (JSONObject) array.get(array.size() - 1);

				fileTitle = (String) latest.get(API_TITLE_VALUE);
				fileName = (String) latest.get(API_NAME_VALUE);
				downloadUrl = (String) latest.get(API_LINK_VALUE);

				String[] parts = fileTitle.split(" v");
				if (parts.length >= 2) {
					version = parts[1];
				}

				checkVersions(version);
				done = true;
			}
		} catch (IOException e) {
			Logger.severe("Failed querying the curseforge api: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private void checkVersions(String newVersion) {
		String thisVersion = HeavySpleef.getInstance().getDescription().getVersion();

		if (thisVersion.toLowerCase().contains("dev")) {
			return;
		}

		if (!thisVersion.equalsIgnoreCase(newVersion)) {
			updateAvailable = true;
		}
	}

	public void update(final CommandSender announceTo) {
		if (!updateAvailable) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				URL url;

				try {
					url = new URL(downloadUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return;
				}

				try {
					URLConnection conn = url.openConnection();

					InputStream in = conn.getInputStream();

					File folder = Bukkit.getServer().getUpdateFolderFile();
					File out = new File(folder, fileName);

					if (!out.exists()) {
						out.createNewFile();
					}

					FileOutputStream writer = new FileOutputStream(out);

					int read;
					byte[] buffer = new byte[1024];

					long size = conn.getContentLengthLong();
					long downloaded = 0;

					int lastPercentPrinted = 0;

					while ((read = in.read(buffer, 0, 1024)) > 0) {
						downloaded += read;

						writer.write(buffer, 0, read);

						int percent = (int) ((downloaded / (double) size) * 100);
						if (percent % 10 == 0 && announceTo != null && percent != lastPercentPrinted) {
							announceTo.sendMessage(ChatColor.GREEN + "Progress: " + percent + "%");
							lastPercentPrinted = percent;
						}
					}

					writer.flush();
					writer.close();
					in.close();
					Logger.info("Downloaded " + fileName + " into update-folder " + updateFolder.getAbsolutePath() + "!");

					if (announceTo != null) {
						announceTo.sendMessage(ChatColor.DARK_GREEN + "Finished! Please " + ChatColor.UNDERLINE + "restart" + ChatColor.DARK_GREEN + " to activate the version.");
					}
				} catch (IOException e) {
					Logger.severe("Error while downloading new version: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void waitForThread() {
		if (thread != null && thread.isAlive()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isDone() {
		waitForThread();

		return done;
	}

	public boolean isUpdateAvailable() {
		waitForThread();

		return updateAvailable;
	}

	public String getFileTitle() {
		waitForThread();

		return fileTitle;
	}

	public String getDownloadUrl() {
		waitForThread();

		return downloadUrl;
	}

	private class UpdaterRunnable implements Runnable {

		@Override
		public void run() {
			query();
		}

	}

}
