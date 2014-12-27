package de.matzefratze123.heavyspleef.core;

import static de.matzefratze123.heavyspleef.core.HeavySpleef.PREFIX;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.regions.CuboidRegion;

import de.matzefratze123.heavyspleef.core.event.EventManager;
import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.GameDisableEvent;
import de.matzefratze123.heavyspleef.core.event.GameEnableEvent;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerBlockBreakEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerBlockPlaceEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerInteractGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent.JoinResult;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.SpleefListener;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.PlayerStateHolder;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

@Entity
@Table(name = "games")
@XmlRootElement(name = "game")
public class Game {

	public static final String NAME_ATTRIBUTE = "name";
	
	@Transient
	@XmlTransient
	private HeavySpleef heavySpleef;
	@Transient
	@XmlTransient
	private EventManager eventManager;
	@Transient
	@XmlTransient
	private Set<SpleefPlayer> ingamePlayers;
	@Transient
	@XmlTransient
	private BiMap<SpleefPlayer, Set<Block>> blocksBroken;
	
	@XmlAttribute
	@Id
	private String name;
	private FlagManager flagManager;
	private GameState state;
	private Map<String, Floor> floors;
	private Set<CuboidRegion> deathzones;
	
	/* Empty constructor for JAXB and Avaje */
	@SuppressWarnings("unused")
	private Game() {}
	
	public Game(HeavySpleef heavySpleef, String name) {
		this.heavySpleef = heavySpleef;
		this.name = name;
		this.ingamePlayers = Sets.newLinkedHashSet();
		this.state = GameState.WAITING;
		this.flagManager = new FlagManager(heavySpleef.getPlugin());
		this.deathzones = Sets.newLinkedHashSet();
		this.blocksBroken = HashBiMap.create();
		
		//Concurrent map for database schematic
		this.floors = new ConcurrentHashMap<String, Floor>();
		
		eventManager = new EventManager();
	}
	
	public void countdown() {
		GameCountdownEvent event = new GameCountdownEvent(this);
		eventManager.callEvent(event);
		
		// Regenerate all floors
		for (Floor floor : floors.values()) {
			floor.regenerate();
		}
		
		List<Location> spawnLocations = event.getSpawnLocations();
		if (spawnLocations == null) {
			//TODO: Define another spawn point
		}
		
		int locIndex = 0;
		for (SpleefPlayer player : ingamePlayers) {
			Location loc = spawnLocations.get(locIndex);
			
			player.getBukkitPlayer().teleport(loc);
			
			locIndex = locIndex + 1 >= spawnLocations.size() ? 0 : ++locIndex;
		}
		
		boolean countdownEnabled = event.isCountdownEnabled();
		int countdownLength = event.getCountdownLength();		
		
		state = GameState.STARTING;
		
		if (countdownEnabled && countdownLength > 0) {
			BasicTask task = new CountdownRunnable(heavySpleef, countdownLength, this);
			task.start();
		} else {
			//Countdown is not enabled so just start the game
			start();
		}
	}
	
	public void start() {
		GameStartEvent event = new GameStartEvent(this);
		eventManager.callEvent(event);
		
		state = GameState.INGAME;
		broadcast(heavySpleef.getMessage(Messages.Broadcast.GAME_STARTED));
	}
	
	public void stop() {
		GameEndEvent event = new GameEndEvent(this);
		eventManager.callEvent(event);
		
		//Create a copy of current ingame players to prevent
		//a ConcurrentModificationException
		Set<SpleefPlayer> ingamePlayersCopy = Sets.newHashSet(ingamePlayers);
		for (SpleefPlayer player : ingamePlayersCopy) {
			leave(player, QuitCause.STOP);
		}
		
		for (Floor floor : floors.values()) {
			floor.regenerate();
		}
		
		broadcast(heavySpleef.getMessage(Messages.Broadcast.GAME_STOPPED));
	}
	
	public void disable() {
		if (state == GameState.DISABLED) {
			return;
		}
		
		if (state.isGameActive()) {
			//Stop this game before disabling it
			stop();
		} else if (state == GameState.LOBBY) {
			//Create a copy of current ingame players to prevent
			//a ConcurrentModificationException
			Set<SpleefPlayer> ingamePlayersCopy = Sets.newHashSet(ingamePlayers);
			for (SpleefPlayer player : ingamePlayersCopy) {
				leave(player);
			}
		}
		
		state = GameState.DISABLED;
		
		GameDisableEvent event = new GameDisableEvent(this);
		eventManager.callEvent(event);
	}
	
	public void enable() {
		if (state.isGameEnabled()) {
			return;
		}
		
		state = GameState.WAITING;
		
		GameEnableEvent event = new GameEnableEvent(this);
		eventManager.callEvent(event);
	}
	
	public void join(SpleefPlayer player) {
		if (ingamePlayers.contains(player)) {
			return;
		}
		
		PlayerJoinGameEvent event = new PlayerJoinGameEvent(this, player);		
		eventManager.callEvent(event);
		
		if (event.getTeleportationLocation() == null) {
			//TODO: Send a message to the player?
			return;
		}
		
		JoinResult result = event.getJoinResult();
		switch (result) {
		case ALLOW:
			//Go through
			break;
		case DENY:
			return;
		case NOT_SPECIFIED:
			//Do a state check
			if (state == GameState.INGAME) {
				return;
			}
			break;
		default:
			break;
		}
		
		ingamePlayers.add(player);
		
		//Store a reference to the player state
		player.savePlayerState(this);
		PlayerStateHolder.applyDefaultState(player.getBukkitPlayer());
		
		Location location = event.getTeleportationLocation();
		player.teleport(location);
		
		broadcast(heavySpleef.getVarMessage(Messages.Broadcast.PLAYER_JOINED_GAME)
				.setVariable("player", player.getName())
				.toString());
		
		if (event.isStartGame()) {
			countdown();
		}
	}
	
	public void leave(SpleefPlayer player) {
		leave(player, QuitCause.SELF);
	}
	
	private void leave(SpleefPlayer player, QuitCause cause, Object... args) {
		if (!ingamePlayers.contains(player)) {
			return;
		}
		
		PlayerLeaveGameEvent event = new PlayerLeaveGameEvent(this, player);
		eventManager.callEvent(event);
		
		if (event.isCancelled()) {
			return;
		}
		
		ingamePlayers.remove(player);
		
		//Receive the state back
		PlayerStateHolder playerState = player.getPlayerState(this);
		if (playerState != null) {
			playerState.apply(player.getBukkitPlayer(), true);
		} else {
			//Ugh, something went wrong
			//TODO: Inform the player
		}
		
		if (ingamePlayers.size() == 0 && state == GameState.LOBBY) {
			setGameState(GameState.WAITING);
		}
		
		switch (cause) {
		case KICK:
			SpleefPlayer clientPlayer = null;
			String message = null;
			
			int messageIndex = 0;
			if (args.length > 0 && args[0] instanceof SpleefPlayer) {
				// Caller gave us a client player
				clientPlayer = (SpleefPlayer) args[0];
				messageIndex = 1;
			}
			
			if (args.length > messageIndex && args[messageIndex] instanceof String) {
				// Caller gave us a kick message
				message = (String) args[1];
			}
			
			String finalMessage = heavySpleef.getVarMessage(Messages.Player.PLAYER_KICK)
					.setVariable("message", message)
					.setVariable("kicker", clientPlayer.getName())
					.toString();
			
			player.sendMessage(finalMessage);
			break;
		case SELF:
			player.sendMessage(heavySpleef.getMessage(Messages.Player.PLAYER_LEAVE));
			break;
		case STOP:
			break;
		case LOSE:
			break;
		default:
			break;
		}
		
		broadcast(heavySpleef.getMessage(Messages.Broadcast.PLAYER_LEFT_GAME));
	}
	
	public void requestLose(SpleefPlayer player) {
		leave(player, QuitCause.LOSE);
	}
	
	public void requestWin(SpleefPlayer player) {
		
	}
	
	public void kickPlayer(SpleefPlayer player, String message) {
		if (!ingamePlayers.contains(player)) {
			throw new IllegalArgumentException("player must be in game to kick");
		}
		
		leave(player, QuitCause.KICK);
	}
	
	public String getName() {
		return name;
	}
	
	public void registerGameListener(SpleefListener listener) {
		eventManager.registerListener(listener);
	}
	
	public void addFlag(AbstractFlag<?> flag) {
		flagManager.addFlag(flag);
	}
	
	public void removeFlag(String name) {
		flagManager.removeFlag(name);
	}
	
	public boolean isFlagPresent(String flagName) {
		return flagManager.isFlagPresent(flagName);
	}
	
	public void addFloor(Floor floor) {
		floors.put(floor.getName(), floor);
	}
	
	public Floor removeFloor(String name) {
		return floors.remove(name);
	}
	
	public Floor getFloor(String name) {
		return floors.get(name);
	}
	
	public Collection<Floor> getFloors() {
		return floors.values();
	}
	
	public void addDeathzone(CuboidRegion region) {
		deathzones.add(region);
	}
	
	public boolean removeDeathzone(CuboidRegion region) {
		return deathzones.remove(region);
	}
	
	public Set<CuboidRegion> getDeathzones() {
		return deathzones;
	}
	
	public void setGameState(GameState state) {
		this.state = state;
	}
	
	public GameState getGameState() {
		return state;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(GameProperty property) {
		return (T) flagManager.getProperty(property);
	}
	
	public BiMap<SpleefPlayer, Set<Block>> getBlocksBroken() {
		return Maps.unmodifiableBiMap(blocksBroken);
	}
	
	public void broadcast(String message) {
		broadcast(BroadcastTarget.AROUND_GAME, message);
	}
	
	public void broadcast(BroadcastTarget target, String message) {
		switch (target) {
		case AROUND_GAME:
			//TODO
			break;
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
	
	public Set<SpleefPlayer> getPlayers() {
		return ingamePlayers;
	}
	
	/* Event hooks */
	@SuppressWarnings("deprecation")
	public void onPlayerInteract(PlayerInteractEvent event, SpleefPlayer player) {
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
			}
		}
		
		//TODO: Add block break to player stats (also below)
	}
	
	public void onPlayerBreakBlock(BlockBreakEvent event, SpleefPlayer player) {
		PlayerBlockBreakEvent spleefEvent = new PlayerBlockBreakEvent(this, player, event.getBlock());
		eventManager.callEvent(spleefEvent);
		
		if (spleefEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		boolean onFloor = false;
		for (Floor floor : floors.values()) {
			if (floor.contains(event.getBlock())) {
				onFloor = true;
				break;
			}
		}
		
		boolean disableBuild = getPropertyValue(GameProperty.DISABLE_BUILD);
		
		if (!onFloor && disableBuild) {
			event.setCancelled(true);
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
		boolean noHunger = getPropertyValue(GameProperty.DISABLE_HUNDER);
		if (noHunger) {
			event.setCancelled(true);
		}
	}
	
	private enum QuitCause {
		
		SELF,
		KICK,
		STOP,
		LOSE;
		
	}

}
