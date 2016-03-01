/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.flag.defaults;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.matzefratze123.inventoryguilib.GuiInventory;
import de.matzefratze123.inventoryguilib.GuiInventorySlot;
import de.xaniox.heavyspleef.core.MetadatableItemStack;
import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.flag.*;
import de.xaniox.heavyspleef.core.game.*;
import de.xaniox.heavyspleef.core.game.Game.JoinResult;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.core.stats.Statistic;
import de.xaniox.heavyspleef.core.stats.StatisticRecorder;
import de.xaniox.heavyspleef.flag.presets.EnumListFlag;
import de.xaniox.heavyspleef.flag.presets.LocationFlag;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Flag(name = "team", conflictsWith = {FlagScoreboard.class, FlagMinPlayers.class, FlagMaxPlayers.class})
@BukkitListener
public class FlagTeam extends EnumListFlag<FlagTeam.TeamColor> {
	
	private static final int INVENTORY_SIZE = 9;
	private static final char INFINITY_CHAR = '\u221E';
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
	
	private static final MaterialData TEAM_SELECT_ITEMDATA = new MaterialData(Material.CLAY_BRICK);
	private static final String TEAM_SELECT_ITEM_KEY = "team_select";
	public static final String OBJECTIVE_NAME = "spleef_teams";
	
	private Map<SpleefPlayer, TeamColor> players;
	private Map<SpleefPlayer, TeamColor> deadPlayers;
	private List<TeamColor> deadTeams;
	private Map<TeamColor, Location> spawnpoints;
	private boolean updateInventory;
	private GuiInventory teamChooser;
	private Scoreboard scoreboard;
	private @Inject
    Game game;
	
	public FlagTeam() {
		players = Maps.newHashMap();
		deadPlayers = Maps.newLinkedHashMap();
		deadTeams = Lists.newArrayList();
		spawnpoints = Maps.newHashMap();
		updateInventory = true;
	}
	
	public Map<SpleefPlayer, TeamColor> getPlayers() {
		return players;
	}
	
	public Scoreboard getScoreboard() {
		return scoreboard;
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
	public void validateInput(List<TeamColor> input) throws ValidationException {
		if (input.size() <= 1) {
			throw new ValidationException(getI18N().getString(Messages.Command.AT_LEAST_TWO_TEAMS));
		}
		
		List<TeamColor> existing = Lists.newArrayList();
		for (TeamColor color : input) {
			if (existing.contains(color)) {
				throw new ValidationException(getI18N().getString(Messages.Command.NO_DUPLICATE_TEAMS));
			}
		}
	}

	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables team games in spleef.");
	}
	
	@Override
	public void onFlagAdd(Game game) {
		StatisticRecorder recorder = game.getStatisticRecorder();
		
		if (recorder != null) {
			RatingCompute compute = new TeamRatingCompute();
			recorder.setRatingCompute(compute);
		}
	}
	
	@Override
	public void onFlagRemove(Game game) {
		StatisticRecorder recorder = game.getStatisticRecorder();
		recorder.setRatingCompute(null);
	}
	
	@Override
	public void marshal(Element element) {
		super.marshal(element);
		
		for (Entry<TeamColor, Location> entry : spawnpoints.entrySet()) {
			Element spawnpointElement = element.addElement("spawnpoint");
			TeamColor color = entry.getKey();
			Location location = entry.getValue();
			
			spawnpointElement.addElement("color").addText(color.name());
			Element locationElement = spawnpointElement.addElement("location");
			
			locationElement.addElement("world").addText(String.valueOf(location.getWorld().getName()));
			locationElement.addElement("x").addText(String.valueOf(location.getX()));
			locationElement.addElement("y").addText(String.valueOf(location.getY()));
			locationElement.addElement("z").addText(String.valueOf(location.getZ()));
			locationElement.addElement("yaw").addText(String.valueOf(location.getYaw()));
			locationElement.addElement("pitch").addText(String.valueOf(location.getPitch()));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void unmarshal(Element element) {
		super.unmarshal(element);
		
		List<Element> spawnpointElements = element.elements("spawnpoint");
		for (Element spawnpointElement : spawnpointElements) {
			TeamColor color = TeamColor.valueOf(spawnpointElement.elementText("color"));
			
			Element locationElement = spawnpointElement.element("location");
			
			World world = Bukkit.getWorld(locationElement.elementText("world"));
			double x = Double.parseDouble(locationElement.elementText("x"));
			double y = Double.parseDouble(locationElement.elementText("y"));
			double z = Double.parseDouble(locationElement.elementText("z"));
			float yaw = Float.parseFloat(locationElement.elementText("yaw"));
			float pitch = Float.parseFloat(locationElement.elementText("pitch"));
			
			Location location = new Location(world, x, y, z, yaw, pitch);
			spawnpoints.put(color, location);
		}
	}
	
	private void updateInventory(final Game game) {
		List<Integer> slots = getInventoryLayout();
		GuiInventory inventory = new GuiInventory(getHeavySpleef().getPlugin(), 1, getI18N().getString(Messages.Player.TEAM_SELECTOR_TITLE)) {
			
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
				
				PlayerSelectTeamEvent selectEvent = new PlayerSelectTeamEvent(game, spleefPlayer, color);
				game.getEventBus().callEvent(selectEvent);
				
				if (selectEvent.isCancelled()) {
					spleefPlayer.sendMessage(selectEvent.getFailMessage());
					return;
				}
				
				players.put(spleefPlayer, color);
				spleefPlayer.sendMessage(getI18N().getVarString(Messages.Player.TEAM_CHOOSEN)
						.setVariable("team", color.getChatColor() + getLocalizedColorName(color))
						.toString());
				updateScoreboard();
				updateInventory(game);
				updateViews();
				
				PlayerSelectedTeamEvent selectedEvent = new PlayerSelectedTeamEvent(game, spleefPlayer, color);
				game.getEventBus().callEvent(selectedEvent);
			}
		};
		
		GetMaxPlayersEvent event = new GetMaxPlayersEvent();
		game.getEventBus().callEvent(event);
		
		for (int i = 0; i < size(); i++) {
			int slot = slots.get(i);
			TeamColor color = get(i);
			
			GuiInventorySlot invSlot = inventory.getSlot(slot, 0);
			
			ItemStack teamStack = color.getMaterialWoolData().toItemStack(1);
			ItemMeta meta = teamStack.getItemMeta();
			
			meta.setDisplayName(color.getChatColor() + getLocalizedColorName(color));
			meta.setLore(Lists.newArrayList(ChatColor.YELLOW + "" + sizeOf(color) + ChatColor.AQUA + "/"
					+ (event.getMaxPlayers() != 0 ? String.valueOf(event.getMaxPlayers()) : INFINITY_CHAR)));
			
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
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		SpleefPlayer spleefPlayer = getHeavySpleef().getSpleefPlayer(player);
		
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		if (!players.containsKey(spleefPlayer)) {
			return;
		}
		
		ItemStack clicked = event.getItem();
		if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()) {
			return;
		}
		
		MetadatableItemStack metadatable = new MetadatableItemStack(clicked);
		
		if (!metadatable.hasMetadata(TEAM_SELECT_ITEM_KEY)) {
			return;
		}
		
		teamChooser.open(player);
	}
	
	@Subscribe
	public void onPlayerPreJoinGame(PlayerPreJoinGameEvent event) {
		if (event.getGame().getGameState() == GameState.STARTING) {
			event.setJoinResult(JoinResult.TEMPORARY_DENY);
			event.setMessage(getI18N().getVarString(Messages.Command.GAME_IS_INGAME)
					.setVariable("game", game.getName())
					.toString());
		}
	}
	
	@Subscribe
	public void onGameJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		SpleefPlayer player = event.getPlayer();
		
		if (updateInventory) {
			updateInventory(game);
		}
		
		MetadatableItemStack teamSelectorOpener = new MetadatableItemStack(TEAM_SELECT_ITEMDATA.toItemStack(1));
		ItemMeta meta = teamSelectorOpener.getItemMeta();
		meta.setDisplayName(getI18N().getString(Messages.Player.TEAM_SELECTOR_TITLE));
		meta.setLore(Lists.newArrayList(getI18N().getString(Messages.Player.CLICK_TO_JOIN_TEAM)));
		teamSelectorOpener.setItemMeta(meta);
		teamSelectorOpener.setMetadata(TEAM_SELECT_ITEM_KEY, null);
		
		player.getBukkitPlayer().getInventory().addItem(teamSelectorOpener);
		player.getBukkitPlayer().updateInventory();
		
		players.put(player, null);
		updateScoreboard();
	}
	
	@Subscribe
	public void onGameCountdown(GameCountdownEvent event) {
		List<TeamSizeHolder> teamSizes = Lists.newArrayList();
		for (TeamColor color : getValue()) {
			TeamSizeHolder holder = new TeamSizeHolder();
			holder.color = color;
			holder.size = sizeOf(color);
			teamSizes.add(holder);
		}
		
		Collections.sort(teamSizes);
		
		Map<SpleefPlayer, TeamColor> tempTeamAssigns = Maps.newHashMap();
		for (Entry<SpleefPlayer, TeamColor> entry : players.entrySet()) {
			TeamColor assignedColor = entry.getValue();
			SpleefPlayer player = entry.getKey();
			
			if (assignedColor == null) {
				TeamSizeHolder lowestHolder = teamSizes.get(0);
				assignedColor = lowestHolder.color;
				
				lowestHolder.size++;
				Collections.sort(teamSizes);
			}
			
			tempTeamAssigns.put(player, assignedColor);
		}
		
		//Finally, check if there are enough players to start the game
		ValidateTeamsEvent validateEvent = new ValidateTeamsEvent(teamSizes);
		game.getEventBus().callEvent(validateEvent);
		
		if (validateEvent.isCancelled()) {
			event.setCancelled(true);
			event.setErrorBroadcast(validateEvent.getErrorMessage());
			return;
		}
		
		//Enough players to start the game, set the temporary assigns to real ones
		players = tempTeamAssigns;
		
		//Remove team selection items
		for (SpleefPlayer player : players.keySet()) {
			Inventory inventory = player.getBukkitPlayer().getInventory();
			for (ItemStack stack : inventory.getContents()) {
				if (stack == null) {
					continue;
				}
				
				MetadatableItemStack metaStack = new MetadatableItemStack(stack);
				if (!metaStack.hasItemMeta() || !metaStack.getItemMeta().hasLore() || !metaStack.hasMetadata(TEAM_SELECT_ITEM_KEY)) {
					continue;
				}
				
				inventory.remove(stack);
			}
			
			TeamColor color = players.get(player);
			
			player.getBukkitPlayer().updateInventory();
			player.sendMessage(getI18N().getVarString(Messages.Player.TEAM_CHOOSEN)
					.setVariable("team", color.getChatColor() + getLocalizedColorName(color))
					.toString());
		}
		
		//Determine spawnpoints for teams
		Map<SpleefPlayer, Location> spawnLocationMap = Maps.newHashMap();
		for (Entry<SpleefPlayer, TeamColor> entry : players.entrySet()) {
			SpleefPlayer player = entry.getKey();
			TeamColor color = entry.getValue();
			
			Location spawnpoint = spawnpoints.get(color);
			if (spawnpoint != null) {
				spawnLocationMap.put(player, spawnpoint);
			}
		}
		
		if (!spawnLocationMap.isEmpty()) {
			event.setSpawnLocationMap(spawnLocationMap);
		}
		
		updateScoreboard();
	}
	
	@Subscribe
	public void onPlayerLeave(PlayerLeftGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		TeamColor color = players.get(player);
		
		if (color == null) {
			return;
		}
		
		players.remove(player);
		
		if (event.getGame().getGameState().isGameActive()) {
			deadPlayers.put(player, color);
			
			if (event.getCause() != QuitCause.WIN) {
				int size = sizeOf(color);
				
				if (size <= 0) {
					deadTeams.add(color);
					event.getGame().broadcast(getI18N().getVarString(Messages.Broadcast.TEAM_IS_OUT)
							.setVariable("color", color.getChatColor() + getLocalizedColorName(color))
							.toString());
				} else {
					event.getGame().broadcast(getI18N().getVarString(Messages.Broadcast.TEAMS_LEFT)
							.setVariable("team", color.getChatColor() + getLocalizedColorName(color))
							.setVariable("amount", String.valueOf(size))
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
					
					deadTeams.add(lastColor);
					event.getGame().requestWin(left, false);
					game.broadcast(getI18N().getVarString(Messages.Broadcast.TEAM_WON)
							.setVariable("team", lastColor.getChatColor() + getLocalizedColorName(lastColor))
							.setVariable("game", game.getName())
							.toString());
				}
			}
		}
		
		updateScoreboard(player);
	}
	
	@Subscribe(priority = Subscribe.Priority.HIGH)
	public void onGameEnd(GameEndEvent event) {
		deadPlayers.clear();
		deadTeams.clear();
	}
	
	@Subscribe
	public void onGetScoreboardDisplayName(FlagScoreboard.GetScoreboardDisplayNameEvent event) {
		event.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Team Spleef");
	}
	
	private void updateScoreboard() {
		updateScoreboard(null);
	}
	
	@SuppressWarnings("deprecation")
	private void updateScoreboard(SpleefPlayer left) {
		if (scoreboard == null) {
			scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
			
			for (TeamColor color : getValue()) {
				Team team = scoreboard.registerNewTeam(color.name());
				team.setCanSeeFriendlyInvisibles(true);
				team.setAllowFriendlyFire(false);
				team.setPrefix(color.getChatColor().toString());
			}
			
			TeamScoreboardInitializeEvent event = new TeamScoreboardInitializeEvent();
			game.getEventBus().callEvent(event);
		}
		
		if (left != null) {
			Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
			left.getBukkitPlayer().setScoreboard(mainBoard);
		}
		
		for (Entry<SpleefPlayer, TeamColor> entry : players.entrySet()) {
			SpleefPlayer player = entry.getKey();
			Player bukkitPlayer = player.getBukkitPlayer();
			
			if (bukkitPlayer.getScoreboard() != scoreboard) {
				bukkitPlayer.setScoreboard(scoreboard);
			}
			
			TeamColor color = entry.getValue();
			if (color == null) {
				continue;
			}
			
			Team team = scoreboard.getTeam(color.name());
			if (!team.hasPlayer(bukkitPlayer)) {
				team.addPlayer(bukkitPlayer);
			}
		}
	}
	
	public String getLocalizedColorName(TeamColor color) {
		String[] localizedArray = getI18N().getStringArray(Messages.Arrays.TEAM_COLOR_ARRAY);
		return color.getLocalizedName(localizedArray);
	}
	
	public int sizeOf(TeamColor color) {
		int count = 0;
		for (Entry<SpleefPlayer, TeamColor> entry : players.entrySet()) {
			if (entry.getValue() == color) {
				++count;
			}
		}
		
		return count;
	}
	
	@Flag(name = "spawnpoint", parent = FlagTeam.class)
	public static class FlagTeamSpawnpoint extends LocationFlag {
		
		private TeamColor color;
		
		@Override
		public Location parseInput(SpleefPlayer player, String input) throws InputParseException {
			//(Ab)using a child flag to set spawnpoints in the parent flag
			TeamColor color;
			
			try {
				color = TeamColor.valueOf(input.toUpperCase());
			} catch (Exception e) {
				color = TeamColor.byColorName(getI18N().getStringArray(Messages.Arrays.TEAM_COLOR_ARRAY), input);
			}
			
			if (color == null) {
				throw new InputParseException(getI18N().getVarString(Messages.Command.TEAM_COLOR_NOT_FOUND)
						.setVariable("color", input)
						.toString());
			}
			
			this.color = color;
			return super.parseInput(player, input);
		}
		
		@Override
		public void onFlagAdd(Game game) {
			FlagTeam team = (FlagTeam) getParent();
			team.spawnpoints.put(color, getValue());
			
			game.removeFlag(getClass());
		}
		
		@Override
		public void getDescription(List<String> description) {
			description.add("Sets the spawnpoint for a team");
		}
		
	}
	
	static class GetMaxPlayersEvent extends Event {
		
		private int maxPlayers;
		
		public int getMaxPlayers() {
			return maxPlayers;
		}
		
		public void setMaxPlayers(int maxPlayers) {
			this.maxPlayers = maxPlayers;
		}
		
	}
	
	static class ValidateTeamsEvent extends Event implements Cancellable {
		
		private boolean cancelled;
		private String errorMessage;
		private List<TeamSizeHolder> teams;
		
		public ValidateTeamsEvent(List<TeamSizeHolder> teams) {
			this.teams = teams;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public List<TeamSizeHolder> getTeams() {
			return teams;
		}
		
	}
	
	static class TeamSizeHolder implements Comparable<TeamSizeHolder> {

		private TeamColor color;
		private int size;
		
		public TeamColor getColor() {
			return color;
		}
		
		public int getSize() {
			return size;
		}
		
		@Override
		public int compareTo(TeamSizeHolder o) {
			return Integer.valueOf(size).compareTo(o.size);
		}
		
	}
	
	public static class PlayerSelectedTeamEvent extends PlayerGameEvent {

		private TeamColor colorSelected;
		
		public PlayerSelectedTeamEvent(Game game, SpleefPlayer who, TeamColor color) {
			super(game, who);
			
			this.colorSelected = color;
		}
		
		public TeamColor getColorSelected() {
			return colorSelected;
		}
		
	}
	
	public static class PlayerSelectTeamEvent extends PlayerGameEvent implements Cancellable {

		private boolean cancelled;
		private String failMessage;
		private TeamColor colorSelected;
		
		public PlayerSelectTeamEvent(Game game, SpleefPlayer who, TeamColor color) {
			super(game, who);
			
			this.colorSelected = color;
		}
		
		@Override
		public boolean isCancelled() {
			return cancelled;
		}
		
		@Override
		public void setCancelled(boolean cancel) {
			this.cancelled = cancel;
		}
		
		public String getFailMessage() {
			return failMessage;
		}
		
		public void setFailMessage(String failMessage) {
			this.failMessage = failMessage;
		}
		
		public TeamColor getColorSelected() {
			return colorSelected;
		}
		
	}
	
	public static class TeamScoreboardInitializeEvent extends Event {
		
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
			final int illegalIndex = -1;
			
			int index = illegalIndex;
			for (int i = 0; i < nameArray.length; i++) {
				if (!nameArray[i].equalsIgnoreCase(name)) {
					continue;
				}
				
				index = i;
				break;
			}
			
			if (index == illegalIndex) {
				return null;
			}
			
			return values()[index];
		}
		
	}
	
	private class TeamRatingCompute implements RatingCompute {
		
		@Override
		public RatingResult compute(Map<String, Statistic> statistics, Game game, SpleefPlayer[] winnersArray) {
			Map<TeamColor, Double> ratings = Maps.newHashMap();
			Map<String, Double> results = Maps.newHashMap();
			
			for (TeamColor color : getValue()) {
				Set<SpleefPlayer> subSet = getDeadPlayersByTeam(color);
				if (subSet.isEmpty()) {
					continue;
				}
				
				double averageRating = 0;
				int counter = 0;
				
				for (SpleefPlayer player : subSet) {
					Statistic statistic = statistics.get(player.getName());
					if (statistic == null) {
						continue;
					}
					
					averageRating += statistic.getRating();
					counter++;
				}
				
				averageRating /= counter;
				ratings.put(color, averageRating);
			}
			
			for (Entry<TeamColor, Double> entry : ratings.entrySet()) {
				TeamColor teamColor = entry.getKey();
				
				double expectation = e(ratings, teamColor);
				double score = s(getPlace(teamColor), ratings.size());
				
				double ratingDif = K * (score - expectation);
				
				Set<SpleefPlayer> players = getDeadPlayersByTeam(teamColor);
				for (SpleefPlayer player : players) {
					Statistic statistic = statistics.get(player.getName());
					
					results.put(player.getName(), statistic.getRating() + (ratingDif / players.size()));
				}
			}
			
			return new RatingResult(results);
		}
		
		private Set<SpleefPlayer> getDeadPlayersByTeam(TeamColor color) {
			Set<SpleefPlayer> subSet = Sets.newHashSet();
			for (Entry<SpleefPlayer, TeamColor> entry : deadPlayers.entrySet()) {
				if (entry.getValue() != color) {
					continue;
				}
				
				subSet.add(entry.getKey());
			}
			
			return subSet;
		}
		
		private int getPlace(TeamColor color) {
			for (int i = 0; i < deadTeams.size(); i++) {
				TeamColor deadTeam = deadTeams.get(i);
				
				if (deadTeam == color) {
					return deadTeams.size() - i;
				}
			}
			
			throw new IllegalArgumentException("Dead team list has does not contain a team with the color " + color.name());
		}
		
		private double e(Map<TeamColor, Double> ratings, TeamColor color) {
			double sumE = 0;
			for (Entry<TeamColor, Double> entry : ratings.entrySet()) {
				if (entry.getKey() == color) {
					continue;
				}
				
				double dif = ratings.get(entry.getKey()) - ratings.get(color);
				if (dif > D) {
					dif = D;
				} else if (dif < -D) {
					dif = -D;
				}
				
				sumE += 1D / (1 + Math.pow(10, dif / D));
			}
			
			final int size = ratings.size();
			return sumE / ((size * (size - 1)) / 2);
		}
		
		private double s(int place, int numTeams) {
			return (double) (numTeams - place) / ((numTeams * (numTeams - 1)) / 2);
		}
		
	}

}