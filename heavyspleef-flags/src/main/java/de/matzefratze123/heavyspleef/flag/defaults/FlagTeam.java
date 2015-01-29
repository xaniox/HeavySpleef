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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent.JoinResult;
import de.matzefratze123.heavyspleef.core.event.PlayerLoseGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.EnumListFlag;

@Flag(name = "team")
public class FlagTeam extends EnumListFlag<FlagTeam.TeamColor> {
	
	private static final MaterialData LEATHER_HELMET_DATA = new MaterialData(Material.LEATHER_HELMET);
	private static final MaterialData LEATHER_CHESTPLATE_DATA = new MaterialData(Material.LEATHER_CHESTPLATE);
	private static final MaterialData LEATHER_LEGGINGS_DATA = new MaterialData(Material.LEATHER_LEGGINGS);
	private static final MaterialData LEATHER_BOOTS_DATA = new MaterialData(Material.LEATHER_BOOTS);
	
	private Map<SpleefPlayer, TeamColor> players;
	
	public FlagTeam() {
		players = Maps.newHashMap();
	}
	
	@Override
	public Class<TeamColor> getEnumType() {
		return TeamColor.class;
	}

	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public boolean hasGameProperties() {
		return false;
	}

	@Override
	public boolean hasBukkitListenerMethods() {
		return false;
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables team games in spleef.");
	}
	
	@GameListener
	public void onGameJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		SpleefPlayer player = event.getPlayer();
		String[] joinArgs = event.joinArgs();
		
		//TODO: Use an unique index for team games as other flags may
		//      also want to use joinArgs
		if (joinArgs.length < 1) {
			event.setJoinResult(JoinResult.DENY);
			event.setMessage(getI18N().getVarString(Messages.Player.SPECIFY_TEAM_COLOR_REQUEST)
					.setVariable("available-colors", getLocalizedStringArray(ChatColor.GRAY))
					.toString());
			return;
		}
		
		String team = joinArgs[0];
		String[] localizedColorNames = getI18N().getStringArray(Messages.Arrays.TEAM_COLOR_ARRAY);
		TeamColor color = TeamColor.byColorName(localizedColorNames, team);
		
		if (color == null) {
			event.setJoinResult(JoinResult.DENY);
			event.setMessage(getI18N().getVarString(Messages.Player.TEAM_COLOR_NOT_AVAILABLE)
					.setVariable("color", getLocalizedColorName(color))
					.setVariable("available-colors", getLocalizedStringArray(ChatColor.GRAY))
					.toString());
			return;
		}
		
		FlagMaxTeamSize maxSizeFlag = getChildFlag(FlagMaxTeamSize.class, game);
		if (maxSizeFlag != null) {
			int maxSize = maxSizeFlag.getValue();
			int size = size(color);
			
			if (size >= maxSize) {
				event.setJoinResult(JoinResult.DENY);
				event.setMessage(getI18N().getString(Messages.Player.TEAM_MAX_PLAYER_COUNT_REACHED));
				return;
			}
		}
		
		event.setMessage(getI18N().getVarString(Messages.Player.PLAYER_JOINED_TEAM)
				.setVariable("color", getLocalizedColorName(color))
				.toString());
		players.put(player, color);
	}
	
	@GameListener
	public void onGameStart(GameStartEvent event) {
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			if (!players.containsKey(player)) {
				continue;
			}
			
			TeamColor color = players.get(player);
			
			ItemStack leatherHelmet = LEATHER_HELMET_DATA.toItemStack(1);
			ItemStack leatherChestplate = LEATHER_CHESTPLATE_DATA.toItemStack(1);
			ItemStack leatherLeggings = LEATHER_LEGGINGS_DATA.toItemStack(1);
			ItemStack leatherBoots = LEATHER_BOOTS_DATA.toItemStack(1);
			
			LeatherArmorMeta meta = (LeatherArmorMeta) leatherHelmet.getItemMeta();
			meta.setColor(color.getRGB());
			
			leatherHelmet.setItemMeta(meta);
			leatherChestplate.setItemMeta(meta);
			leatherLeggings.setItemMeta(meta);
			leatherBoots.setItemMeta(meta);
			
			PlayerInventory inventory = player.getBukkitPlayer().getInventory();
			inventory.setHelmet(leatherHelmet);
			inventory.setChestplate(leatherChestplate);
			inventory.setLeggings(leatherLeggings);
			inventory.setBoots(leatherBoots);
		}
	}
	
	@GameListener
	public void onPlayerLose(PlayerLoseGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		TeamColor color = players.get(player);
		
		if (color == null) {
			return;
		}
		
		players.remove(player);
		int size = size(color);
		
		if (size <= 0) {
			event.getGame().broadcast(getI18N().getVarString(Messages.Broadcast.TEAM_IS_OUT)
					.setVariable("color", getLocalizedColorName(color))
					.toString());
		}
		
		TeamColor lastColor = null;
		boolean oneTeamLeft = true;
		for (TeamColor teamColor : players.values()) {
			if (lastColor == null) {
				lastColor = teamColor;
				continue;
			}
			
			if (lastColor != teamColor) {
				oneTeamLeft = false;
			}
		}
		
		if (oneTeamLeft) {
			Set<SpleefPlayer> leftSet = players.keySet();
			SpleefPlayer[] left = leftSet.toArray(new SpleefPlayer[leftSet.size()]);
			
			event.getGame().requestWin(left);
		}
	}
	
	private String getLocalizedColorName(TeamColor color) {
		String[] localizedArray = getI18N().getStringArray(Messages.Arrays.TEAM_COLOR_ARRAY);
		return color.getLocalizedName(localizedArray);
	}
	
	private String getLocalizedStringArray(ChatColor delimiterColor) {
		StringBuilder builder = new StringBuilder();
		String[] localizedArray = getI18N().getStringArray(Messages.Arrays.TEAM_COLOR_ARRAY);
		
		for (int i = 0; i < localizedArray.length; i++) {
			String colorName = localizedArray[i];
			TeamColor color = TeamColor.byColorName(localizedArray, colorName);
			
			builder.append(color.getChatColor())
				.append(colorName);
			
			if (i + 1 < localizedArray.length) {
				builder.append(delimiterColor)
					.append(", ");
			}
		}
		
		return builder.toString();
	}
	
	public int size(TeamColor color) {
		int count = 0;
		for (Entry<SpleefPlayer, TeamColor> entry : players.entrySet()) {
			if (entry.getValue() == color) {
				++count;
			}
		}
		
		return count;
	}
	
	public enum TeamColor {
		
		WHITE(ChatColor.WHITE, 0x0, Color.WHITE),
		GOLD(ChatColor.GOLD, 0x1, Color.fromRGB(0xFFD700)),
		MAGENTA(ChatColor.DARK_PURPLE, 0x2, Color.fromRGB(0xE600FF)),
		LIGHT_BLUE(ChatColor.AQUA, 0x3, Color.fromRGB(0x00E6FF)),
		YELLOW(ChatColor.YELLOW, 0x4, Color.YELLOW),
		LIGHT_GREEN(ChatColor.GREEN, 0x5, Color.LIME),
		PINK(ChatColor.LIGHT_PURPLE, 0x6, Color.fromRGB(0xFF66CC)),
		GRAY(ChatColor.GRAY, 0x7, Color.GRAY),
		BLUE(ChatColor.BLUE, 0xB, Color.BLUE),
		GREEN(ChatColor.DARK_GREEN, 0xD, Color.GREEN),
		RED(ChatColor.RED, 0xE, Color.RED);
		
		private ChatColor chatColor;
		private byte legacyWoolData;
		private Color rgbColor;
		
		private TeamColor(ChatColor chatColor, int legacyWoolData, Color rgbColor) {
			this.chatColor = chatColor;
			this.legacyWoolData = (byte) legacyWoolData;
			this.rgbColor = rgbColor;
		}
		
		public ChatColor getChatColor() {
			return chatColor;
		}
		
		public byte getLegacyWoolData() {
			return legacyWoolData;
		}
		
		public Color getRGB() {
			return rgbColor;
		}
		
		public String getLocalizedName(String[] nameArray) {
			//Important: Keep the order of the array like the enum order
			return nameArray[ordinal()];
		}
		
		
		public static TeamColor byColorName(String[] nameArray, String name) {
			int index = -1;
			for (int i = 0; i < nameArray.length; i++) {
				if (!nameArray[i].equalsIgnoreCase(name)) {
					continue;
				}
				
				index = i;
				break;
			}
			
			return values()[index];
		}
		
	}

}
