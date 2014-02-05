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
package de.matzefratze123.heavyspleef.config.sections;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.config.SpleefConfig;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.selection.SelectionManager.WandType;
import de.matzefratze123.heavyspleef.util.Util;

public class SettingsSectionGeneral implements SettingsSection {
	
	private static final String SECTION_PATH = "general";
	
	private SpleefConfig			configuration;
	private ConfigurationSection	section;

	private int						broadcastRadius;
	private String					prefix;
	private boolean					protectArenas;
	private WandType				wandType;
	private Material				wandItem;
	private List<String>			commandWhitelist;
	private boolean					votesEnabled;
	private int						votesNeeded;
	private SimpleBlockData			readyBlockData;
	private String                  vipPrefix;
	private boolean                 vipCanJoinFull;
	private int						pvpTimer;
	
	public SettingsSectionGeneral(SpleefConfig config) {
		this.configuration = config;
		
		reload();
	}
	
	@Override
	public SpleefConfig getConfig() {
		return configuration;
	}

	@Override
	public ConfigurationSection getSection() {
		return section;
	}

	@Override
	public Object getValue(String path) {
		return section.get(path);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void reload() {
		this.section = configuration.getFileConfiguration().getConfigurationSection(SECTION_PATH);
		
		broadcastRadius = section.getInt("broadcast-radius", 40);
		prefix = ChatColor.translateAlternateColorCodes('&', section.getString("spleef-prefix", "&8[&6&lSpleef&8]"));
		protectArenas = section.getBoolean("protectArena", true);

		String type = section.getString("wandType", HeavySpleef.PLUGIN_NAME);

		if (type.equalsIgnoreCase("WorldEdit")) {
			wandType = WandType.WORLDEDIT;
		} else {
			wandType = WandType.HEAVYSPLEEF;
		}

		final Material defaultMaterial = Material.STICK;
		String configString = section.getString("wandItem");
		
		try {
			wandItem = Material.valueOf(configString.toUpperCase());
		} catch (Exception e) {
			// Not a material string
			if (Util.isNumber(configString)) {
				wandItem = Material.getMaterial(Integer.parseInt(configString));
			} else {
				// All failed, use default
				wandItem = defaultMaterial;
			}
		}

		commandWhitelist = section.getStringList("commandWhitelist");
		if (commandWhitelist == null)
			commandWhitelist = new ArrayList<String>();

		votesEnabled = section.getBoolean("autostart-vote-enabled", true);
		votesNeeded = section.getInt("autostart-vote", 70);

		String blockDataString = section.getString("ready-block");
		readyBlockData = Util.parseMaterial(blockDataString, false);
		if (readyBlockData == null) {
			readyBlockData = new SimpleBlockData(Material.IRON_BLOCK, (byte) 0);
		}

		vipPrefix = ChatColor.translateAlternateColorCodes('&', section.getString("vip-prefix", "&4"));
		vipCanJoinFull = section.getBoolean("vip-join-full");

		pvpTimer = section.getInt("pvptimer");
	}

	public int getBroadcastRadius() {
		return broadcastRadius;
	}

	public String getPrefix() {
		return prefix;
	}

	public boolean isProtectArenas() {
		return protectArenas;
	}

	public WandType getWandType() {
		return wandType;
	}

	public Material getWandItem() {
		return wandItem;
	}

	public List<String> getCommandWhitelist() {
		return commandWhitelist;
	}

	public boolean isVotesEnabled() {
		return votesEnabled;
	}

	public int getVotesNeeded() {
		return votesNeeded;
	}

	public SimpleBlockData getReadyBlockData() {
		return readyBlockData;
	}

	public String getVipPrefix() {
		return vipPrefix;
	}
	
	public boolean getVipJoinFull() {
		return vipCanJoinFull;
	}

	public int getPvPTimer() {
		return pvpTimer;
	}

}
