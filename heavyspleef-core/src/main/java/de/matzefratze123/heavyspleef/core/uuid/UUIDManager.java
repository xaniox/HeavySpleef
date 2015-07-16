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
package de.matzefratze123.heavyspleef.core.uuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public class UUIDManager {
	
	private static final String NAME_BASE_URL = "https://api.mojang.com/profiles/minecraft";
	private static final String UUID_BASE_URL = " https://api.mojang.com/user/profiles/%s/names";
	private static final int PROFILES_PER_REQUEST = 100;
	
	private final JSONParser parser = new JSONParser();
	private final boolean onlineMode;
	
	private LoadingCache<String, GameProfile> profileNameCache;
	private LoadingCache<UUID, GameProfile> profileUUIDCache;
	
	public UUIDManager() {
		// Use fast Entity#getUniqueId() when onlineMode = true
		// so we don't have to query the mojang servers
		this.onlineMode = Bukkit.getOnlineMode();
		
		this.profileNameCache = CacheBuilder.newBuilder()
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, GameProfile>() {

					@SuppressWarnings("deprecation")
					@Override
					public GameProfile load(String key) throws Exception {
						GameProfile profile;
						
						List<GameProfile> profiles = fetchGameProfiles(new String[] {key});
						
						if (profiles.size() != 0) {
							profile = profiles.get(0);
						} else {
							OfflinePlayer player = Bukkit.getOfflinePlayer(key);
							profile = new GameProfile(player.getUniqueId(), player.getName());
						}
						
						validateContain(profile);
						return profile;
					}
					
					private void validateContain(GameProfile profile) {
						Map<UUID, GameProfile> map = profileUUIDCache.asMap();
						if (!map.containsKey(profile.getUniqueIdentifier())) {
							profileUUIDCache.put(profile.getUniqueIdentifier(), profile);
						}
					}
				});
		
		this.profileUUIDCache = CacheBuilder.newBuilder()
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<UUID, GameProfile>() {

					@Override
					public GameProfile load(UUID key) throws Exception {
						GameProfile profile = fetchGameProfile(key);
						
						if (profile == null) {
							OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(key);
							profile = new GameProfile(player.getUniqueId(), player.getName());
						}
						
						validateContain(profile);
						return profile;
					}
					
					private void validateContain(GameProfile profile) {
						Map<String, GameProfile> map = profileNameCache.asMap();
						if (!map.containsKey(profile.getUniqueIdentifier())) {
							profileUUIDCache.put(profile.getUniqueIdentifier(), profile);
						}
					}
				});
	}
	
	@SuppressWarnings("deprecation")
	public GameProfile getProfile(String name) throws ExecutionException {
		return getProfile(Bukkit.getOfflinePlayer(name));
	}
	
	public GameProfile getProfile(OfflinePlayer player) throws ExecutionException {
		GameProfile profile = null;
		
		if (onlineMode) {
			// We're in online mode so we can use the bukkit api
			// to construct our GameProfile
			profile = new GameProfile(player.getUniqueId(), player.getName());
		} else {
			profile = profileNameCache.get(player.getName());
		}
		
		return profile;
	}
	
	public List<GameProfile> getProfiles(OfflinePlayer[] players) throws ExecutionException {
		String[] names = new String[players.length];
		for (int i = 0; i < players.length; i++) {
			names[i] = players[i].getName();
		}
		
		return getProfiles(names);
	}
	
	public List<GameProfile> getProfiles(String[] names) throws ExecutionException {
		return getProfiles(names, false);
	}
	
	@SuppressWarnings("deprecation")
	public List<GameProfile> getProfiles(String[] names, boolean forceMojangAPI) throws ExecutionException {
		List<GameProfile> profiles = Lists.newArrayList();
		
		if (onlineMode && !forceMojangAPI) {
			for (String name : names) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(name);
				GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
				profiles.add(profile);
			}
		} else {
			List<String> profilesToLoad = Lists.newArrayList();
			
			for (String name : names) {
				if (profileNameCache.getIfPresent(name) == null) {
					profilesToLoad.add(name);
				} else {
					profiles.add(profileNameCache.get(name));
				}
			}
			
			if (!profilesToLoad.isEmpty()) {
				try {
					profiles.addAll(fetchGameProfiles(profilesToLoad.toArray(new String[profilesToLoad.size()])));
				} catch (Exception e) {
					throw new ExecutionException(e);
				}
				
				for (String loadingName : profilesToLoad) {
					GameProfile found = null;
					
					for (GameProfile profile : profiles) {
						if (profile.getName().equalsIgnoreCase(loadingName)) {
							found = profile;
							break;
						}
					}
					
					if (found == null) {
						OfflinePlayer player = Bukkit.getOfflinePlayer(loadingName);
						UUID uuid = player.getUniqueId();
						
						profiles.add(new GameProfile(uuid, player.getName()));
					}
					
					profileNameCache.put(loadingName, found);
					profileUUIDCache.put(found.getUniqueIdentifier(), found);
				}
			}
		}
		
		return profiles;
	}
	
	public GameProfile getProfile(UUID uuid) throws ExecutionException {
		GameProfile profile;
		
		if (onlineMode) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			profile = new GameProfile(uuid, player.getName());
		} else {
			profile = profileUUIDCache.get(uuid);
		}
		
		return profile;
	}
	
	public List<GameProfile> getProfiles(UUID[] uuids) throws ExecutionException {
		List<GameProfile> profiles = Lists.newArrayList();
		
		if (onlineMode) {
			for (UUID uuid : uuids) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				GameProfile profile = new GameProfile(uuid, player.getName());
				profiles.add(profile);
			}
		} else {
			List<UUID> profilesToLoad = Lists.newArrayList();
			
			for (UUID uuid : uuids) {
				if (profileUUIDCache.getIfPresent(uuid) == null) {
					profilesToLoad.add(uuid);
				} else {
					GameProfile profile;
					
					try {
						profile = fetchGameProfile(uuid);
					} catch (Exception e) {
						throw new ExecutionException(e);
					}
					
					if (profile == null) {
						OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
						profile = new GameProfile(uuid, player.getName());
					}
					
					profileUUIDCache.put(uuid, profile);
					profileNameCache.put(profile.getName(), profile);
					
					profiles.add(profile);
				}
			}
		}
		
		return profiles;
	}
	
	private List<GameProfile> fetchGameProfiles(String[] names) throws IOException, ParseException {
		List<String> namesList = Arrays.asList(names);
		URL url = new URL(NAME_BASE_URL);
		List<GameProfile> profiles = Lists.newArrayList();		
		int requests = (int) Math.ceil((double)names.length / PROFILES_PER_REQUEST);
		
		for (int i = 0; i < requests; i++) {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			
			String body = JSONArray.toJSONString(namesList.subList(i * 100, Math.min((i + 1) * 100, namesList.size())));
			
			OutputStream out = connection.getOutputStream();
			out.write(body.getBytes());
			out.flush();
			out.close();
			
			InputStream in = connection.getInputStream();
			Reader reader = new InputStreamReader(in);
			
			JSONArray resultArray = (JSONArray) parser.parse(reader);
			
			for (Object object : resultArray) {
				JSONObject result = (JSONObject) object;
				
				String name = (String) result.get("name");
				UUID uuid = getUUID((String) result.get("id"));
				GameProfile profile = new GameProfile(uuid, name);
				
				profiles.add(profile);
			}
		}
		
		return profiles;
	}
	
	private GameProfile fetchGameProfile(UUID uuid) throws IOException, ParseException {
		String uuidString = uuid.toString().replace("-", "");
		URL url = new URL(String.format(UUID_BASE_URL, uuidString));
		
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		InputStream inputStream = connection.getInputStream();
		
		JSONArray result = (JSONArray) parser.parse(new InputStreamReader(inputStream));
		JSONObject current = (JSONObject) result.get(result.size() - 1);
		String name = (String) current.get("name");
		if (name == null) {
			return null;
		}
		
		GameProfile profile = new GameProfile(uuid, name);
		return profile;
	}

	private static UUID getUUID(String id) {
		return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-"
				+ id.substring(20, 32));
	}

}
