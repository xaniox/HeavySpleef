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
package de.xaniox.heavyspleef.core.game;

import com.google.common.collect.*;
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
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.MinecraftVersion;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.DefaultConfig;
import de.xaniox.heavyspleef.core.config.GeneralSection;
import de.xaniox.heavyspleef.core.config.QueueSection;
import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.extension.ExtensionManager;
import de.xaniox.heavyspleef.core.extension.GameExtension;
import de.xaniox.heavyspleef.core.flag.AbstractFlag;
import de.xaniox.heavyspleef.core.flag.FlagManager;
import de.xaniox.heavyspleef.core.floor.DefaultFloorRegenerator;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.floor.FloorRegenerator;
import de.xaniox.heavyspleef.core.floor.RegenerationCause;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.hook.WorldEditHook;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.PlayerStateHolder;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.core.script.Variable;
import de.xaniox.heavyspleef.core.script.VariableSuppliable;
import de.xaniox.heavyspleef.core.stats.StatisticRecorder;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.metadata.MetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements VariableSuppliable {
	
	private static final String SPLEEF_COMMAND = "spleef";
	private static final int NO_BLOCK_LIMIT = -1;
	private static final int DEFAULT_COUNTDOWN = 10;
	private static final String HAS_FLAG_PREFIX = "has_flag";
	private static final String FLAG_VALUE_PREFIX = "flag_value";
	private static final Map<Class<? extends Region>, SpawnpointGenerator<?>> SPAWNPOINT_GENERATORS;
	
	static {
		SPAWNPOINT_GENERATORS = Maps.newHashMap();
		
		SPAWNPOINT_GENERATORS.put(CuboidRegion.class, new CuboidSpawnpointGenerator());
		SPAWNPOINT_GENERATORS.put(CylinderRegion.class, new CylinderSpawnpointGenerator());
		SPAWNPOINT_GENERATORS.put(Polygonal2DRegion.class, new Polygonal2DSpawnpointGenerator());
	}
	
	private final I18N i18n;
	
	private final EditSessionFactory editSessionFactory;
	private HeavySpleef heavySpleef;
	private EventBus eventBus;
	private Set<SpleefPlayer> ingamePlayers;
	private List<SpleefPlayer> deadPlayers;
	private List<SpleefPlayer> killedPlayers;
	private BiMap<SpleefPlayer, Set<Block>> blocksBroken;
	private KillDetector killDetector;
	private JoinRequester joinRequester;
	private Queue<SpleefPlayer> queuedPlayers;
	private CountdownTask countdownTask;
	private StatisticRecorder statisticRecorder;
	private FloorRegenerator floorRegenerator;
	private Queue<Location> spawnLocationQueue;
	
	private String name;
	private World world;
	private com.sk89q.worldedit.world.World worldEditWorld;
	private FlagManager flagManager;
	private ExtensionManager extensionManager;
	private GameState gameState;
	private Map<String, Floor> floors;
	private Map<String, Region> deathzones;
	
	public Game(HeavySpleef heavySpleef, String name, World world) {
		this.heavySpleef = heavySpleef;
		this.name = name;
		this.world = world;
		this.worldEditWorld = new BukkitWorld(world);
		this.i18n = I18NManager.getGlobal();
		this.ingamePlayers = Sets.newLinkedHashSet();
		this.deadPlayers = Lists.newArrayList();
		this.eventBus = heavySpleef.getGlobalEventBus().newChildBus();
		this.statisticRecorder = new StatisticRecorder(heavySpleef, heavySpleef.getLogger());
		this.floorRegenerator = new DefaultFloorRegenerator();
		this.killedPlayers = Lists.newArrayList();
		
		eventBus.registerListener(statisticRecorder);
		setGameState(GameState.WAITING);
		
		DefaultConfig configuration = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
		FlagManager.GamePropertyBundle defaults = new FlagManager.DefaultGamePropertyBundle(configuration.getProperties());
		
		this.flagManager = new FlagManager(heavySpleef.getPlugin(), defaults);
		this.extensionManager = heavySpleef.getExtensionRegistry().newManagerInstance(eventBus);
		this.deathzones = Maps.newHashMap();
		this.blocksBroken = HashBiMap.create();
		this.killDetector = new DefaultKillDetector();
		this.queuedPlayers = new LinkedList<SpleefPlayer>();
		this.spawnLocationQueue = new LinkedList<Location>();
		
		//Concurrent map for database schematics
		this.floors = new ConcurrentHashMap<String, Floor>();
		
		WorldEditHook hook = (WorldEditHook) heavySpleef.getHookManager().getHook(HookReference.WORLDEDIT);
		WorldEdit worldEdit = hook.getWorldEdit();
		
		this.editSessionFactory = worldEdit.getEditSessionFactory();
		
		GeneralSection generalSection = configuration.getGeneralSection();
		this.joinRequester = new JoinRequester(this, heavySpleef.getPvpTimerManager());
		this.joinRequester.setPvpTimerMode(generalSection.getPvpTimer() > 0);
	}
	
	public HeavySpleef getHeavySpleef() {
		return heavySpleef;
	}
	
	public void setHeavySpleef(HeavySpleef heavySpleef) {
		Validate.notNull(heavySpleef, "HeavySpleef instance cannot be null");
		this.heavySpleef = heavySpleef;
	}
	
	public String getName() {
		return name;
	}
	
	void setName(String newName) {
		String old = this.name;
		this.name = newName;
		
		GameRenameEvent event = new GameRenameEvent(this, old);
		eventBus.callEvent(event);
	}
	
	public World getWorld() {
		return world;
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public EventBus getEventBus() {
		return eventBus;
	}
	
	public FlagManager getFlagManager() {
		return flagManager;
	}
	
	public Map<String, Region> getDeathzones() {
		return deathzones;
	}
	
	public List<SpleefPlayer> getDeadPlayers() {
		return deadPlayers;
	}
	
	public BiMap<SpleefPlayer, Set<Block>> getBlocksBroken() {
		return blocksBroken;
	}
	
	public JoinRequester getJoinRequester() {
		return joinRequester;
	}
	
	public CountdownTask getCountdownTask() {
		return countdownTask;
	}
	
	public StatisticRecorder getStatisticRecorder() {
		return statisticRecorder;
	}
	
	public FloorRegenerator getFloorRegenerator() {
		return floorRegenerator;
	}
	
	public void setFloorRegenerator(FloorRegenerator regenerator) {
		if (regenerator == null) {
			regenerator = new DefaultFloorRegenerator();
		}
		
		this.floorRegenerator = regenerator;
	}
	
	public boolean countdown() {
		GameCountdownEvent event = new GameCountdownEvent(this);
		eventBus.callEvent(event);
		
		if (event.isCancelled()) {
			return false;
		}
		
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
		
		DefaultConfig config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
		GeneralSection section = config.getGeneralSection();
		
		if (section.getBroadcastGameStart()) {
			List<String> blacklists = section.getBroadcastGameStartBlacklist();
			String message = i18n.getVarString(Messages.Broadcast.BROADCAST_GAME_START)
					.setVariable("game", name)
					.toString();
			
			Object baseComponent = null;
			
			if (MinecraftVersion.isSpigot()) {
				baseComponent = new ComponentBuilder(message)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
							TextComponent.fromLegacyText(i18n.getString(Messages.Command.CLICK_TO_JOIN))))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spleef join " + name))
					.color(net.md_5.bungee.api.ChatColor.GOLD)
					.create();
			}
			
			for (Player globalPlayer : Bukkit.getOnlinePlayers()) {
				World globalPlayerWorld = globalPlayer.getWorld();
				
				if (blacklists.contains(globalPlayerWorld.getName())) {
					continue;
				}
				
				if (MinecraftVersion.isSpigot()) {
					globalPlayer.spigot().sendMessage((BaseComponent[]) baseComponent);
				} else {
					globalPlayer.sendMessage(section.getSpleefPrefix() + message);
				}
			}
		}
		
		EditSession editSession = editSessionFactory.getEditSession(worldEditWorld, NO_BLOCK_LIMIT);
		
		// Regenerate all floors
		for (Floor floor : floors.values()) {
			floorRegenerator.regenerate(floor, editSession, RegenerationCause.COUNTDOWN);
		}
		
		// Generate a random spawnpoint
		Floor topFloor = null;
		for (Floor floor : floors.values()) {
			if (topFloor == null || floor.getRegion().getMaximumPoint().getBlockY() > topFloor.getRegion().getMaximumPoint().getBlockY()) {
				topFloor = floor;
			}
		}
		
		Region region = topFloor.getRegion();
		
		List<Location> randomLocations = Lists.newArrayList();
		generateSpawnpoints(region, randomLocations, ingamePlayers.size());
		
		int listIndex = 0;
		int randomIndex = 0;
		List<Location> spawnLocations = event.getSpawnLocations();
		Map<SpleefPlayer, Location> spawnLocationMap = event.getSpawnLocationMap();
		
		for (SpleefPlayer player : ingamePlayers) {
			Location location;			
			
			if (spawnLocationMap != null && spawnLocationMap.containsKey(player)) {
				location = spawnLocationMap.get(player);
			} else if (spawnLocations != null && !spawnLocations.isEmpty()) {
				location = spawnLocations.get(listIndex++);
				listIndex = listIndex >= spawnLocations.size() ? 0 : listIndex;
			} else {
				location = randomLocations.get(randomIndex++);
			}
			
			player.teleport(location);
		}
		
		spawnLocationQueue.clear();
		if (spawnLocations != null) {
			for (int i = listIndex; i < spawnLocations.size(); i++) {
				Location next = spawnLocations.get(i);
				spawnLocationQueue.offer(next);
			}
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
					for (SpleefPlayer player : ingamePlayers) {
						Player bukkitPlayer = player.getBukkitPlayer();

						bukkitPlayer.setLevel(0);
						bukkitPlayer.setExp(0f);
						bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);
					}
					
					start();
					
					countdownTask = null;
				}
				
				@Override
				public void onCountdownCount(CountdownTask task) {
					boolean broadcast = task.getRemaining() % 10 == 0 || task.getRemaining() <= 5;
					
					GameCountdownChangeEvent event = new GameCountdownChangeEvent(Game.this, countdownTask, broadcast);
					eventBus.callEvent(event);
					
					float percent = (float)task.getRemaining() / task.getLength();
					for (SpleefPlayer player : ingamePlayers) {
						player.getBukkitPlayer().setLevel(task.getRemaining());
						player.getBukkitPlayer().setExp(percent);
					}
					
					if (broadcast) {
						broadcast(BroadcastTarget.INGAME, i18n.getVarString(Messages.Broadcast.GAME_COUNTDOWN_MESSAGE)
							.setVariable("remaining", String.valueOf(task.getRemaining()))
							.toString());
						
						for (SpleefPlayer player : ingamePlayers) {
							Player bukkitPlayer = player.getBukkitPlayer();
							bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
						}
					}
				}
			});
			
			for (SpleefPlayer player : ingamePlayers) {
				player.getBukkitPlayer().setLevel(countdownLength);
				player.getBukkitPlayer().setExp(1.0f);
			}
			
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
		eventBus.callEvent(event);
		
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
			leave(player, QuitCause.STOP, true);
		}
		
		resetGame();
		
		GameEndEvent event = new GameEndEvent(this);
		eventBus.callEvent(event);
		
		broadcast(i18n.getVarString(Messages.Broadcast.GAME_STOPPED)
				.setVariable("game", name)
				.toString());
	}
	
	private void resetGame() {
		EditSession editSession = editSessionFactory.getEditSession(worldEditWorld, NO_BLOCK_LIMIT);
		for (Floor floor : floors.values()) {
			floorRegenerator.regenerate(floor, editSession, RegenerationCause.RESET);
		}
		
		blocksBroken.clear();
		deadPlayers.clear();
		spawnLocationQueue.clear();
		setGameState(GameState.WAITING);
		
		//Stop the countdown if necessary
		if (countdownTask != null) {
			countdownTask.cancel();
			countdownTask = null;
		}
	}
	
	public void flushQueue() {
		Queue<SpleefPlayer> failedToQueue = Lists.newLinkedList();
		
		//Flush the queue
		while (!queuedPlayers.isEmpty()) {
			SpleefPlayer player = queuedPlayers.poll();
			if (!player.isOnline()) {
				continue;
			}
			
			PlayerQueueFlushEvent event = new PlayerQueueFlushEvent(this, player);
			eventBus.callEvent(event);
			
			PlayerQueueFlushEvent.FlushResult result = event.getResult();
			if (result == PlayerQueueFlushEvent.FlushResult.ALLOW) {
				PlayerPreJoinGameEvent joinEvent = new PlayerPreJoinGameEvent(this, player);
				eventBus.callEvent(joinEvent);
				
				if (joinEvent.getJoinResult() != JoinResult.TEMPORARY_DENY) {
					join(player, joinEvent);
					continue;
				}
			}
			
			failedToQueue.offer(player);
		}
		
		queuedPlayers.addAll(failedToQueue);
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
		eventBus.callEvent(event);
	}
	
	public void enable() {
		if (gameState.isGameEnabled()) {
			return;
		}
		
		setGameState(GameState.WAITING);
		
		GameEnableEvent event = new GameEnableEvent(this);
		eventBus.callEvent(event);
	}
	
	public JoinResult join(SpleefPlayer player) {
		return join(player, null);
	}
	
	private JoinResult join(SpleefPlayer player, PlayerPreJoinGameEvent event) {
		if (ingamePlayers.contains(player)) {
			return JoinResult.PERMANENT_DENY;
		}
		
		if (event == null) {
			//Only call this event if the caller didn't give us an event
			//This event is called before the player joins the game!
			event = new PlayerPreJoinGameEvent(this, player);		
			eventBus.callEvent(event);
		}
		
		if (event.getTeleportationLocation() == null) {
			player.sendMessage(i18n.getString(Messages.Player.ERROR_NO_LOBBY_POINT_SET));
			return JoinResult.PERMANENT_DENY;
		}
		
		JoinResult result = event.getJoinResult();
		switch (result) {
		case ALLOW:
			//Go through
			break;
		case PERMANENT_DENY:
		case TEMPORARY_DENY:
			String denyMessage = event.getMessage();
			if (denyMessage != null) {
				player.sendMessage(denyMessage);
			}
			
			return result;
		case NOT_SPECIFIED:
			//Do a state check
			if (gameState == GameState.INGAME) {
				return JoinResult.TEMPORARY_DENY;
			}
			break;
		default:
			break;
		}
		
		ingamePlayers.add(player);
		
		if (gameState == GameState.WAITING) {
			setGameState(GameState.LOBBY);
		}
		
		Location location;
		
		if (gameState.isGameActive()) {
			location = spawnLocationQueue.poll();
			
			if (location == null) {
				// Generate a random spawnpoint
				Floor topFloor = null;
				for (Floor floor : floors.values()) {
					if (topFloor == null || floor.getRegion().getMaximumPoint().getBlockY() > topFloor.getRegion().getMaximumPoint().getBlockY()) {
						topFloor = floor;
					}
				}
				
				Region region = topFloor.getRegion();
				
				List<Location> randomLocations = Lists.newArrayList();
				generateSpawnpoints(region, randomLocations, 1);
				
				location = randomLocations.get(0);
			}
		} else {
			location = event.getTeleportationLocation();
		}

		PlayerStateHolder holder = new PlayerStateHolder();
		holder.setLocation(player.getBukkitPlayer().getLocation());
		holder.setGamemode(player.getBukkitPlayer().getGameMode());
		
		//Firstly set the players gamemode and teleport him
		//to provide compatibility with inventory plugins such as MultiInv
		//and xInventories
		player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
		player.teleport(location);
		
		//Store a reference to the player state
		holder.updateState(player.getBukkitPlayer(), false, holder.getGamemode());
		player.savePlayerState(this, holder);
		
		PlayerStateHolder.applyDefaultState(player.getBukkitPlayer());
		
		//This event is called when the player actually joins the game
		PlayerJoinGameEvent joinGameEvent = new PlayerJoinGameEvent(this, player);
		eventBus.callEvent(joinGameEvent);
		
		broadcast(i18n.getVarString(Messages.Broadcast.PLAYER_JOINED_GAME)
				.setVariable("player", player.getDisplayName())
				.toString());
		
		if (event.getMessage() != null) {
			player.sendMessage(event.getMessage());
		}
		
		if (joinGameEvent.getStartGame()) {
			countdown();
		}
		
		return JoinResult.ALLOW;
	}
	
	public boolean queue(SpleefPlayer player) {
		PlayerEnterQueueEvent event = new PlayerEnterQueueEvent(this, player);
		eventBus.callEvent(event);
		
		if (event.isCancelled()) {
			return false;
		}
		
		queuedPlayers.add(player);
		return true;
	}
	
	public boolean isQueued(SpleefPlayer player) {
		return queuedPlayers.contains(player);
	}
	
	public void unqueue(SpleefPlayer player) {
		PlayerLeaveQueueEvent event = new PlayerLeaveQueueEvent(this, player);
		eventBus.callEvent(event);
		
		queuedPlayers.remove(player);
	}
	
	public Queue<SpleefPlayer> getQueuedPlayers() {
		return queuedPlayers;
	}
	
	public void leave(SpleefPlayer player) {
		leave(player, QuitCause.SELF, true);
	}
	
	public void leave(SpleefPlayer player, QuitCause cause, boolean sendMessages, Object... args) {
		if (!ingamePlayers.contains(player)) {
			return;
		}
		
		ingamePlayers.remove(player);
		
		if (gameState == GameState.INGAME) {
			deadPlayers.add(player);
		}
		
		SpleefPlayer killer = null;
		if (cause == QuitCause.LOSE && args != null && args.length > 0 && args[0] != null && args[0] instanceof SpleefPlayer) {
			killer = (SpleefPlayer) args[0];
		}
		
		PlayerLeaveGameEvent event = new PlayerLeaveGameEvent(this, player, killer, cause);
		eventBus.callEvent(event);
		
		if (event.isCancelled()) {
			//Add the player again...
			ingamePlayers.add(player);
			deadPlayers.remove(player);
			return;
		}
		
		if (!event.isSendMessages()) {
			sendMessages = false;
		}
		
		Location tpLoc = event.getTeleportationLocation();
		
		//Receive the state back
		if (!player.getBukkitPlayer().isDead()) {
			PlayerStateHolder playerState = player.getPlayerState(this);
			if (playerState != null) {
				playerState.apply(player.getBukkitPlayer(), tpLoc == null);
				player.removePlayerState(this);
			} else {
				//Ugh, something went wrong
				player.sendMessage(i18n.getString(Messages.Player.ERROR_ON_INVENTORY_LOAD));
			}
		} else {
			//Keep track of this dead player to restore
			//his inventory on respawn
			killedPlayers.add(player);
		}
		
		if (tpLoc != null) {
			player.teleport(tpLoc);
		}
		
		if (ingamePlayers.size() == 0 && gameState == GameState.LOBBY) {
			setGameState(GameState.WAITING);
		}
		
		if (cause == QuitCause.WIN) {
			player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
		}
		
		if (sendMessages) {
			String broadcastMessage = i18n.getVarString(Messages.Broadcast.PLAYER_LEFT_GAME)
					.setVariable("player", player.getDisplayName())
					.toString();
			String playerMessage = i18n.getString(Messages.Player.PLAYER_LEAVE);
			BroadcastTarget broadcastMessageTarget = BroadcastTarget.AROUND_GAME;
			
			if (event.getPlayerMessage() != null && event.getBroadcastMessage() != null) {
				playerMessage = event.getPlayerMessage();
				broadcastMessage = event.getBroadcastMessage();
			} else {
				switch (cause) {
				case KICK:
					CommandSender clientPlayer = null;
					String message = null;
					
					int messageIndex = 0;
					if (args != null && args.length > 0 && args[0] instanceof CommandSender) {
						// Caller gave us a client player
						clientPlayer = (CommandSender) args[0];
						messageIndex = 1;
					}
					
					if (args != null && args.length > messageIndex && args[messageIndex] instanceof String) {
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
								.setVariable("player", player.getDisplayName())
								.setVariable("killer", killer.getDisplayName())
								.toString();
					} else {
						broadcastMessage = i18n.getVarString(Messages.Broadcast.PLAYER_LOST_GAME_UNKNOWN_KILLER)
								.setVariable("player", player.getDisplayName())
								.toString();
					}
					
					playerMessage = i18n.getString(Messages.Player.PLAYER_LOSE);
					break;
				case WIN:
					broadcastMessage = i18n.getVarString(Messages.Broadcast.PLAYER_WON_GAME)
							.setVariable("player", player.getDisplayName())
							.toString();
					
					DefaultConfig config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
					GeneralSection section = config.getGeneralSection();
					
					if (section.isWinMessageToAllEnabled()) {
						broadcastMessageTarget = BroadcastTarget.PARTICIPATED;
					}
					
					playerMessage = i18n.getString(Messages.Player.PLAYER_WIN);
				default:
					break;
				}
			}
			
			broadcast(broadcastMessageTarget, broadcastMessage);
			player.sendMessage(playerMessage);
		}
		
		PlayerLeftGameEvent leftEvent = new PlayerLeftGameEvent(this, player, killer, cause);
		eventBus.callEvent(leftEvent);
	}
	
	public void requestLose(SpleefPlayer player, QuitCause cause) {
		Object[] args = null;
		
		if (cause == QuitCause.LOSE) {
			final SpleefPlayer killer = killDetector.detectKiller(this, player);
			args = new Object[] { killer };
		}
		
		requestLose(player, true, cause, args);
	}
	
	private void requestLose(SpleefPlayer player, boolean checkWin, QuitCause cause, Object... args) {
		if (!ingamePlayers.contains(player)) {
			return;
		}
		
		leave(player, cause == null ? QuitCause.LOSE : cause, true, args);
		
		if (gameState == GameState.STARTING || gameState == GameState.INGAME) {
			if (ingamePlayers.size() == 1 && checkWin) {
				SpleefPlayer playerLeft = ingamePlayers.iterator().next();
				requestWin(playerLeft);
			} else if (ingamePlayers.size() == 0) {
				GameEndEvent event = new GameEndEvent(this);
				eventBus.callEvent(event);
				
				resetGame();
			}
		}
	}
	
	public void requestWin(SpleefPlayer... players) {
		requestWin(players, true);
	}
	
	public void requestWin(SpleefPlayer[] players, boolean sendMessages) {
		List<SpleefPlayer> winnerList = Arrays.asList(players);
		for (SpleefPlayer ingamePlayer : Sets.newHashSet(ingamePlayers)) {
			if (winnerList.contains(ingamePlayer)) {
				leave(ingamePlayer, QuitCause.WIN, sendMessages);
			} else {
				requestLose(ingamePlayer, sendMessages, QuitCause.LOSE);
			}
		}

		int toIndex = deadPlayers.size() - players.length;
		if (toIndex < 0) {
			toIndex = 0;
		}

		List<SpleefPlayer> dead = Lists.newArrayList(this.deadPlayers.subList(0, toIndex));
		Collections.reverse(dead);
		
		PlayerWinGameEvent event = new PlayerWinGameEvent(this, players, dead);
		eventBus.callEvent(event);
		
		resetGame();
		flushQueue();
	}
	
	public void kickPlayer(SpleefPlayer player, String message, CommandSender sender) {
		if (!ingamePlayers.contains(player)) {
			throw new IllegalArgumentException("Player must be in game to kick");
		}
		
		requestLose(player, true, QuitCause.KICK, sender, message);
	}
	
	public Set<SpleefPlayer> getPlayers() {
		return ingamePlayers;
	}
	
	public boolean isIngame(SpleefPlayer player) {
		return ingamePlayers.contains(player);
	}
	
	public void registerGameListener(SpleefListener listener) {
		eventBus.registerListener(listener);
	}
	
	public EditSession newEditSession() {
		return editSessionFactory.getEditSession(worldEditWorld, NO_BLOCK_LIMIT);
	}
	
	public void addFlag(AbstractFlag<?> flag) {
		addFlag(flag, true);
	}
	
	public void addFlag(AbstractFlag<?> flag, boolean initFlag) {
		flagManager.addFlag(flag);
		eventBus.registerListener(flag);
		
		if (initFlag) {
			flag.onFlagAdd(this);
		}
	}
	
	public void removeFlag(String path) {
		AbstractFlag<?> flag = flagManager.removeFlag(path);
		if (flag != null) {
			flag.onFlagRemove(this);
			
			eventBus.unregister(flag);
		}
	}
	
	public void removeFlag(Class<? extends AbstractFlag<?>> flagClass) {
		AbstractFlag<?> flag = flagManager.removeFlag(flagClass);
		if (flag != null) {
			flag.onFlagRemove(this);
			
			eventBus.unregister(flag);
		}
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
		extension.setGame(this);
		extensionManager.addExtension(extension);
	}
	
	public void removeExtension(GameExtension extension) {
		extension.setGame(null);
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
		
	@Override
	public void supply(Set<Variable> vars, Set<String> requested) {
		String gameStateName = gameState.name();
		gameStateName = Character.toUpperCase(gameStateName.charAt(0)) + gameStateName.substring(1);
		
		vars.add(new Variable("name", name));
		vars.add(new Variable("state", gameStateName));
		vars.add(new Variable("localized_state", gameState.getLocalizedName()));
		vars.add(new Variable("players", ingamePlayers.size()));
		vars.add(new Variable("dead", deadPlayers.size()));
		vars.add(new Variable("countdown", countdownTask != null ? countdownTask.getRemaining() : 0));
		vars.add(new Variable("is_countdown", countdownTask != null));
		
		for (String req : requested) {
			StringTokenizer tokenizer = new StringTokenizer(req, ":");
			
			String primaryReq = tokenizer.nextToken();
			
			boolean hasFlagRequest = primaryReq.equals(HAS_FLAG_PREFIX);
			boolean flagValueRequest = primaryReq.equals(FLAG_VALUE_PREFIX);
			
			if (hasFlagRequest || flagValueRequest) {
				if (!tokenizer.hasMoreTokens()) {
					throw new IllegalStateException("Requested variable '" + req + "' must be followed by a flag name ('<request>:<flagpath>')");
				}
				
				String flagPath = tokenizer.nextToken();
				
				if (hasFlagRequest) {
					vars.add(new Variable(req, isFlagPresent(flagPath)));
				} else if (flagValueRequest) {
					AbstractFlag<?> flag = getFlag(flagPath);
					
					vars.add(new Variable(req, flag != null ? flag.getValue() : null));
				}
			}
		}
	}
	
	public void setGameState(GameState state) {
		GameState old = this.gameState;
		this.gameState = state;
		
		GameStateChangeEvent event = new GameStateChangeEvent(this, old);
		eventBus.callEvent(event);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T getPropertyValue(GameProperty property) {
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
		case PARTICIPATED:
		case AROUND_GAME:
			//Use any floor as a fixpoint
			Iterator<Floor> iterator = floors.values().iterator();
			if (iterator.hasNext()) {
				Floor floor = iterator.next();
				
				Vector center = floor.getRegion().getCenter();
				int broadcastRadius = getPropertyValue(GameProperty.BROADCAST_RADIUS);
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					SpleefPlayer spleefPlayer = heavySpleef.getSpleefPlayer(player);
					Vector playerVec = BukkitUtil.toVector(player.getLocation());
					
					double distanceSq = center.distanceSq(playerVec);
					if (distanceSq <= Math.pow(broadcastRadius, 2) || isIngame(spleefPlayer)
							|| (target == BroadcastTarget.PARTICIPATED && deadPlayers.contains(spleefPlayer))) {
						player.sendMessage(heavySpleef.getSpleefPrefix() + message);
					}
				}
				
				break;
			}
			
			//$FALL-THROUGH$
		case GLOBAL:
			Bukkit.broadcastMessage(heavySpleef.getSpleefPrefix() + message);
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
		Block block = event.getClickedBlock();
		Action action = event.getAction();
		
		PlayerInteractGameEvent spleefEvent = new PlayerInteractGameEvent(this, player, block, action);
		eventBus.callEvent(spleefEvent);
		
		if (spleefEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		if (gameState != GameState.INGAME) {
			return;
		}
		
		boolean isInstantBreak = getPropertyValue(GameProperty.INSTANT_BREAK);
		boolean playBreakEffect = getPropertyValue(GameProperty.PLAY_BLOCK_BREAK);
		
		if (action == Action.LEFT_CLICK_BLOCK && isInstantBreak) {
			boolean breakBlock = false;
			
			for (Floor floor : floors.values()) {
				if (floor.contains(block)) {
					breakBlock = true;
					break;
				}
			}
			
			if (breakBlock) {
				PlayerBlockBreakEvent breakEvent = new PlayerBlockBreakEvent(this, player, block);
				eventBus.callEvent(breakEvent);
				
				if (breakEvent.isCancelled()) {
					return;
				}
				
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
			event.setCancelled(true);
			return;
		}
		
		Block block = event.getBlock();
		
		boolean onFloor = false;
		for (Floor floor : floors.values()) {
			if (floor.contains(block)) {
				onFloor = true;
				break;
			}
		}
		
		boolean disableBuild = getPropertyValue(GameProperty.DISABLE_BUILD);
		boolean disableFloorBreak = getPropertyValue(GameProperty.DISABLE_FLOOR_BREAK);
		
		if ((!onFloor && disableBuild) || disableFloorBreak) {
			event.setCancelled(true);
		} else {
			PlayerBlockBreakEvent spleefEvent = new PlayerBlockBreakEvent(this, player, event.getBlock());
			eventBus.callEvent(spleefEvent);
			
			
			if (spleefEvent.isCancelled()) {
				event.setCancelled(true);
				return;
			}
			
			addBlockBroken(player, block);
			//Prevent drops
			block.setType(Material.AIR);
		}
	}
	
	public void onPlayerPlaceBlock(BlockPlaceEvent event, SpleefPlayer player) {
		PlayerBlockPlaceEvent spleefEvent = new PlayerBlockPlaceEvent(this, player, event.getBlock());
		eventBus.callEvent(spleefEvent);
		
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
		handleQuit(quitter);
	}
	
	public void onPlayerKick(PlayerKickEvent event, SpleefPlayer quitter) {
		handleQuit(quitter);
	}
	
	private void handleQuit(SpleefPlayer quitter) {
		if (ingamePlayers.contains(quitter)) {
			requestLose(quitter, QuitCause.SELF);
		} else if (isQueued(quitter)) {
			unqueue(quitter);
		}
	}
	
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event, SpleefPlayer typing) {
		boolean blockCommands = false;
		
		if (ingamePlayers.contains(typing)) {
			blockCommands = getPropertyValue(GameProperty.BLOCK_COMMANDS);
		} else if (isQueued(typing)) {
			DefaultConfig config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
			QueueSection section = config.getQueueSection();
			
			blockCommands = !section.isCommandsInQueue();
		}
		
		if (!blockCommands || typing.hasPermission(Permissions.PERMISSION_COMMAND_BYPASS)) {
			return;
		}
		
		String message = event.getMessage();
		String[] components = message.split(" ");
		
		String command = components[0];
		command = command.substring(1);
		
		DefaultConfig config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
		GeneralSection section = config.getGeneralSection();
		
		List<String> whitelistedCommands = section.getWhitelistedCommands();
		if (whitelistedCommands.contains(command) || command.equalsIgnoreCase(SPLEEF_COMMAND)) {
			return;
		}
		
		//Block this command
		event.setCancelled(true);
		typing.sendMessage(i18n.getString(Messages.Player.COMMAND_NOT_ALLOWED));
	}
	
	public void onPlayerDeath(PlayerDeathEvent event, SpleefPlayer dead) {
		requestLose(dead, QuitCause.LOSE);
	}

	public void onPlayerRespawn(PlayerRespawnEvent event, final SpleefPlayer respawning) {
		if (!killedPlayers.contains(respawning)) {
			return;
		}
		
		killedPlayers.remove(respawning);
		Bukkit.getScheduler().scheduleSyncDelayedTask(heavySpleef.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				if (respawning.isOnline()) {
					PlayerStateHolder playerState = respawning.getPlayerState(Game.this);
					if (playerState != null) {
						playerState.apply(respawning.getBukkitPlayer(), true);
					} else {
						//Ugh, something went wrong
						respawning.sendMessage(i18n.getString(Messages.Player.ERROR_ON_INVENTORY_LOAD));
					}
				}
			}
		}, 5L);
	}
	
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event, SpleefPlayer player) {
		if (gameState != GameState.INGAME && gameState != GameState.STARTING) {
			return;
		}
		
		GameMode gameMode = event.getNewGameMode();
		if (gameMode != GameMode.CREATIVE) {
			return;
		}
		
		event.setCancelled(true);
		player.sendMessage(i18n.getString(Messages.Player.CANNOT_CHANGE_GAMEMODE));
	}
	
	public void onPlayerTeleport(PlayerTeleportEvent event, SpleefPlayer player) {
		Player bukkitPlayer = player.getBukkitPlayer();
		
		if (bukkitPlayer.hasMetadata(SpleefPlayer.ALLOW_NEXT_TELEPORT_KEY)) {
			List<MetadataValue> values = bukkitPlayer.getMetadata(SpleefPlayer.ALLOW_NEXT_TELEPORT_KEY);
			
			for (MetadataValue value : values) {
				if (value.getOwningPlugin() != heavySpleef.getPlugin()) {
					continue;
				}
				
				if (value.asBoolean()) {
					return;
				}
			}
		}
		
		event.setCancelled(true);
	}
	
	public enum JoinResult {
		
		TEMPORARY_DENY,
		PERMANENT_DENY,
		NOT_SPECIFIED,
		ALLOW;
		
	}

}