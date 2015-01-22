package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent.JoinResult;
import de.matzefratze123.heavyspleef.core.event.PlayerLoseGameEvent;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.EnumListFlag;

public class FlagTeam extends EnumListFlag<FlagTeam.TeamColor> {
	
	private static final List<Character> SKIP_CHARS = Lists.newArrayList('-', '_');
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
			event.setMessage(null); //TODO: Add localized message
			return;
		}
		
		String team = joinArgs[0];
		TeamColor color = TeamColor.byColorName(team);
		
		if (color == null) {
			event.setJoinResult(JoinResult.DENY);
			event.setMessage(null); //TODO: Also add localized message
			return;
		}
		
		FlagMaxTeamSize maxSizeFlag = getChildFlag(FlagMaxTeamSize.class, game);
		if (maxSizeFlag != null) {
			int maxSize = maxSizeFlag.getValue();
			int size = size(color);
			
			if (size >= maxSize) {
				event.setJoinResult(JoinResult.DENY);
				event.setMessage(null); //TODO: Add localized deny message
				return;
			}
		}
		
		//TODO Also add an allow message?
		event.setMessage(null);
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
			//TODO Team is out message
			event.getGame().broadcast(null);
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
		
		public static TeamColor byColorName(String color) {
			for (TeamColor col : values()) {
				String colName = col.name();
				
				int nameIndex = 0;
				int strIndex = 0;
				
				boolean isMatching = true;
				
				do {
					if (nameIndex >= colName.length() || strIndex >= color.length()) {
						break;
					}
					
					char nameChar = Character.toLowerCase(colName.charAt(nameIndex));
					char strChar = Character.toLowerCase(color.charAt(strIndex));
					boolean skip = false;
					
					if (SKIP_CHARS.contains(nameChar)) {
						nameIndex++;
						skip = true;
					}
					
					if (SKIP_CHARS.contains(strChar)) {
						strIndex++;
						skip = true;
					}
					
					if (skip) {
						continue;
					}
					
					isMatching = nameChar == strChar;
					strIndex++;
					nameIndex++;
				} while (isMatching);
				
				if (isMatching) {
					return col;
				}
			}
			
			return null;
		}
		
	}

}
