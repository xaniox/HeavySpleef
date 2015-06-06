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
package de.matzefratze123.heavyspleef.migration;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unsynchronized.ArrayCollection;
import org.unsynchronized.ArrayObject;
import org.unsynchronized.ClassDescription;
import org.unsynchronized.Content;
import org.unsynchronized.EnumObject;
import org.unsynchronized.Field;
import org.unsynchronized.Instance;
import org.unsynchronized.JDeserialize;
import org.unsynchronized.StringObject;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall.WallValidationException;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.floor.SimpleClipboardFloor;
import de.matzefratze123.heavyspleef.flag.defaults.FlagAntiCamping;
import de.matzefratze123.heavyspleef.flag.defaults.FlagBowspleef;
import de.matzefratze123.heavyspleef.flag.defaults.FlagItemReward;
import de.matzefratze123.heavyspleef.flag.defaults.FlagLeavepoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagLobby;
import de.matzefratze123.heavyspleef.flag.defaults.FlagLosePoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagMaxTeamSize;
import de.matzefratze123.heavyspleef.flag.defaults.FlagMinTeamSize;
import de.matzefratze123.heavyspleef.flag.defaults.FlagMultiSpawnpoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagQueueLobby;
import de.matzefratze123.heavyspleef.flag.defaults.FlagScoreboard;
import de.matzefratze123.heavyspleef.flag.defaults.FlagShears;
import de.matzefratze123.heavyspleef.flag.defaults.FlagShovels;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSpawnpoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSpectate;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSplegg;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam;
import de.matzefratze123.heavyspleef.flag.defaults.FlagWinPoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam.TeamColor;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;
import de.matzefratze123.heavyspleef.flag.presets.ItemStackFlag;
import de.matzefratze123.heavyspleef.flag.presets.ItemStackListFlag;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;
import de.matzefratze123.heavyspleef.flag.presets.LocationListFlag;
import de.matzefratze123.heavyspleef.persistence.xml.GameAccessor;

@RequiredArgsConstructor
public class GameMigrator implements Migrator<Configuration, File> {

	private static final String FILE_EXTENSION = ".xml";
	private static final int TEAM_FLAG_NOT_SET = -1;
	private static final Map<String, Class<? extends AbstractFlag<?>>> LEGACY_TO_FLAG_MAPPING = Maps.newHashMap();
	
	static {
		LEGACY_TO_FLAG_MAPPING.put("shovels", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("shears", FlagShears.class);
		LEGACY_TO_FLAG_MAPPING.put("bowspleef", FlagBowspleef.class);
		LEGACY_TO_FLAG_MAPPING.put("splegg", FlagSplegg.class);
		LEGACY_TO_FLAG_MAPPING.put("anticamping", FlagAntiCamping.class);
		LEGACY_TO_FLAG_MAPPING.put("scoreboard", FlagScoreboard.class);
		LEGACY_TO_FLAG_MAPPING.put("win", FlagWinPoint.class);
		LEGACY_TO_FLAG_MAPPING.put("lose", FlagLosePoint.class);
		LEGACY_TO_FLAG_MAPPING.put("lobby", FlagLobby.class);
		LEGACY_TO_FLAG_MAPPING.put("queuelobby", FlagQueueLobby.class);
		LEGACY_TO_FLAG_MAPPING.put("spectate", FlagSpectate.class);
		LEGACY_TO_FLAG_MAPPING.put("spawnpoint", FlagSpawnpoint.class);
		LEGACY_TO_FLAG_MAPPING.put("leavepoint", FlagLeavepoint.class);
		LEGACY_TO_FLAG_MAPPING.put("nextspawnpoint", FlagMultiSpawnpoint.class);
		LEGACY_TO_FLAG_MAPPING.put("itemreward", FlagItemReward.class);
		LEGACY_TO_FLAG_MAPPING.put("minplayers", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("maxplayers", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("autostart", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("countdown", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("entryfee", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("reward", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("timeout", FlagShovels.class);
		LEGACY_TO_FLAG_MAPPING.put("regen", FlagShovels.class);
	}
	
	private final OutputFormat outputFormat = OutputFormat.createPrettyPrint();
	private final JDeserialize jdeserialize = new JDeserialize();
	private @NonNull HeavySpleef heavySpleef;
	
	@Override
	public void migrate(Configuration inputSource, File outputFolder) throws MigrationException {
		Set<String> gameNames = inputSource.getKeys(false);
		for (String name : gameNames) {
			ConfigurationSection section = inputSource.getConfigurationSection(name);
			
			File xmlFile = new File(outputFolder, name + FILE_EXTENSION);
			if (xmlFile.exists()) {
				//Rename this game as there is already a file
				xmlFile = new File(outputFolder, name + "_1" + FILE_EXTENSION);
			}
			
			XMLWriter writer = null;
			
			try {
				xmlFile.createNewFile();
				
				GameAccessor accessor = new GameAccessor(heavySpleef);
				Document document = DocumentHelper.createDocument();
				Element rootElement = document.addElement("game");
				
				migrateGame(section, accessor, rootElement);
				
				OutputStream out = new FileOutputStream(xmlFile);
				writer = new XMLWriter(out, outputFormat);
				writer.write(document);
			} catch (IOException e) {
				throw new MigrationException(e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	
	private void migrateGame(ConfigurationSection in, GameAccessor accessor, Element element) throws MigrationException {
		String name = in.getName();
		World world = legacyStringToLocation(in.getString("first")).getWorld();
		Game game = new Game(null, name, world);
		
		ConfigurationSection floorsSection = in.getConfigurationSection("floors");
		
		if (floorsSection != null) {
			for (String floorKey : floorsSection.getKeys(false)) {
				ConfigurationSection floorSection = floorsSection.getConfigurationSection(floorKey);
				String id = "floor_" + floorSection.getString("id");
				
				String shape = floorSection.getString("shape");
				Region region;
				
				if (shape.equals("CUBOID")) {
					Vector first = legacyStringToVector(floorSection.getString("first"));
					Vector second = legacyStringToVector(floorSection.getString("second"));
					
					region = new CuboidRegion(first, second);
				} else if (shape.equals("CYLINDER")) {
					//TODO: Add cylinder floor support for older versions of HeavySpleef?
					continue;
				} else {
					//Unknown floor type
					continue;
				}
				
				Clipboard clipboard = new BlockArrayClipboard(region);
				Floor floor = new SimpleClipboardFloor(id, clipboard);
				
				game.addFloor(floor);
			}
		}
		
		ConfigurationSection losezonesSection = in.getConfigurationSection("losezones");
		
		if (losezonesSection != null) {
			for (String losezoneKey : losezonesSection.getKeys(false)) {
				ConfigurationSection losezoneSection = losezonesSection.getConfigurationSection(losezoneKey);
				String id = "deathzone_" + losezoneSection.getString("id");
				
				Vector first = legacyStringToVector(losezoneSection.getString("first"));
				Vector second = legacyStringToVector(losezoneSection.getString("second"));
				Region region = new CuboidRegion(first, second);
				
				game.addDeathzone(id, region);
			}
		}
		
		ConfigurationSection flagsSection = in.getConfigurationSection("flags");
		boolean enableTeamGames = false;
		
		if (flagsSection != null) {
			for (String flagKey : flagsSection.getKeys(false)) {
				ConfigurationSection flagSection = losezonesSection.getConfigurationSection(flagKey);
				String legacyValueString = flagSection.getString("value");
				
				//Queuelobby?!?
				if (flagKey.equals("team")) {
					//The team flag is the only flag that must be handled seperately
					enableTeamGames = extractFlagValue(legacyValueString, Boolean.class, null);
					continue;
				}
				
				AbstractFlag<?> flag = getFlag(flagKey, legacyValueString);
				game.addFlag(flag);
			}
		}
		
		ConfigurationSection teamsSection = in.getConfigurationSection("teams");
		List<TeamColor> colors = Lists.newArrayList();
		int minPlayers = TEAM_FLAG_NOT_SET;
		int maxPlayers = TEAM_FLAG_NOT_SET;
		
		if (teamsSection != null) {
			for (String teamString : teamsSection.getKeys(false)) {
				ConfigurationSection teamSection = teamsSection.getConfigurationSection(teamString);
				String color = teamSection.getString("color");
				int sectionMinPlayers = teamSection.getInt("min-players", TEAM_FLAG_NOT_SET);
				int sectionMaxPlayers = teamSection.getInt("max-players", TEAM_FLAG_NOT_SET);
				
				if (minPlayers == TEAM_FLAG_NOT_SET && sectionMinPlayers != TEAM_FLAG_NOT_SET) {
					minPlayers = sectionMinPlayers;
				}
				
				if (maxPlayers == TEAM_FLAG_NOT_SET && sectionMaxPlayers != TEAM_FLAG_NOT_SET) {
					maxPlayers = sectionMaxPlayers;
				}
				
				color = color.toUpperCase();
				TeamColor teamColor = TeamColor.valueOf(color);
				
				colors.add(teamColor);
			}
		}
		
		//We need at least two teams
		if (enableTeamGames && colors.size() > 1) {
			FlagTeam teamFlag = new FlagTeam();
			teamFlag.setValue(colors);
			game.addFlag(teamFlag);
			
			if (minPlayers != TEAM_FLAG_NOT_SET) {
				FlagMinTeamSize minTeamSizeFlag = new FlagMinTeamSize();
				minTeamSizeFlag.setValue(minPlayers);
				game.addFlag(minTeamSizeFlag);
			}
			
			if (maxPlayers != TEAM_FLAG_NOT_SET) {
				FlagMaxTeamSize maxTeamSizeFlag = new FlagMaxTeamSize();
				maxTeamSizeFlag.setValue(maxPlayers);
				game.addFlag(maxTeamSizeFlag);
			}
		}
		
		ConfigurationSection signwallsSection = in.getConfigurationSection("signwalls");
		
		if (signwallsSection != null) {
			for (String signWallId : signwallsSection.getKeys(false)) {
				ConfigurationSection signwallSection = signwallsSection.getConfigurationSection(signWallId);
				
				Location first = legacyStringToLocation(signwallSection.getString("first"));
				Location second = legacyStringToLocation(signwallSection.getString("second"));
				
				ExtensionLobbyWall wall;
				
				try {
					wall = new ExtensionLobbyWall(first, second);
				} catch (WallValidationException e) {
					throw new MigrationException(e);
				}
				
				game.addExtension(wall);
			}
		}
	}
	
	private Location legacyStringToLocation(String legacyString) {
		String[] components = legacyString.split(",");
		
		World world = Bukkit.getWorld(components[0]);
		double x = Double.parseDouble(components[1]);
		double y = Double.parseDouble(components[2]);
		double z = Double.parseDouble(components[3]);
		float pitch = 0f;
		float yaw = 0f;
		
		if (components.length > 4) {
			pitch = Float.parseFloat(components[4]);
			yaw = Float.parseFloat(components[5]);
		}
		
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	private Vector legacyStringToVector(String legacyString) {
		Location location = legacyStringToLocation(legacyString);
		return BukkitUtil.toVector(location);
	}
	
	@SuppressWarnings("unchecked")
	private <T> AbstractFlag<?> getFlag(String legacyFlagName, String legacyValue) throws MigrationException {
		Class<? extends AbstractFlag<T>> flagClazz = (Class<? extends AbstractFlag<T>>) LEGACY_TO_FLAG_MAPPING.get(legacyFlagName);
		T result = null;
		
		if (BooleanFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, Boolean.class, null);
		} else if (IntegerFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, Integer.class, null);
		} else if (ItemStackFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, ItemStack.class, null);
		} else if (ItemStackListFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, List.class, ItemStack.class);
		} else if (LocationFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, Location.class, null);
		} else if (LocationListFlag.class.isAssignableFrom(flagClazz)) {
			result = (T) extractFlagValue(legacyValue, List.class, Location.class);
		}
		
		AbstractFlag<T> flag;
		
		try {
			flag = flagClazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MigrationException("Cannot find no-args constructor for flag \"" + flagClazz.getName() + "\"");
		}
		
		flag.setValue(result);
		return flag;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private <T, K> T extractFlagValue(String legacyFlagString, Class<T> expected, Class<K> expectedGenericClass) throws MigrationException {
		String[] components = legacyFlagString.split(":");
		Validate.isTrue(components.length > 1, "Invalid legacy flag value string \"" + legacyFlagString + "\"");
		String valueString = components[1];
		
		T value = null;
		
		if (expected == Boolean.class) {
			value = (T)(Boolean) Boolean.parseBoolean(valueString);
		} else if (expected == Integer.class) {
			value = (T)(Integer)Integer.parseInt(valueString);
		} else if (expected == Location.class) {
			value = (T)legacyStringToLocation(valueString);
		} else if (expected == ItemStack.class) {
			String[] itemStackComponents = valueString.split("-");
			
			int id = Integer.parseInt(itemStackComponents[0]);
			byte data = 0;
			int amount = 1;
			
			if (itemStackComponents.length > 1) {
				data = Byte.parseByte(itemStackComponents[1]);
				
				if (itemStackComponents.length > 2) {
					amount = Integer.parseInt(itemStackComponents[2]);
				}
			}
			
			MaterialData materialData = new MaterialData(id, data);
			value = (T) materialData.toItemStack(amount);
		} else if (expected == List.class) {
			String[] listComponents = valueString.split(";");
			List<K> resultList = Lists.newArrayList();
			
			for (int i = 0; i < listComponents.length; i++) {
				String base64SerializedString = listComponents[i];
				byte[] serializedBytes = Base64Coder.decode(base64SerializedString);
				
				Map<String, Object> fields;
				
				try {
					fields = decodeSerializedObject(serializedBytes);
				} catch (IOException e) {
					throw new MigrationException(e);
				}
				
				K result = null;
				
				if (expectedGenericClass == ItemStack.class) {
					EnumObject materialEnumObject = (EnumObject) fields.get("material");
					
					Material material = Material.getMaterial(materialEnumObject.value.value);
					byte data = (byte) fields.get("data");
					int amount = (int) fields.get("amount");
					
					Object displayNameObj = fields.get("displayName");
					String displayName = null;
					
					if (displayNameObj != null) {
						displayName = ((StringObject) displayNameObj).value;
					}
					
					List<String> lore = null;
					//The lore is a type of an instance as it is an ArrayList
					Instance arrayListInstance = (Instance) fields.get("lore");
					
					if (arrayListInstance != null) {
						lore = Lists.newArrayList();
						Map<Field, Object> arrayListFields = arrayListInstance.fielddata.values().iterator().next();
						
						for (Entry<Field, Object> entry : arrayListFields.entrySet()) {
							Field field = entry.getKey();
							Object fieldValue = entry.getValue();
							
							//Let's hope oracle doesn't change the array's field name in the future
							if (!(fieldValue instanceof ArrayObject) || !field.name.equals("a")) {
								ArrayObject array = (ArrayObject) fieldValue;
								ArrayCollection collection = array.data;
								
								for (Object collectionValue : collection) {
									String loreLine = (String) collectionValue;
									lore.add(loreLine);
								}
							}
						}
					}
					
					MaterialData materialData = new MaterialData(material, data);
					ItemStack stack = materialData.toItemStack(amount);
					ItemMeta meta = stack.getItemMeta();
					
					if (displayName != null) {
						meta.setDisplayName(displayName);
					}
					
					if (lore != null) {
						meta.setLore(lore);
					}
					
					stack.setItemMeta(meta);
					result = (K) stack;
				} else if (expectedGenericClass == Location.class) {
					World world = Bukkit.getWorld(((StringObject) fields.get("world")).value);
					double x = (double) fields.get("x");
					double y = (double) fields.get("y");
					double z = (double) fields.get("z");
					
					float pitch = (float) fields.get("pitch");
					float yaw = (float) fields.get("yaw");
					
					Location location = new Location(world, x, y, z, yaw, pitch);
					result = (K) location;
				}
				
				resultList.add(result);
			}
			
			value = (T) resultList;
		}
		
		return value;
	}
	
	private Map<String, Object> decodeSerializedObject(byte[] serialized) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
		DataInputStream dis = new DataInputStream(bais);
		
		jdeserialize.run(dis, false);
		List<Content> contents = jdeserialize.getContent();
		
		if (contents.size() == 0) {
			throw new IOException("No content in serialized byte array (contents.size() == 0)");
		}
		
		Content content = contents.get(0);
		if (!(content instanceof Instance)) {
			throw new IOException("Byte array is not a serialized instance");
		}
		
		Instance instance = (Instance) content;
		Map<ClassDescription, Map<Field, Object>> fieldData = instance.fielddata;
		
		if (fieldData.size() == 0) {
			throw new IOException("Instance does not contain any field data");
		}
		
		Entry<ClassDescription, Map<Field, Object>> entry = fieldData.entrySet().iterator().next();
		Map<Field, Object> fields = entry.getValue();
		Map<String, Object> fieldsResult = Maps.newHashMap();
		
		for (Entry<Field, Object> fieldEntry : fields.entrySet()) {
			fieldsResult.put(fieldEntry.getKey().name, fieldEntry.getValue());
		}
		
		return fieldsResult;
	}

}