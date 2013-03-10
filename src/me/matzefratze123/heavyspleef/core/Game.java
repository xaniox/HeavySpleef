/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
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
package me.matzefratze123.heavyspleef.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.region.Floor;
import me.matzefratze123.heavyspleef.core.region.LoseZone;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;
import me.matzefratze123.heavyspleef.utility.PlayerStateManager;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticManager;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Game {
	
	protected Map<Integer, Floor> floors = new HashMap<Integer, Floor>();
	protected Map<Integer, LoseZone> loseZones = new HashMap<Integer, LoseZone>();
	protected Map<String, Integer> knockouts = new HashMap<String, Integer>();
	protected Map<String, Integer> chancesLeft = new HashMap<String, Integer>();
	
	public  Map<String, List<Block>> brokenBlocks = new HashMap<String, List<Block>>();
	
	protected GameState state;
	
	protected Location winPoint;
	protected Location losePoint;
	protected Location preLobbyPoint;
	
	protected int reward;
	protected int jackpot = 0;
	protected int jackpotToPay;
	protected int neededPlayers;
	protected int countdown;
	protected int chances = 1;
	
	protected boolean startOnMinPlayers = false;
	protected boolean shovels = false;
	
	protected ConfigurationSection gameSection;
	
	protected String name;
	
	public List<String> players = new ArrayList<String>();
	public List<String> wereOffline = new ArrayList<String>();
	
	/**
	 * Constructs a new game with the given name
	 * 
	 * @param name The name of the game
	 */
	public Game(String name) {
		this.name = name;
		
		this.state = GameState.NOT_INGAME;
		this.gameSection = HeavySpleef.instance.database.getConfigurationSection(name);
		this.neededPlayers = HeavySpleef.instance.getConfig().getInt("general.neededPlayers");
		this.jackpotToPay = HeavySpleef.instance.getConfig().getInt("general.defaultToPay");
		this.reward = HeavySpleef.instance.getConfig().getInt("general.defaultReward");
		this.setCountdown(HeavySpleef.instance.getConfig().getInt("general.countdownFrom"));
	}
	
	/**
	 * Gets the type of this game
	 * 
	 * @return The type of the game
	 */
	public abstract Type getType();
	
	/**
	 * Checks if a Location is inside the game (including walls)
	 * 
	 * @param l The location to check
	 * @return True if the game contains the location
	 */
	public abstract boolean contains(Location l);
	
	/**
	 * Checks if a Location is inside the game (excluding walls)
	 * 
	 * @param l The location to check
	 * @return True if the game contains the location
	 */
	public abstract boolean containsInner(Location l);
	
	/**
	 * Checks if a Block is inside the game (including walls)
	 * 
	 * @param b The block to check
	 * @return True if the game contains the block
	 */
	public boolean contains(Block b) {
		return contains(b.getLocation());
	}
	
	/**
	 * Broadcasts a message to this game
	 * 
	 * @param msg The message to send
	 */
	public abstract void broadcast(String msg);
	
	/**
	 * Gets a random location inside this arena
	 * 
	 * @return A random location inside the arena
	 */
	public abstract Location getRandomLocation();
	
	/**
	 * Adds a floor to this game
	 * 
	 * @param m The material of the floor
	 * @param data The data of the floor
	 * @param wool True if the floor should change wool colors
	 * @param givenFloor True if the floor was manually builded
	 * @param locations The locations that this floor should contain (Center at a cylindergame, Two Corners at a cuboidgame)
	 */
	public abstract int addFloor(int blockID, byte data, boolean wool, boolean givenFloor, Location... locations);
	
	/**
	 * Removes a floor from this game
	 */
	public abstract void removeFloor(int id);
	
	/**
	 * Adds a losezone to this game
	 * @param locations The two corners of the losezones
	 */
	public abstract int addLoseZone(Location... locations);
	
	/**
	 * Generates the arena of the game
	 */
	protected abstract void generate();
	

	/**
	 * Starts the game (without countdown)
	 */
	@SuppressWarnings("deprecation")
	public void start() {
		this.jackpot = 0;
		if (HeavySpleef.hooks.hasVault()) {
			if (getJackpotToPay() > 0) {
				for (Player p : getPlayers()) {
					HeavySpleef.hooks.getVaultEconomy().withdrawPlayer(p.getName(), getJackpotToPay());
					p.sendMessage(_("paidIntoJackpot", HeavySpleef.hooks.getVaultEconomy().format(getJackpotToPay())));
					this.jackpot += getJackpotToPay();
				}
			}
		}
		int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.instance, new CountingTask(countdown, this.name), 20L, 20L);
		GameManager.tasks.put(this.name, taskID);
		if (isShovels()) {
			for (Player p : getPlayers()) {
				p.getInventory().addItem(getSpleefShovel());
				p.updateInventory();
			}
		}
	}
	
	/**
	 * Stops this game
	 */
	public void stop() {
		if (GameManager.tasks.containsKey(getName()))
			Bukkit.getScheduler().cancelTask(GameManager.tasks.get(getName()));
		for (String playerName : players) {
			Player player = Bukkit.getPlayer(playerName);
			if (player == null)
				continue;
			player.teleport(getLosePoint());
			if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
				PlayerStateManager.restorePlayerState(player);
		}
		broadcast(_("gameStopped"));
		setGameState(GameState.NOT_INGAME);
		GameManager.removeAllPlayersFromGameQueue(this.name);
		players.clear();
		chancesLeft.clear();
	}
	
	/**
	 * Disables this game
	 * 
	 * @param disabler The disabler of this game (can be also null) 
	 */
	public void disable(String disabler) {
		if (isCounting() || isPreLobby() || isIngame())
			stop();
		setGameState(GameState.DISABLED);
		if (disabler != null)
			broadcast(_("gameDisabled", getName(), disabler));
	}
	
	/**
	 * Enables this game
	 */
	public void enable() {
		setGameState(GameState.NOT_INGAME);
	}
	
	/**
	 * Adds a player to this game
	 * 
	 * @param player The player to add
	 */
	@SuppressWarnings("deprecation")
	public void addPlayer(Player player) {
		players.add(player.getName());
		
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
			PlayerStateManager.savePlayerState(player);
		
		if (HeavySpleef.instance.getConfig().getBoolean("sounds.plingSound")) {
			for (Player p : getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 4.0F, p.getLocation().getPitch());
		}
		
		if (isWaiting() && !isCounting() && !isIngame())
			setGameState(GameState.PRE_LOBBY);
		
		tellAll(_("playerJoinedGame", player.getName()));
		if (HeavySpleef.hooks.hasVault() && getJackpotToPay() > 0 && isCounting()) {
			HeavySpleef.hooks.getVaultEconomy().withdrawPlayer(player.getName(), getJackpotToPay());
			player.sendMessage(_("paidIntoJackpot", HeavySpleef.hooks.getVaultEconomy().format(getJackpotToPay())));
			this.jackpot += getJackpotToPay();
		}
		
		if (isCounting() && this.shovels) {
			player.getInventory().addItem(getSpleefShovel());
			player.updateInventory();
		}
		
		if (startsOnMinPlayers() && players.size() >= getNeededPlayers() && isPreLobby())
			start();
	}
	
	/**
	 * Removes a player from this game with the given cause
	 * 
	 * @param player Player to remove
	 * @param cause Cause of lose
	 */
	public void removePlayer(Player player, LoseCause cause) {
		if (player == null)
			return;
		if (cause == null)
			return;
		
		if (!players.contains(player.getName()))
			return;
		if (cause == LoseCause.LOSE) {
			StatisticManager.getStatistic(player.getName(), true).addLose();
			if (!chancesLeft.containsKey(player.getName()))
				chancesLeft.put(player.getName(), 1);
			else
				chancesLeft.put(player.getName(), chancesLeft.get(player.getName()) + 1);
		
			if (chancesLeft.get(player.getName()) < this.chances) {
				player.teleport(getNewRandomLocation(player));
				int livesLeft = chances - chancesLeft.get(player.getName()) - 1;
				
				player.sendMessage(_("chancesLeft", String.valueOf(livesLeft)));
				broadcast(_("chancesLeftBroadcast", player.getName(), String.valueOf(livesLeft)));
				return;
			}
			player.sendMessage(Game._("outOfGame"));	
		}
		
		players.remove(player.getName());
		if (!isPreLobby())
			player.sendMessage(_("yourKnockOuts", String.valueOf(getKnockouts(player))));
		broadcast(getLoseMessage(cause, player));
		player.teleport(getLosePoint());
		player.setFireTicks(0);
		
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
			PlayerStateManager.restorePlayerState(player);
		if (players.size() <= 0)
			setGameState(GameState.NOT_INGAME);
		if (state == GameState.INGAME || state == GameState.COUNTING) {
			if (players.size() != 1)
				return;
			if (GameManager.tasks.containsKey(this.getName()))
				Bukkit.getScheduler().cancelTask(GameManager.getTaskID(getName()));
			for (int i = 0; i < players.size(); i++) {
				Player winner = Bukkit.getPlayer(players.get(i));
				this.win(winner);
			}
		}
	}
	
	private Location getNewRandomLocation(Player player) {
		Location loc = null;
		int count = 0;
		
		do {
			loc = getRandomLocation();
			count++;
			
			if (count > 59) {
				return loc;
			}
		} while (loc.getBlock().getRelative(0, -1, 0).getType() == Material.AIR);
		
		loc.setY(loc.getBlockY() + 1);
		return loc;
	}
	
	private ItemStack getSpleefShovel() {
		ItemStack shovel = new ItemStack(Material.DIAMOND_SPADE, 1);
		ItemMeta meta = shovel.getItemMeta();
		
		meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Spleef Shovel");
		
		shovel.setItemMeta(meta);
		
		return shovel;
	}
	
	/**
	 * Let a player win a game
	 * 
	 * @param p The player that should win...
	 */
	protected void win(Player p) {
		if (p == null) //No NPEs
			return;
		
		p.teleport(getWinPoint());
		setGameState(GameState.NOT_INGAME);
		setupFloors();
		players.remove(p.getName());
		for (String pl : players) {
			players.remove(Bukkit.getPlayer(pl));
			Bukkit.getPlayer(pl).teleport(getLosePoint());
		}
		players.clear();
		chancesLeft.clear();
		StatisticManager.getStatistic(p.getName(), true).addWin();
		
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
			PlayerStateManager.restorePlayerState(p);
		
		broadcast(_("hasWon", p.getName(), this.getName()));
		p.sendMessage(_("win"));
		p.sendMessage(_("yourKnockOuts", String.valueOf(getKnockouts(p))));
		this.brokenBlocks.clear();
		this.knockouts.clear();
		if (HeavySpleef.instance.getConfig().getBoolean("sounds.levelUp"))
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 4.0F, p.getLocation().getPitch());
		addPlayersFromQueue();
		if (HeavySpleef.hooks.hasVault()) {
			if (this.jackpot > 0) {
				EconomyResponse r = HeavySpleef.hooks.getVaultEconomy().depositPlayer(p.getName(), this.jackpot);
				p.sendMessage(_("jackpotReceived", HeavySpleef.hooks.getVaultEconomy().format(r.amount)));
				this.jackpot = 0;
			}
			if (reward > 0) {
				EconomyResponse r = HeavySpleef.hooks.getVaultEconomy().depositPlayer(p.getName(), getReward());
				p.sendMessage(_("rewardReceived", HeavySpleef.hooks.getVaultEconomy().format(r.amount)));
			}
		}
	}
	
	private String getLoseMessage(LoseCause cause, Player player) {
		switch(cause) {
		case QUIT:
			return _("loseCause_" + cause.name().toLowerCase(), player.getName());
		case KICK:
			return _("loseCause_" + cause.name().toLowerCase(), player.getName());
		case LEAVE:
			return _("loseCause_" + cause.name().toLowerCase(), player.getName(), getName());
		case LOSE:
			return _("loseCause_" + cause.name().toLowerCase(), player.getName(), getKiller(player, true));
		case UNKNOWN:
			return _("loseCause_" + cause.name().toLowerCase(), player.getName());
		default:
			return "null...";
		}
	}
	
	/**
	 * Gets the killer of a player
	 * 
	 * @param player The player that was knocked out
	 * @param addKnockout Wether a knockout should be added to the killer
	 * 
	 * @return The name of the killer
	 */
	public String getKiller(Player player, boolean addKnockout) {
		Floor lowerMost = getLowermostFloor();
		
		for (String name : brokenBlocks.keySet()) {
			List<Block> blocks = brokenBlocks.get(name);
			for (Block block : blocks) {
				if (block.getY() != lowerMost.getY())
					continue;
				
				int differenceX = block.getX() < player.getLocation().getBlockX() ? player.getLocation().getBlockX() - block.getX() : block.getX() - player.getLocation().getBlockX();
				int differenceZ = block.getZ() < player.getLocation().getBlockZ() ? player.getLocation().getBlockZ() - block.getZ() : block.getZ() - player.getLocation().getBlockZ();
				
				if (differenceX == 0 && differenceZ == 0) {
					if (addKnockout)
						addKnockout(name);
					return name;
				}
				
			}
		}
		
		//If nothing was found in the hashmap, it should be the AntiCamping feature...
		return "AntiCamping";
	}
	
	/**
	 * Adds a knockout to a player
	 * 
	 * @param player The playername to add
	 */
	public void addKnockout(String player) {
		if (knockouts.containsKey(player))
			knockouts.put(player, knockouts.get(player) + 1);
		else
			knockouts.put(player, 1);
		StatisticManager.getStatistic(player, true).addKnockout();
	}
	
	/**
	 * Gets the knockouts of a player
	 * @param player The player to get
	 * @return The knockouts
	 */
	public int getKnockouts(Player player) {
		if (knockouts.containsKey(player.getName()))
			return knockouts.get(player.getName());
		return 0;
	}

	/**
	 * Sets the game to the given state
	 * @param state The state to set
	 */
	public void setGameState(GameState state) {
		this.state = state;
	}
	
	/**
	 * Gets the gamestate of this game
	 * @return The gamestate of the game
	 */
	public GameState getGameState() {
		return state;
	}
	
	/**
	 * Checks wether this game is ingame
	 * @return True if the game is ingame
	 */
	public boolean isIngame() {
		return state == GameState.INGAME;
	}
	
	/**
	 * Checks wether this game is counting
	 * @return True if the game is counting
	 */
	public boolean isCounting() {
		return state == GameState.COUNTING;
	}
	
	/**
	 * Checks wether this game is waiting, or is not ingame
	 * @return True if the game is waiting
	 */
	public boolean isWaiting() {
		return state == GameState.NOT_INGAME;
	}
	
	/**
	 * Checks wether this game is in the lobby state
	 * @return True if the game is in the lobby state
	 */
	public boolean isPreLobby() {
		return state == GameState.PRE_LOBBY;
	}
	
	/**
	 * Checks wether this game is disabled
	 * @return True if the game is disabled
	 */
	public boolean isDisabled() {
		return state == GameState.DISABLED;
	}
	
	/**
	 * Gets the winpoint of this game
	 * @return The location of the winpoint
	 */
	public Location getWinPoint() {
		return winPoint;
	}

	/**
	 * Sets the winpoint of this game
	 * @param winPoint The new winpoint
	 */
	public void setWinPoint(Location winPoint) {
		this.winPoint = winPoint;
	}

	/**
	 * Gets the losepoint of this game
	 * @return The location of the losepoint
	 */
	public Location getLosePoint() {
		return losePoint;
	}

	/**
	 * Sets the losepoint of this game
	 * @param losePoint The new losepoint
	 */
	public void setLosePoint(Location losePoint) {
		this.losePoint = losePoint;
	}
	
	/**
	 * Adds a existing floor to this game
	 * 
	 * @param floor The floor to add
	 * @param create Wether this floor should be created
	 * @return True if the floor was added
	 */
	public boolean addFloor(Floor floor, boolean create) {
		if (floors.containsKey(floor.getId()))
			return false;
		floors.put(floor.getId(), floor);
		if (create)
			floor.create();
		return true;
	}
	
	/**
	 * Checks wether this game has a floor with the given id
	 * 
	 * @param id The id to check
	 * @return True if the game has a floor with this id
	 */
	public boolean hasFloor(int id) {
		return floors.containsKey(id);
	}
	
	/**
	 * Gets the number how many floors this game contains
	 * 
	 * @return The size of the floors
	 */
	public int getFloorSize() {
		return floors.size();
	}
	
	/**
	 * Adds a existing losezone to this game
	 * 
	 * @param loseZone The losezone to add
	 * @return True if the losezone was added
	 */
	public boolean addLoseZone(LoseZone loseZone) {
		if (loseZones.containsKey(loseZone.getId()))
			return false;
		loseZones.put(loseZone.getId(), loseZone);
		return true;
	}
	
	/**
	 * Removes a losezone
	 * 
	 * @param id The id of the losezone
	 */
	public void removeLoseZone(int id) {
		loseZones.remove(id);
	}
	
	/**
	 * Checks wether this game has a losezone with the given id
	 * 
	 * @param id The id to check
	 * @return True if the game has a losezone with this id
	 */
	public boolean hasLoseZone(int id) {
		return loseZones.containsKey(id);
	}
	
	/**
	 * Gets the number how many losezones this game contains
	 * 
	 * @return The size of the losezones
	 */
	public int getLoseZoneSize() {
		return loseZones.size();
	}
	
	/**
	 * Gets all floors of this game
	 * @return A collection containing all floors
	 */
	public Collection<Floor> getFloors() {
		return floors.values();
	}
	
	/**
	 * Gets all losezones of this game
	 * @return A collection containing all losezones
	 */
	public Collection<LoseZone> getLoseZones() {
		return loseZones.values();
	}
	
	/**
	 * Tells a message to all ingame players
	 * @param msg The message to send
	 */
	protected void tellAll(String msg) {
		for (Player p : getPlayers()) {
			p.sendMessage(msg);
		}
	}
	
	/**
	 * Gets the lowermost floor of this game
	 * @return The lowermost floor
	 */
	public Floor getLowermostFloor() {
		Map<Integer, Floor> floorsWithY = new HashMap<Integer, Floor>();
		
		for (Floor f : getFloors()) {
			int minY = f.getY();
			floorsWithY.put(minY, f);
		}
		
		Integer[] keySet = floorsWithY.keySet().toArray(new Integer[floorsWithY.size()]);
		Arrays.sort(keySet);
		return floorsWithY.get(keySet[0]);
	}
	
	/**
	 * Gets the highest floor of this game
	 * @return The highest game
	 */
	public Floor getHighestFloor() {
		Map<Integer, Floor> floorsWithY = new HashMap<Integer, Floor>();
		
		for (Floor f : getFloors()) {
			int minY = f.getY();
			floorsWithY.put(minY, f);
		}
		
		Integer[] keySet = floorsWithY.keySet().toArray(new Integer[floorsWithY.size()]);
		Arrays.sort(keySet);
		return floorsWithY.get(keySet[keySet.length - 1]);
	}

	/**
	 * Adds all players from the queue in this game
	 */
	private void addPlayersFromQueue() {
		Collection<String> keySet = GameManager.queues.keySet();
		
		//Can't use foreach loop, will cause a ConcurrentModificationException...
		//A foreach loop looks good, but will often cause problems.
		for (int i = 0; i < keySet.size(); i++) {
			if (GameManager.queues.get(name).equalsIgnoreCase(getName())) {
				Player currentPlayer = Bukkit.getPlayer(name);
				if (currentPlayer == null) {
					GameManager.queues.remove(name);
					continue;
				}
				currentPlayer.teleport(getPreGamePoint());
				addPlayer(currentPlayer);
				currentPlayer.sendMessage(_("noLongerInQueue"));
				GameManager.queues.remove(currentPlayer.getName());
			}
		}
	}

	/**
	 * Stylizes the value of this keystring
	 * 
	 * @param key The key of the message (language)
	 * @return The messagevalue with a HeavySpleef prefix
	 */
	public static String _(String... key) {
		return ChatColor.RED + "[" + ChatColor.GOLD + HeavySpleef.PREFIX + ChatColor.RED + "] " + ChatColor.RESET + LanguageHandler._(key);
	}
	
	/**
	 * Gets the message of the given key
	 * 
	 * @param key The key of this message
	 * @return The messagevalue of the given key
	 */
	public static String __(String... key) {
		return LanguageHandler._(key);
	}
	
	/**
	 * Gets all players of this game
	 * 
	 * @return An array containing all players of this game
	 */
	public Player[] getPlayers() {
		String[] playersAsString = players.toArray(new String[players.size()]);
		ArrayList<Player> pList = new ArrayList<Player>(); 
		for (String player : playersAsString) {
			Player p = Bukkit.getPlayer(player);
			if (p == null)
				continue;
			pList.add(p);
		}
		return pList.toArray(new Player[pList.size()]);
	}

	/**
	 * Gets the lobby point of this game
	 * @return The location of the lobby point
	 */
	public Location getPreGamePoint() {
		return preLobbyPoint;
	}

	/**
	 * Sets the lobby point
	 * @param preLobbyPoint The location of the point
	 */
	public void setPreGamePoint(Location preLobbyPoint) {
		this.preLobbyPoint = preLobbyPoint;
	}
	
	/**
	 * Setup the floors of this game and creates all of this floors
	 */
	public void setupFloors() {
		for (Floor floor : floors.values()) {
			floor.create();
		}
	}

	/**
	 * Decides wether this game is ready to play
	 * 
	 * @return True if the game is ready to play, otherwise false
	 */
	public boolean isFinal() {
		return winPoint != null && losePoint != null && preLobbyPoint != null && floors.size() > 0;
	}

	/**
	 * Get's the name of the game
	 * 
	 * @return The name of the game
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the configurationsection of this game on the disk
	 * @return The configurationsection of this game
	 */
	public ConfigurationSection getGameSection() {
		return gameSection;
	}

	/**
	 * Gets the count of players that are needed to start this game
	 * @return The count of players
	 */
	public int getNeededPlayers() {
		return neededPlayers;
	}

	/**
	 * Sets the count of the players that are needed to start the game
	 * @param neededPlayers The count of needed players
	 */
	public void setNeededPlayers(int neededPlayers) {
		this.neededPlayers = neededPlayers;
	}

	/**
	 * Get's the money that every player has to pay
	 * into the jackpot at the beginning of the game
	 * 
	 * @return The money value
	 */
	public int getJackpotToPay() {
		return this.jackpotToPay;
	}

	/**
	 * Set's the money that every player has to pay
	 * into the jackpot at the beginning of the game
	 * 
	 * @param money Value to set
	 */
	public void setJackpotToPay(int jackpotToPay) {
		this.jackpotToPay = jackpotToPay;
	}
	
	/**
	 * Gets the reward of this game
	 * @return The reward
	 */
	public int getReward() {
		return this.reward;
	}
	
	/**
	 * Sets the reward of this game
	 * @param reward The reward to set
	 */
	public void setReward(int reward) {
		this.reward = reward;
	}
	
	/**
	 * Adds a broken block to the game
	 * 
	 * @param p The player that broke this block
	 * @param b The block that was broken
	 */
	public void addBrokenBlock(Player p, Block b) {
		if (brokenBlocks.containsKey(p.getName()))
			brokenBlocks.get(p.getName()).add(b);
		else {
			List<Block> blocks = new ArrayList<Block>();
			blocks.add(b);
			brokenBlocks.put(p.getName(), blocks);
		}
	}

	/**
	 * Set's the value if the game should start </br>
	 * when the minimum count of players are reached.
	 * 
	 * @param startOnMinPlayers The value
	 */
	public void setStartOnMinPlayers(boolean startOnMinPlayers) {
		this.startOnMinPlayers = startOnMinPlayers;
	}

	/**
	 * Set's the countdown to the specified value
	 * 
	 * @param countdown The countdown to set
	 */
	public void setCountdown(int countdown) {
		this.countdown = countdown;
	}
	
	/**
	 * Get's the countdown of the game
	 * 
	 * @return The countdown
	 */
	public int getCountdown() {
		return this.countdown;
	}
	
	/**
	 * Checks wether the game starts when the </br>
	 * minimum count of players are reached...
	 * 
	 * @return True if the game starts on min players, otherwise false
	 */
	public boolean startsOnMinPlayers() {
		return this.startOnMinPlayers;
	}

	/**
	 * Checks if this game starts with shovels
	 * 
	 * @return True if the game starts with shovels, otherwise false
	 */
	public boolean isShovels() {
		return shovels;
	}

	/**
	 * Set's the value if this game should start with shovels
	 * 
	 * @param shovels The value
	 */
	public void setShovels(boolean shovels) {
		this.shovels = shovels;
	}
	
	/**
	 * Sets the value of chances that every player has before he is out of the game
	 * 
	 * @param chances
	 */
	public void setChances(int chances) {
		this.chances = chances;
	}
	
	/**
	 * Gets the chances
	 * 
	 * @return The chances that every player has
	 */
	public int getChances() {
		return this.chances;
	}
	
	
}
