/*
 * This file is part of HeavySpleef.
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
package de.matzefratze123.heavyspleef.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import de.matzefratze123.heavyspleef.core.persistence.MoreFutures;

public class Updater {
	
	private static final int THREAD_POOL_SIZE = 1;
	private static final int PROJECT_ID = 51622;
	private static final String SERVERMODS_API_URL = "https://api.curseforge.com/servermods/files?projectIds=";
	private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+\\.)+[0-9]+");
	private static final String USER_AGENT = "HeavySpleef-Updater";
	private static final int BUFFER_SIZE = 1024;
	
	private final JSONParser parser = new JSONParser();
	private final ListeningExecutorService service;
	private final Plugin plugin;
	private final PluginDescriptionFile desc;
	private @Getter final File updateFolder;
	private @Getter CheckResult result;
	
	public Updater(Plugin plugin) {
		ExecutorService execService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		this.service = MoreExecutors.listeningDecorator(execService);
		this.plugin = plugin;
		this.desc = plugin.getDescription();
		this.updateFolder = plugin.getServer().getUpdateFolderFile();
	}
	
	public ListenableFuture<CheckResult> check(FutureCallback<CheckResult> callback) {
		Callable<CheckResult> callable = new CheckCallable();
		ListenableFuture<CheckResult> future = service.submit(callable);
		
		if (callback != null) {
			MoreFutures.addBukkitSyncCallback(plugin, future, callback);
		}
		
		return future;
	}
	
	public ListenableFuture<Void> update(CommandSender progressionReceiver, FutureCallback<Void> callback) {
		if (result == null) {
			throw new IllegalArgumentException("Must check for updates with Updater#check(FutureCallback) first");
		}
		
		//We assume that the caller checked if an update is available, so just execute the update
		Callable<Void> callable = new UpdateCallable(progressionReceiver);
		ListenableFuture<Void> future = service.submit(callable);
		
		if (callback != null) {
			MoreFutures.addBukkitSyncCallback(plugin, future, callback);
		}
		
		return future;
	}
	
	private class CheckCallable implements Callable<CheckResult> {

		@Override
		public CheckResult call() throws Exception {
			URL url = new URL(SERVERMODS_API_URL + PROJECT_ID);
			URLConnection connection = url.openConnection();
			
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setRequestProperty("User-Agent", USER_AGENT);
			
			String name;
			String fileName;
			String downloadUrl;
			
			try (InputStream in = connection.getInputStream()) {
				Reader reader = new InputStreamReader(in);
				
				JSONArray fileArray = (JSONArray) parser.parse(reader);
				
				//Search for the latest jar available
				boolean isJar = false;
				for (int i = 1; !isJar && i <= fileArray.size(); i++) {
					JSONObject latestJarObj = (JSONObject) fileArray.get(fileArray.size() - i);
					
					name = (String) latestJarObj.get("name");
					fileName = (String) latestJarObj.get("fileName");
					downloadUrl = (String) latestJarObj.get("downloadUrl");
					
					if (fileName.toLowerCase().endsWith(".jar")) {
						isJar = true;
					}
				}
				
				JSONObject latestFileObj = (JSONObject) fileArray.get(fileArray.size() - 1);
				
				name = (String) latestFileObj.get("name");
				fileName = (String) latestFileObj.get("fileName");
				downloadUrl = (String) latestFileObj.get("downloadUrl");
			}
			
			Matcher versionMatcher = VERSION_PATTERN.matcher(name);
			boolean found = versionMatcher.find();
			if (!found) {
				throw new Version.VersionException("Update name '" + name + "' does not contain a version");
			}
			
			String versionString = versionMatcher.group();
			Version version = Version.parse(versionString);
			Version thisVersion = Version.parse(desc.getVersion());
			
			boolean updateAvailable = thisVersion.compareTo(version) < 0;
			
			result = new CheckResult(updateAvailable, downloadUrl, fileName, version);
			return result;
		}
		
	}
	
	@AllArgsConstructor
	private class UpdateCallable implements Callable<Void> {

		private CommandSender messageReceiver;
		
		@Override
		public Void call() throws Exception {
			String downloadUrl = result.getDownloadUrl();
			
			if (!updateFolder.exists()) {
				updateFolder.mkdir();
			}
			
			File file = new File(updateFolder, result.getFileName());
			if (!file.exists()) {
				file.createNewFile();
			}
			
			URL url = new URL(downloadUrl);
			
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			connection.setRequestProperty("User-Agent", USER_AGENT);
			
			final long size = connection.getContentLengthLong();
			long downloaded = 0;
			int lastPercentagePrinted = 5;
			
			try (InputStream in = connection.getInputStream(); 
					OutputStream out = new FileOutputStream(file)) {
				int read;
				byte[] buffer = new byte[BUFFER_SIZE];
				
				while ((read = in.read(buffer, 0, BUFFER_SIZE)) > 0) {
					downloaded += read;
					out.write(buffer, 0, read);
					
					int percent = (int) (downloaded * 100D / size);
					if (percent % 5 == 0 && messageReceiver != null && percent != lastPercentagePrinted) {
						StringBuilder progressionBuilder = new StringBuilder();
						progressionBuilder.append(ChatColor.GREEN);
						int partsDownloaded = percent / 5;
						int partsLeft = (100 - percent) / 5;
						
						for (int i = 0; i < partsDownloaded; i++) {
							progressionBuilder.append("|");
						}
						
						progressionBuilder.append(ChatColor.RED);
						for (int i = 0; i < partsLeft; i++) {
							progressionBuilder.append("|");
						}
						
						messageReceiver.sendMessage(ChatColor.DARK_GRAY + " [" + progressionBuilder
								+ ChatColor.DARK_GRAY + "] " + ChatColor.GOLD + percent + "%");
						lastPercentagePrinted = percent;
					}
				}
			}
			
			return null;
		}
		
	}
	
	public static class Version implements Comparable<Version> {
		
		private static final String SNAPSHOT_IDENTIFIER = "-SNAPSHOT";
		private static final String SPLIT_BY_DOT = "\\.";
		private int[] components;
		private boolean snapshot;
		
		public Version(int[] components, boolean snapshot) {
			this.components = components;
			this.snapshot = snapshot;
		}
		
		public static Version parse(String versionString) throws VersionException {
			boolean snapshot;
			if ((snapshot = versionString.endsWith(SNAPSHOT_IDENTIFIER))) {
				versionString = versionString.substring(0, versionString.lastIndexOf(SNAPSHOT_IDENTIFIER));
			}
			
			String[] strComponents = versionString.split(SPLIT_BY_DOT);
			final int deepness = strComponents.length;
			int[] components = new int[deepness];
			
			try {
				for (int i = 0; i < deepness; i++) {
					int comp = Integer.parseInt(strComponents[i]);
					components[i] = comp;
				}
			} catch (NumberFormatException nfe) {
				throw new InvalidVersionException("Invalid version string '" + versionString + "'");
			}
			
			return new Version(components, snapshot);
		}
		
		@Override
		public String toString() {
			int length = components.length;
			StringBuilder builder = new StringBuilder();
			
			for (int i = 0; i < length; i++) {
				builder.append(components[i]);
				
				if (i + 1 < length) {
					builder.append('.');
				}
			}
			
			if (snapshot) {
				builder.append(SNAPSHOT_IDENTIFIER);
			}
			
			return builder.toString();
		}
		
		@Override
		public int compareTo(Version o) {
			int[] otherComponents = o.components;
			int thisDeepness = components.length;
			int otherDeepness = otherComponents.length;
			
			int deepness = Math.max(thisDeepness, otherDeepness);
			
			for (int i = 0; i < deepness; i++) {
				int comp = i < thisDeepness ? components[i] : 0;
				int otherComp = i < otherDeepness ? otherComponents[i] : 0;
				
				if (comp < otherComp) {
					return -1;
				} else if (comp > otherComp) {
					return 1;
				}
			}
			
			//Version are exact the same, try to check if one is snapshot
			if (snapshot && !o.snapshot) {
				return 1;
			} else if (!snapshot && o.snapshot) {
				return -1;
			}
			
			return 0;
		}
		
		private static class VersionException extends RuntimeException {

			private static final long serialVersionUID = 7300635845275692986L;
			
			public VersionException(String message) {
				super(message);
			}
			
		}
		
		private static class InvalidVersionException extends VersionException {

			private static final long serialVersionUID = 3202153428976606075L;
			
			public InvalidVersionException(String message) {
				super(message);
			}
			
		}
		
	}
	
	@AllArgsConstructor
	@Getter
	public class CheckResult {
		
		private boolean updateAvailable;
		private String downloadUrl;
		private String fileName;
		private Version version;
		
	}

}
