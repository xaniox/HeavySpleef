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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.MetadatableItemStack;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLoseGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent.JoinResult;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.EnumListFlag;
import de.matzefratze123.inventoryguilib.GuiInventory;
import de.matzefratze123.inventoryguilib.GuiInventorySlot;

@Flag(name = "team")
@BukkitListener
public class FlagTeam extends EnumListFlag<FlagTeam.TeamColor> {
	
	private static final int INVENTORY_SIZE = 9;
	private static final char INFINITY_CHAR = '∞';
	private static final int[][] INVENTORY_LAYOUTS = {
		{2},
		{1},
		{1, 3},
		{0, 2},
		{0, 2, 3},
		{0, 1, 3},
		{0, 1, 2, 3},
		{0, 1, 2, 3},
	};
	private static final int OFFSET = 2;
	
	private static final MaterialData LEATHER_HELMET_DATA = new MaterialData(Material.LEATHER_HELMET);
	private static final MaterialData LEATHER_CHESTPLATE_DATA = new MaterialData(Material.LEATHER_CHESTPLATE);
	private static final MaterialData LEATHER_LEGGINGS_DATA = new MaterialData(Material.LEATHER_LEGGINGS);
	private static final MaterialData LEATHER_BOOTS_DATA = new MaterialData(Material.LEATHER_BOOTS);
	private static final MaterialData TEAM_SELECT_ITEMDATA = new MaterialData(Material.CLAY_BRICK);
	
	private static final String TEAM_SELECT_ITEM_KEY = "team_select";
	
	private Map<SpleefPlayer, TeamColor> players;
	private boolean updateInventory;
	private GuiInventory teamChooser;
	
	public FlagTeam() {
		players = Maps.newHashMap();
		updateInventory = true;
	}
	
	@Override
	public void setValue(List<TeamColor> value) {
		super.setValue(value);
		
		updateInventory = true;
	}
	
	@Override
	public Class<TeamColor> getEnumType() {
		return TeamColor.class;
	}

	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables team games in spleef.");
	}
	
	private void updateInventory(Game game) {
		List<Integer> slots = getInventoryLayout();
		GuiInventory inventory = new GuiInventory(getHeavySpleef().getPlugin(), 1, "Team selection") { //TODO Add message
			
			@Override
			public void onClick(GuiClickEvent event) {
				Player player = event.getPlayer();
				SpleefPlayer spleefPlayer = getHeavySpleef().getSpleefPlayer(player);
				GuiInventorySlot slot = event.getSlot();
				
				event.setCancelled(true);
				
				TeamColor color = (TeamColor) slot.getValue();
				if (color == null) {
					return;
				}
				
				players.put(spleefPlayer, color);
			}
		};
		
		FlagMaxTeamSize maxTeamSizeFlag = getChildFlag(FlagMaxTeamSize.class, game);
		
		for (int i = 0; i < size(); i++) {
			int slot = slots.get(i);
			TeamColor color = get(i);
			
			GuiInventorySlot invSlot = inventory.getSlot(slot, 0);
			
			ItemStack teamStack = color.getMaterialWoolData().toItemStack(1);
			ItemMeta meta = teamStack.getItemMeta();
			
			meta.setDisplayName(color.getChatColor() + getLocalizedColorName(color));
			meta.setLore(Lists.newArrayList(ChatColor.YELLOW + "" + size(color) + ChatColor.AQUA + "/" + (maxTeamSizeFlag != null ? maxTeamSizeFlag.getValue() : INFINITY_CHAR)));
			
			teamStack.setItemMeta(meta);
			invSlot.setItem(teamStack);
			invSlot.setValue(color);
		}
		
		teamChooser = inventory;
	}
	
	private List<Integer> getInventoryLayout() {
		int size = size();
		List<Integer> slots = Lists.newArrayList();
		
		if (size % 2 != 0) {
			slots.add(4);
		}
		
		for (int slot : INVENTORY_LAYOUTS[size - OFFSET]) {
			int reflectedSlot = INVENTORY_SIZE - 1 - slot;
			
			slots.add(slot);
			slots.add(reflectedSlot);
		}
		
		return slots;
	}
	
	@GameListener
	public void onPreGameJoin(PlayerPreJoinGameEvent event) {
		FlagMaxTeamSize maxSizeFlag = getChildFlag(FlagMaxTeamSize.class, event.getGame());
		if (maxSizeFlag != null) {
			int maxSize = maxSizeFlag.getValue();
			
			if (players.size() >= maxSize * size()) {
				event.setJoinResult(JoinResult.DENY);
				event.setMessage(getI18N().getString(Messages.Player.TEAM_MAX_PLAYER_COUNT_REACHED));
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		SpleefPlayer spleefPlayer = getHeavySpleef().getSpleefPlayer(player);
		
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		if (players.containsKey(spleefPlayer)) {
			return;
		}
		
		ItemStack clicked = event.getItem();
		MetadatableItemStack metadatable = new MetadatableItemStack(clicked);
		
		if (!metadatable.hasMetadata(TEAM_SELECT_ITEM_KEY)) {
			return;
		}
		
		teamChooser.open(player);
	}
	
	@SuppressWarnings("deprecation")
	@GameListener
	public void onGameJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		SpleefPlayer player = event.getPlayer();
		
		if (updateInventory) {
			updateInventory(game);
		}
		
		MetadatableItemStack teamSelectorOpener = new MetadatableItemStack(TEAM_SELECT_ITEMDATA.toItemStack(1));
		ItemMeta meta = teamSelectorOpener.getItemMeta();
		meta.setDisplayName("Team selector"); //TODO Add localized message
		meta.setLore(Lists.newArrayList("Right-Click to choose your team")); //TODO Add localized message
		teamSelectorOpener.setItemMeta(meta);
		teamSelectorOpener.setMetadata(TEAM_SELECT_ITEM_KEY, null);
		
		player.getBukkitPlayer().getInventory().addItem(teamSelectorOpener);
		player.getBukkitPlayer().updateInventory();
		
		players.put(player, null);
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
		YELLOW(ChatColor.YELLOW, 0x4, Color.YELLOW),
		GREEN(ChatColor.GREEN, 0x5, Color.LIME),
		PINK(ChatColor.LIGHT_PURPLE, 0x6, Color.fromRGB(0xFF66CC)),
		GRAY(ChatColor.GRAY, 0x7, Color.GRAY),
		BLUE(ChatColor.BLUE, 0xB, Color.BLUE),
		RED(ChatColor.RED, 0xE, Color.RED);
		
		private ChatColor chatColor;
		private byte legacyWoolData;
		private Color rgbColor;
		private MaterialData woolData;
		
		@SuppressWarnings("deprecation")
		private TeamColor(ChatColor chatColor, int legacyWoolData, Color rgbColor) {
			this.chatColor = chatColor;
			this.legacyWoolData = (byte) legacyWoolData;
			this.rgbColor = rgbColor;
			this.woolData = new MaterialData(Material.WOOL, (byte)legacyWoolData);
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
		
		public MaterialData getMaterialWoolData() {
			return woolData;
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
