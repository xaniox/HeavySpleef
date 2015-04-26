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

import static de.matzefratze123.heavyspleef.core.HeavySpleef.PREFIX;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.core.FlagManager.DefaultGamePropertyBundle;
import de.matzefratze123.heavyspleef.core.FlagManager.GamePropertyBundle;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.event.EventManager;
import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.GameDisableEvent;
import de.matzefratze123.heavyspleef.core.event.GameEnableEvent;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.GameStateChangeEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerBlockBreakEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerBlockPlaceEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerInteractGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent.JoinResult;
import de.matzefratze123.heavyspleef.core.event.PlayerQueueFlushEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerQueueFlushEvent.FlushResult;
import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.event.SpleefListener;
import de.matzefratze123.heavyspleef.core.extension.ExtensionManager;
import de.matzefratze123.heavyspleef.core.extension.GameExtension;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.hook.WorldEditHook;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.PlayerStateHolder;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class Game {
	
	private static final int NO_BLOCK_LIMIT = -1;
	private static final int DEFAULT_COUNTDOWN = 10;
	private static final Map<Class<? extends Region>, SpawnpointGenerator<?>> SPAWNPOINT_GENERATORS;
	
	static {
		SPAWNPOINT_GENERATORS = Maps.newHashMap();
		
		SPAWNPOINT_GENERATORS.put(CuboidRegion.class, new CuboidSpawnpointGenerator());
		SPAWNPOINT_GENERATORS.put(CylinderRegion.class, new CylinderSpawnpointGenerator());
		SPAWNPOINT_GENERATORS.put(Polygonal2DRegion.class, new Polygonal2DSpawnpointGenerator());
	}
	
	private final I18N i18n = I18N.getInstance();
	
	private final EditSessionFactory editSessionFactory;
	@Getter
	private HeavySpleef heavySpleef;
	private EventManager eventManager;
	private Set<SpleefPlayer> ingamePlayers;
	@Getter
	private List<SpleefPlayer> deadPlayers;
	@Getter
	private BiMap<SpleefPlayer, Set<Block>> blocksBroken;
	private KillDetector killDetector;
	private Queue<SpleefPlayer> queuedPlayers;
	@Getter
	private CountdownTask countdownTask;
	@Getter
	private StatisticRecorder statisticRecorder;
	
	@Getter @Setter(value = AccessLevel.PACKAGE)
	private String name;
	@Getter
	private World world;
	private com.sk89q.worldedit.world.World worldEditWorld;
	@Getter
	private FlagManager flagManager;
	private ExtensionManager extensionManager;
	@Getter
	private GameState gameState;
	private Map<String, Floor> floors;
	@Getter
	private Map<String, Region> deathzones;
	
	public Game(HeavySpleef heavySpleef, String name, World world) {
		this.heavySpleef = heavySpleef;
		this.name = name;
		this.world = world;
		this.worldEditWorld = new BukkitWorld(world);
		this.ingamePlayers = Sets.newLinkedHashSet();
		this.deadPlayers = Lists.newArrayList();
		this.eventManager = new EventManager(heavySpleef.getLogger());
		this.statisticRecorder = new StatisticRecorder(heavySpleef.getDatabaseHandler(), heavySpleef.getLogger());
		
		eventManager.registerListener(statisticRecorder);
		setGameState(GameState.WAITING);
		
		DefaultConfig configuration = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
		GamePropertyBundle defaults = new DefaultGamePropertyBundle(configuration.getProperties());
		
		this.flagManager = new FlagManager(heavySpleef.getPlugin(), defaults);
		this.extensionManager = heavySpleef.getExtensionRegistry().newManagerInstance(eventManager);
		this.deathzones = Maps.newHashMap();
		this.blocksBroken = HashBiMap.create();
		this.killDetector = new DefaultKillDetector();
		this.queuedPlayers = new LinkedList<SpleefPlayer>();
		
		//Concurrent map for database schematics
		this.floors = new ConcurrentHashMap<String, Floor>();
		
		WorldEditHook hook = (WorldEditHook) heavySpleef.getHookManager().getHook(HookReference.WORLDEDIT);
		WorldEdit worldEdit = hook.getWorldEdit();
		
		this.editSessionFactory = worldEdit.getEditSessionFactory();
	}
	
	public void setHeavySpleef(HeavySpleef heavySpleef) {
		Validate.notNull(heavySpleef, "HeavySpleef instance cannot be null");
		this.heavySpleef = heavySpleef;
	}
	
	public boolean countdown() {
		GameCountdownEvent event = new GameCountdownEvent(this);
		eventManager.callEvent(event);
		
		// The player cannot play alone
		if (ingamePlayers.size() <= 1) {
			broadcast(i18n.getVarString(Messages.Player.NEED_MIN_PLAYERS)
					.setVariable("amount", String.valueOf(2))
					.toString());
			return false;
		}
		
		// Spleef cannot be started when there is no floor
		if (floors.size() == 0) {
			broadcast(i18n.getString(Messages.Broadcast.NEED_FLOORS));
			return false;
		}
		
		EditSession editSession = editSessionFactory.getEditSession(worldEditWorld, NO_BLOCK_LIMIT);
		
		// Regenerate all floors
		for (Floor floor : floors.values()) {
			floor.generate(editSession);
		}
		
		List<Location> spawnLocations = event.getSpawnLocations();
		if (spawnLocations == null) {
			spawnLocations = Lists.newArrayList();
			
			// Generate a random spawnpoint
			Floor topFloor = null;
			for (Floor floor : floors.values()) {
				if (topFloor == null || floor.getRegion().getMaximumPoint().getBlockY() > topFloor.getRegion().getMaximumPoint().getBlockY()) {
					topFloor = floor;
				}
			}
			
			Region region = topFloor.getRegion();
			
			generateSpawnpoints(region, spawnLocations, ingamePlayers.size());
		}
		
		int locIndex = 0;
		for (SpleefPlayer player : ingamePlayers) {
			Location loc = spawnLocations.get(locIndex);
			
			player.getBukkitPlayer().teleport(loc);
			
			locIndex = locIndex + 1 >= spawnLocations.size() ? 0 : ++locIndex;
		}
		
		boolean countdownEnabled = event.isCountdownEnabled();
		int countdownLength = event.getCountdownLength();
		
		if (countdownLength <= 0) {
			countdownLength = DEFAULT_COUNTDOWN;
		}
		
		setGameState(GameState.STARTING);
		
		if (countdownEnabled && countdownLength > 0) {
			countdownTask = new CountdownTask(heavySpleef.getPlugin(), countdownLength, new CountdownTask.CountdownCallback() {
				
				@Override
				public void onCountdownFinish(CountdownTask task) {
					start();
					
					countdownTask = null;
				}
				
				@Override
				public void onCountdownCount(CountdownTask task) {
					broadcast(BroadcastTarget.INGAME, i18n.getVarString(Messages.Broadcast.GAME_COUNTDOWN_MESSAGE)
						.setVariable("remaining", String.valueOf(task.getRemaining()))
						.toString());
				}
			});
			
			countdownTask.start();
		} else {
			//Countdown is not enabled so just start the game
			start();
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Region> void generateSpawnpoints(T region, List<Location> spawnpoints, int n) {
		World world = Bukkit.getWorld(region.getWorld().getName());
		
		SpawnpointGenerator<T> generator = (SpawnpointGenerator<T>) SPAWNPOINT_GENERATORS.get(region.getClass());
		generator.generateSpawnpoints(region, world, spawnpoints, n);
	}
	
	public void start() {
		GameStartEvent event = new GameStartEvent(this);
		eventManager.callEvent(event);
		
		setGameState(GameState.INGAME);
		broadcast(i18n.getVarString(Messages.Broadcast.GAME_STARTED)
				.setVariable("game", name)
				.setVariable("count", String.valueOf(ingamePlayers.size()))
				.toString());
	}
	
	public void stop() {
		//Create a copy of current ingame players to prevent
		//a ConcurrentModificationException
		Set<SpleefPlayer> ingamePlayersCopy = Sets.newHashSet(ingamePlayers);
		for (SpleefPlayer player : ingamePlayersCopy) {
			leave(player, QuitCause.STOP);
		}
		
		resetGame();
		
		GameEndEvent event = new GameEndEvent(this);
		eventManager.callEvent(event);
		
		broadcast(i18n.getString(Messages.Broadcast.GAME_STOPPED));
	}
	
	private void resetGame() {
		EditSession editSession = editSessionFactory.getEditSession(worldEditWorld, NO_BLOCK_LIMIT);
		for (Floor floor : floors.values()) {
			floor.generate(editSession);
		}
		
		Queue<SpleefPlayer> failedToQueue = Lists.newLinkedList();
		
		//Flush the queue
		while (!queuedPlayers.isEmpty()) {
			SpleefPlayer player = queuedPlayers.poll();
			
			PlayerQueueFlushEvent event = new PlayerQueueFlushEvent(this, player);
			eventManager.callEvent(event);
			
			FlushResult result = event.getResult();
			if (result == FlushResult.ALLOW) {				
				PlayerPreJoinGameEvent joinEvent = new PlayerPreJoinGameEvent(this, player, new String[0]); //TODO Store join args in queue?
				eventManager.callEvent(joinEvent);
				
				if (joinEvent.getJoinResult() != JoinResult.DENY) {
					join(player, joinEvent, (String[])null);
					continue;
				}
			}
			
			failedToQueue.offer(player);
		}
		
		queuedPlayers.addAll(failedToQueue);
		blocksBroken.clear();
		deadPlayers.clear();
		setGameState(GameState.WAITING);
		
		//Stop the countdown if necessary
		if (countdownTask != null) {
			countdownTask.cancel();
			countdownTask = null;
		}
	}
	
	public void disable() {
		if (gameState == GameState.DISABLED) {
			return;
		}
		
		if (gameState.isGameActive()) {
			//Stop this game before disabling it
			stop();
		} else if (gameState == GameState.LOBBY) {
			//Create a copy of current ingame players to prevent
			//a ConcurrentModificationException
			Set<SpleefPlayer> ingamePlayersCopy = Sets.newHashSet(ingamePlayers);
			for (SpleefPlayer player : ingamePlayersCopy) {
				leave(player);
			}
		}
		
		setGameState(GameState.DISABLED);
		
		GameDisableEvent event = new GameDisableEvent(this);
		eventManager.callEvent(event);
	}
	
	public void enable() {
		if (gameState.isGameEnabled()) {
			return;
		}
		
		setGameState(GameState.WAITING);
		
		GameEnableEvent event = new GameEnableEvent(this);
		eventManager.callEvent(event);
	}
	
	public void join(SpleefPlayer player) {
		join(player, (String[]) null);
	}
	
	public void join(SpleefPlayer player, String... args) {
		join(player, null, args);
	}
	
	private void join(SpleefPlayer player, PlayerPreJoinGameEvent event, String... args) {
		if (ingamePlayers.contains(player)) {
			return;
		}
		
		if (event == null) {
			//Only call this event if the caller didn't give us an event
			//This event is called before the player joins the game!
			event = new PlayerPreJoinGameEvent(this, player, args == null ? new String[0] : args);		
			eventManager.callEvent(event);
		}
		
		if (event.getTeleportationLocation() == null) {
			player.sendMessage(i18n.getString(Messages.Player.ERROR_NO_LOBBY_POINT_SET));
			return;
		}
		
		JoinResult result = event.getJoinResult();
		switch (result) {
		case ALLOW:
			//Go through
			break;
		case DENY:
			String denyMessage = event.getMessage();
			if (denyMessage != null) {
				player.sendMessage(PREFIX + denyMessage);
			}
			
			return;
		case NOT_SPECIFIED:
			//Do a state check
			if (gameState == GameState.INGAME) {
				return;
			}
			break;
		default:
			break;
		}
		
		ingamePlayers.add(player);
		
		if (gameState == GameState.WAITING) {
			setGameState(GameState.LOBBY);
		}
		
		//Store a reference to the player state
		player.savePlayerState(this);
		PlayerStateHolder.applyDefaultState(player.getBukkitPlayer());
		
		Location location = event.getTeleportationLocation();
		player.teleport(location);
		
		//This event is called when the player actually joins the game
		PlayerJoinGameEvent joinGameEvent = new PlayerJoinGameEvent(this, player);
		eventManager.callEvent(joinGameEvent);
		
		broadcast(i18n.getVarString(Messages.Broadcast.PLAYER_JOINED_GAME)
				.setVariable("player", player.getName())
				.toString());
		
		if (event.getMessage() != null) {
			player.sendMessage(PREFIX + event.getMessage());
		}
		
		if (joinGameEvent.getStartGame()) {
			countdown();
		}
	}
	
	public void queue(SpleefPlayer player) {
		queuedPlayers.add(player);
	}
	
	public void unqueue(SpleefPlayer player) {
		queuedPlayers.remove(player);
	}
	
	public void leave(SpleefPlayer player) {
		leave(player, QuitCause.SELF);
	}
	
	public void leave(SpleefPlayer player, QuitCause cause, Object... args) {
		if (!ingamePlayers.contains(player)) {
			return;
		}
		
		ingamePlayers.remove(player);
		
		if (gameState == GameState.INGAME) {
			deadPlayers.add(player);
		}
		
		SpleefPlayer killer = null;
		if (cause == QuitCause.LOSE && args.length > 0 && args[0] != null && args[0] instanceof SpleefPlayer) {
			killer = (SpleefPlayer) args[0];
		}
		
		PlayerLeaveGameEvent event = new PlayerLeaveGameEvent(this, player, killer, cause);
		eventManager.callEvent(event);
		
		if (event.isCancelled()) {
			//Add the player again...
			ingamePlayers.add(player);
			deadPlayers.remove(player);
			return;
		}
		
		Location tpLoc = event.getTeleportationLocation();
		
		//Receive the state back
		PlayerStateHolder playerState = player.getPlayerState(this);
		if (playerState != null) {
			playerState.apply(player.getBukkitPlayer(), tpLoc == null);
		} else {
			//Ugh, something went wrong
			player.sendMessage(i18n.getString(Messages.Player.ERROR_ON_INVENTORY_LOAD));
		}
		
		if (tpLoc != null) {
			player.getBukkitPlayer().teleport(tpLoc);
		}
		
		if (ingamePlayers.size() == 0 && gameState == GameState.LOBBY) {
			setGameState(GameState.WAITING);
		}
		
		String broadcastMessage = i18n.getVarString(Messages.Broadcast.PLAYER_LEFT_GAME)
				.setVariable("player", player.getName())
				.toString();
		String playerMessage = i18n.getString(Messages.Player.PLAYER_LEAVE);
		
		switch (cause) {
		case KICK:
			CommandSender clientPlayer = null;
			String message = null;
			
			int messageIndex = 0;
			if (args.length > 0 && args[0] instanceof CommandSender) {
				// Caller gave us a client player
				clientPlayer = (CommandSender) args[0];
				messageIndex = 1;
			}
			
			if (args.length > messageIndex && args[messageIndex] instanceof String) {
				// Caller gave us a kick message
				message = (String) args[1];
			}
			
			playerMessage = i18n.getVarString(Messages.Player.PLAYER_KICK)
					.setVariable("message", message != null ? message : "No reason provided")
					.setVariable("kicker", clientPlayer != null ? clientPlayer.getName() : "Unknown")
					.toString();
			break;
		case SELF:
			playerMessage = i18n.getString(Messages.Player.PLAYER_LEAVE);
			break;
		case STOP:
			playerMessage = i18n.getString(Messages.Player.GAME_STOPPED);
			break;
		case LOSE:
			if (killer != null) {
				broadcastMessage = i18n.getVarString(Messages.Broadcast.PLAYER_LOST_GAME)
						.setVariable("player", player.getName())
						.setVariable("killer", killer.getName())
						.toString();
			} else {
				broadcastMessage = i18n.getVarString(Messages.Broadcast.PLAYER_LOST_GAME_UNKNOWN_KILLER)
						.setVariable("player", player.getName())
						.toString();
			}
			
			playerMessage = i18n.getString(Messages.Player.PLAYER_LOSE);
			break;
		case WIN:
			broadcastMessage = i18n.getVarString(Messages.Broadcast.PLAYER_WON_GAME)
					.setVariable("player", player.getName())
					.toString();
			
			playerMessage = i18n.getString(Messages.Player.PLAYER_WIN);
		default:
			break;
		}
		
		broadcast(broadcastMessage);
		player.sendMessage(playerMessage);
	}
	
	public void requestLose(SpleefPlayer player) {
		requestLose(player, true);
	}
	
	private void requestLose(SpleefPlayer player, boolean checkWin) {
		if (!ingamePlayers.contains(player)) {
			return;
		}
		
		final SpleefPlayer killer = killDetector.detectKiller(this, player);
		leave(player, QuitCause.LOSE, killer);
		
		if (ingamePlayers.size() == 1 && checkWin) {
			SpleefPlayer playerLeft = ingamePlayers.iterator().next();
			requestWin(playerLeft);
		} else if (ingamePlayers.size() == 0) {
			GameEndEvent event = new GameEndEvent(this);
			eventManager.callEvent(event);
			
			resetGame();
		}
	}
	
	public void requestWin(SpleefPlayer... players) {
		for (SpleefPlayer ingamePlayer : ingamePlayers) {
			for (SpleefPlayer player : players) {
				if (ingamePlayer == player) {
					leave(player, QuitCause.WIN);
				} else {
					requestLose(player, false);
				}
			}
		}
		
		PlayerWinGameEvent event = new PlayerWinGameEvent(this, players);
		eventManager.callEvent(event);
		
		resetGame();		
	}
	
	public void kickPlayer(SpleefPlayer player, String message, CommandSender sender) {
		if (!ingamePlayers.contains(player)) {
			throw new IllegalArgumentException("Player must be in game to kick");
		}
		
		leave(player, QuitCause.KICK, sender, message);
	}
	
	public Set<SpleefPlayer> getPlayers() {
		return ingamePlayers;
	}
	
	public boolean isIngame(SpleefPlayer player) {
		return ingamePlayers.contains(player);
	}
	
	public void registerGameListener(SpleefListener listener) {
		eventManager.registerListener(listener);
	}
	
	public EditSession newEditSession() {
		return editSessionFactory.getEditSession(worldEditWorld, NO_BLOCK_LIMIT);
	}
	
	public void addFlag(AbstractFlag<?> flag) {
		flag.onFlagAdd(this);
		flagManager.addFlag(flag);
		
		eventManager.registerListener(flag);
	}
	
	public void removeFlag(String path) {
		AbstractFlag<?> flag = flagManager.removeFlag(path);
		flag.onFlagRemove(this);
		
		eventManager.unregister(flag);
	}
	
	public boolean isFlagPresent(String path) {
		return flagManager.isFlagPresent(path);
	}
	
	public boolean isFlagPresent(Class<? extends AbstractFlag<?>> clazz) {
		return flagManager.isFlagPresent(clazz);
	}
	
	public <T extends AbstractFlag<?>> T getFlag(Class<T> flag) {
		return flagManager.getFlag(flag);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractFlag<?>> T getFlag(String path) {
		return (T) flagManager.getFlag(path);
	}
	
	public void addExtension(GameExtension extension) {
		extensionManager.addExtension(extension);
	}
	
	public void removeExtension(GameExtension extension) {
		extensionManager.removeExtension(extension);
	}
	
	public Set<GameExtension> getExtensions() {
		return extensionManager.getExtensions();
	}
	
	public <T extends GameExtension> Set<T> getExtensionsByType(Class<T> extClass) {
		return getExtensionsByType(extClass, false);
	}
	
	public <T extends GameExtension> Set<T> getExtensionsByType(Class<T> extClass, boolean strict) {
		return extensionManager.getExtensionsByType(extClass, strict);
	}
	
	public void addFloor(Floor floor) {
		floors.put(floor.getName(), floor);
	}
	
	public Floor removeFloor(String name) {
		return floors.remove(name);
	}
	
	public boolean isFloorPresent(String name) {
		return floors.containsKey(name);
	}
	
	public Floor getFloor(String name) {
		return floors.get(name);
	}
	
	public Collection<Floor> getFloors() {
		return floors.values();
	}
	
	public boolean canSpleef(Block block) {
		if (block.getType() == Material.AIR) {
			//Player can not "spleef" an empty block
			return false;
		}
		
		if (gameState != GameState.INGAME) {
			//Players can't spleef while the game is not ingame
			return false;
		}
		
		boolean onFloor = false;
		for (Floor floor : getFloors()) {
			if (floor.contains(block)) {
				onFloor = true;
				break;
			}
		}
		
		return onFloor;
	}
	
	public void addDeathzone(String name, Region region) {
		deathzones.put(name, region);
	}
	
	public Region removeDeathzone(String name) {
		return deathzones.remove(name);
	}
	
	public Region getDeathzone(String name) {
		return deathzones.get(name);
	}
	
	public boolean isDeathzonePresent(String name) {
		return deathzones.containsKey(name);
	}
	
	public void setGameState(GameState state) {
		GameState old = this.gameState;
		this.gameState = state;
		
		GameStateChangeEvent event = new GameStateChangeEvent(this, old);
		eventManager.callEvent(event);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(GameProperty property) {
		return (T) flagManager.getProperty(property);
	}
	
	public void requestProperty(GameProperty property, Object value) {
		flagManager.requestProperty(property, value);
	}
	
	public void addBlockBroken(SpleefPlayer player, Block brokenBlock) {
		Set<Block> set = blocksBroken.get(player);
		if (set == null) {
			// Lazily initialize the set
			set = Sets.newHashSet();
			blocksBroken.put(player, set);
		}
		
		set.add(brokenBlock);
	}
	
	public void setKillDetector(KillDetector detector) {
		Validate.notNull(detector, "detector cannot be null");
		
		this.killDetector = detector;
	}
	
	public void broadcast(String message) {
		broadcast(BroadcastTarget.AROUND_GAME, message);
	}
	
	public void broadcast(BroadcastTarget target, String message) {
		switch (target) {
		case AROUND_GAME:
			//Use any floor as a fixpoint
			Iterator<Floor> iterator = floors.values().iterator();
			if (iterator.hasNext()) {
				Floor floor = iterator.next();
				
				Vector center = floor.getRegion().getCenter();
				int broadcastRadius = getPropertyValue(GameProperty.BROADCAST_RADIUS);
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					Vector playerVec = BukkitUtil.toVector(player.getLocation());
					
					double distanceSq = center.distanceSq(playerVec);
					if (distanceSq <= Math.pow(broadcastRadius, 2)) {
						player.sendMessage(PREFIX + message);
					}
				}
				
				break;
			}
			
			//$FALL-THROUGH$
		case GLOBAL:
			Bukkit.broadcastMessage(PREFIX + message);
			break;
		case INGAME:
			for (SpleefPlayer player : ingamePlayers) {
				player.sendMessage(message);
			}
			break;
		default:
			break;
		}
	}
	
	/* Event hooks */
	@SuppressWarnings("deprecation")
	public void onPlayerInteract(PlayerInteractEvent event, SpleefPlayer player) {
		if (gameState != GameState.INGAME) {
			return;
		}
		
		Action action = event.getAction();
		boolean isInstantBreak = getPropertyValue(GameProperty.INSTANT_BREAK);
		boolean playBreakEffect = getPropertyValue(GameProperty.PLAY_BLOCK_BREAK);
		
		PlayerInteractGameEvent spleefEvent = new PlayerInteractGameEvent(this, player);
		eventManager.callEvent(spleefEvent);
		
		if (spleefEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		if (action == Action.LEFT_CLICK_BLOCK && isInstantBreak) {
			Block block = event.getClickedBlock();
			boolean breakBlock = false;
			
			for (Floor floor : floors.values()) {
				if (floor.contains(block)) {
					breakBlock = true;
					break;
				}
			}
			
			if (breakBlock) {
				Material blockMaterial = block.getType();
				block.setType(Material.AIR);
				
				if (playBreakEffect) {
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, blockMaterial.getId());
				}
				
				addBlockBroken(player, block);
			}
		}
	}
	
	public void onPlayerBreakBlock(BlockBreakEvent event, SpleefPlayer player) {
		if (gameState != GameState.INGAME) {
			return;
		}
		
		PlayerBlockBreakEvent spleefEvent = new PlayerBlockBreakEvent(this, player, event.getBlock());
		eventManager.callEvent(spleefEvent);
		
		Block block = event.getBlock();
		
		if (spleefEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		boolean onFloor = false;
		for (Floor floor : floors.values()) {
			if (floor.contains(block)) {
				onFloor = true;
				break;
			}
		}
		
		boolean disableBuild = getPropertyValue(GameProperty.DISABLE_BUILD);
		
		if (!onFloor && disableBuild) {
			event.setCancelled(true);
		} else {
			addBlockBroken(player, block);
		}
	}
	
	public void onPlayerPlaceBlock(BlockPlaceEvent event, SpleefPlayer player) {
		PlayerBlockPlaceEvent spleefEvent = new PlayerBlockPlaceEvent(this, player, event.getBlock());
		eventManager.callEvent(spleefEvent);
		
		if (spleefEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		boolean disableBuild = getPropertyValue(GameProperty.DISABLE_BUILD);
		
		if (disableBuild) {
			event.setCancelled(true);
		}
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event, SpleefPlayer player) {
		boolean disablePickup = getPropertyValue(GameProperty.DISABLE_ITEM_PICKUP);
		if (disablePickup) {
			event.setCancelled(true);
		}
	}
	
	public void onPlayerDropItem(PlayerDropItemEvent event, SpleefPlayer player) {
		boolean disableDrop = getPropertyValue(GameProperty.DISABLE_ITEM_DROP);
		if (disableDrop) {
			event.setCancelled(true);
		}
	}
	
	public void onPlayerFoodLevelChange(FoodLevelChangeEvent event, SpleefPlayer player) {
		boolean noHunger = getPropertyValue(GameProperty.DISABLE_HUNGER);
		if (noHunger) {
			event.setCancelled(true);
		}
	}
	
	public void onEntityByEntityDamageEvent(EntityDamageByEntityEvent event, SpleefPlayer damagedPlayer) {
		boolean disablePvp = getPropertyValue(GameProperty.DISABLE_PVP);
		boolean disableDamage = getPropertyValue(GameProperty.DISABLE_DAMAGE);
		
		if (event.getDamager() instanceof Player && disablePvp || !(event.getDamager() instanceof Player) && disableDamage) { 
			event.setCancelled(true);
		}
	}
	
	public void onEntityDamageEvent(EntityDamageEvent event, SpleefPlayer damaged) {
		boolean disableDamage = getPropertyValue(GameProperty.DISABLE_DAMAGE);
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK && disableDamage) {
			event.setCancelled(true);
		}
	}
	
	public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event, SpleefPlayer targetted) {
		boolean disableDamage = getPropertyValue(GameProperty.DISABLE_DAMAGE);
		
		if (disableDamage) {
			event.setCancelled(true);
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event, SpleefPlayer quitter) {
		leave(quitter, QuitCause.SELF);
	}

}
