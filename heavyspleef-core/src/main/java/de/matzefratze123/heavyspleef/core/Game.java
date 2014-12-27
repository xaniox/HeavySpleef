package de.matzefratze123.heavyspleef.core;

import static de.matzefratze123.heavyspleef.core.HeavySpleef.PREFIX;

import java.util.Collection;
import java.util.Iterator;
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

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
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
import de.matzefratze123.heavyspleef.core.event.PlayerLoseGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
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
	@Transient
	@XmlTransient
	private KillDetector killDetector;
	
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
		this.killDetector = new DefaultKillDetector();
		
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
			player.sendMessage(heavySpleef.getMessage(Messages.Player.ERROR_ON_INVENTORY_LOAD));
		}
		
		if (ingamePlayers.size() == 0 && state == GameState.LOBBY) {
			setGameState(GameState.WAITING);
		}
		
		String broadcastMessage = heavySpleef.getVarMessage(Messages.Broadcast.PLAYER_LEFT_GAME)
				.setVariable("player", player.getName())
				.toString();
		String playerMessage = heavySpleef.getMessage(Messages.Player.PLAYER_LEAVE);
		
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
			
			playerMessage = heavySpleef.getVarMessage(Messages.Player.PLAYER_KICK)
					.setVariable("message", message)
					.setVariable("kicker", clientPlayer.getName())
					.toString();
			break;
		case SELF:
			playerMessage = heavySpleef.getMessage(Messages.Player.PLAYER_LEAVE);
			break;
		case STOP:
			playerMessage = heavySpleef.getMessage(Messages.Player.GAME_STOPPED);
			break;
		case LOSE:
			String killer = args.length > 0 ? (String)args[0] : "unknown";
			broadcastMessage = heavySpleef.getVarMessage(Messages.Broadcast.PLAYER_LOST_GAME)
					.setVariable("player", player.getName())
					.setVariable("killer", killer)
					.toString();
			
			playerMessage = heavySpleef.getMessage(Messages.Player.PLAYER_LOSE);
			break;
		case WIN:
			broadcastMessage = heavySpleef.getVarMessage(Messages.Broadcast.PLAYER_WON_GAME)
					.setVariable("player", player.getName())
					.toString();
			
			playerMessage = heavySpleef.getMessage(Messages.Player.PLAYER_WIN);
		default:
			break;
		}
		
		broadcast(broadcastMessage);
		player.sendMessage(playerMessage);
	}
	
	public void requestLose(SpleefPlayer player) {
		if (ingamePlayers.contains(player)) {
			return;
		}
		
		PlayerLoseGameEvent event = new PlayerLoseGameEvent(this, player);
		eventManager.callEvent(event);
		
		final OfflinePlayer killer = killDetector.detectKiller(this, player);
		leave(player, QuitCause.LOSE, killer != null ? killer.getName() : null);
		
		if (ingamePlayers.size() == 1) {
			SpleefPlayer playerLeft = ingamePlayers.iterator().next();
			requestWin(playerLeft);
		}
	}
	
	public void requestWin(SpleefPlayer player) {
		PlayerWinGameEvent event = new PlayerWinGameEvent(this, player);
		eventManager.callEvent(event);
		
		for (SpleefPlayer ingamePlayer : ingamePlayers) {
			if (ingamePlayer == player) {
				continue;
			}
			
			requestLose(ingamePlayer);
		}
		
		leave(player, QuitCause.WIN);
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
	
	private void addBlockBroken(SpleefPlayer player, Block brokenBlock) {
		Set<Block> set = blocksBroken.get(player);
		if (set == null) {
			set = Sets.newHashSet();
			blocksBroken.put(player, set);
		}
		
		set.add(brokenBlock);
	}
	
	public BiMap<SpleefPlayer, Set<Block>> getBlocksBroken() {
		return Maps.unmodifiableBiMap(blocksBroken);
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
				
				addBlockBroken(player, block);
			}
		}
	}
	
	public void onPlayerBreakBlock(BlockBreakEvent event, SpleefPlayer player) {
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
	
	private enum QuitCause {
		
		SELF,
		KICK,
		STOP,
		LOSE,
		WIN;
		
	}

}
