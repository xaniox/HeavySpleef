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
import me.matzefratze123.heavyspleef.core.task.StartCountdownTask;
import me.matzefratze123.heavyspleef.core.task.RoundsCountdownTask;
import me.matzefratze123.heavyspleef.event.SpleefFinishEvent;
import me.matzefratze123.heavyspleef.event.SpleefStartEvent;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;
import me.matzefratze123.heavyspleef.utility.LocationSaver;
import me.matzefratze123.heavyspleef.utility.PlayerStateManager;
import me.matzefratze123.heavyspleef.utility.StringUtil;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticManager;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Game {
	
	protected Map<Integer, Floor> floors = new HashMap<Integer, Floor>();
	protected Map<Integer, LoseZone> loseZones = new HashMap<Integer, LoseZone>();
	protected Map<String, Integer> knockouts = new HashMap<String, Integer>();
	protected Map<String, Integer> chancesUsed = new HashMap<String, Integer>();
	protected Map<Integer, ScoreBoard> scoreboards = new HashMap<Integer, ScoreBoard>();
	
	public    Map<String, List<Block>> brokenBlocks = new HashMap<String, List<Block>>();
	
	protected GameState state;
	
	protected Location winPoint;
	protected Location losePoint;
	protected Location preLobbyPoint;
	
	public int tid = -1;
	public int roundTid = -1;
	
	protected int reward;
	protected int jackpot = 0;
	protected int jackpotToPay;
	protected int minPlayers;
	protected int maxPlayers = 0;
	protected int autoStartAt = 0;
	protected int countdown;
	protected int chances = 0;
	
	protected boolean startOnMinPlayers = false;
	protected boolean shovels = false;
	
	protected boolean oneVsOne = false;
	protected int rounds = 2;
	protected int roundsPlayed = rounds;
	
	protected ConfigurationSection gameSection;
	
	protected String name;
	
	public List<String> players = new ArrayList<String>();
	public List<String> wereOffline = new ArrayList<String>();
	public ArrayList<Game.Win> wins = new ArrayList<Game.Win>();
	
	/**
	 * Constructs a new game with the given name
	 * 
	 * @param name The name of the game
	 */
	public Game(String name) {
		this.name = name;
		
		this.state = GameState.NOT_INGAME;
		this.gameSection = HeavySpleef.instance.database.getConfigurationSection(name);
		this.minPlayers = HeavySpleef.instance.getConfig().getInt("general.neededPlayers", 2);
		this.jackpotToPay = HeavySpleef.instance.getConfig().getInt("general.defaultToPay", 5);
		this.reward = HeavySpleef.instance.getConfig().getInt("general.defaultReward", 0);
		this.setCountdown(HeavySpleef.instance.getConfig().getInt("general.countdownFrom", 15));
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
	 * Countdowns the game
	 */
	public void countdown() {
		prepareGame();//Prepare the game
		Bukkit.getPluginManager().callEvent(new SpleefStartEvent(this)); //Call our spleef start event
		this.tid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.instance, new StartCountdownTask(countdown, this), 20L, 20L);//Let the countdown begin
		for (Player p : getPlayers())
			p.teleport(getRandomLocation()); // Teleport every player to a random location inside the arena at the start of the game
		
		setGameState(GameState.COUNTING);//Set our gamestate to counting
		updateScoreBoards();//Update scoreboards... (buggy...)
	}
	
	/**
	 * Starts the game
	 */
	public void start() {
		updateScoreBoards();
		tellAll(_("gameHasStarted"));
		broadcast(_("gameOnArenaHasStarted", getName()));
		broadcast(_("startedGameWith", String.valueOf(players.size())));
		setGameState(GameState.INGAME);
		for (Player p : getPlayers())
			StatisticManager.getStatistic(p.getName(), true).addGame();
		
		//Withdraw jackpot money
		if (HeavySpleef.hooks.hasVault() && getJackpotToPay() > 0) {
			for (Player p : getPlayers()) {
				HeavySpleef.hooks.getVaultEconomy().withdrawPlayer(p.getName(), getJackpotToPay());
				p.sendMessage(_("paidIntoJackpot", HeavySpleef.hooks.getVaultEconomy().format(getJackpotToPay())));
				this.jackpot += getJackpotToPay();
			}
		}
		
		cancelSTTask();
	}
	
	/**
	 * Stops this game
	 */
	public void stop() {
		cancelTasks();
		for (Player p : getPlayers()) {
			
			if (getLosePoint() == null) p.teleport(LocationSaver.load(p));
			else p.teleport(getLosePoint());
			
			if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState", true))
				PlayerStateManager.restorePlayerState(p);
		}
		
		Bukkit.getPluginManager().callEvent(new SpleefFinishEvent(this, StopCause.STOP));
		broadcast(_("gameStopped"));
		setGameState(GameState.NOT_INGAME);
		GameManager.removeAllPlayersFromGameQueue(this.name);
		
		regen();
		clearAll();
	}
	
	protected void clearAll() {
		roundsPlayed = rounds;
		knockouts.clear();
		wins.clear();
		players.clear();
		chancesUsed.clear();
	}
	
	/**
	 * Disables this game
	 * 
	 * @param disabler The disabler of this game (can be null) 
	 */
	public void disable(String disabler) {
		if (isDisabled())
			return;
		if (hasActivity())
			stop();
		setGameState(GameState.DISABLED);
		if (disabler != null)
			broadcast(_("gameDisabled", getName(), StringUtil.colorName(disabler)));
	}
	
	/**
	 * Enables this game
	 */
	public void enable() {
		if (!isDisabled())
			return;
		setGameState(GameState.NOT_INGAME);
	}
	
	/**
	 * Adds a player to this game
	 * 
	 * @param player The player to add
	 */
	@SuppressWarnings("deprecation")
	public void addPlayer(Player player) {
		if (isDisabled())
			return;
		if (is1vs1() && players.size() >= 2)
			return;
		players.add(player.getName());
		
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState", true))
			PlayerStateManager.savePlayerState(player);
		
		if (HeavySpleef.instance.getConfig().getBoolean("sounds.plingSound", true)) {
			for (Player p : getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 4.0F, p.getLocation().getPitch());
		}
		
		if (isWaiting()) //Activate the lobby mode
			setGameState(GameState.PRE_LOBBY);
		
		tellAll(_("playerJoinedGame", StringUtil.colorName(player.getName())));
		player.setAllowFlight(false);
		player.setFireTicks(0);
		
		if (isCounting() && this.shovels) {
			player.getInventory().addItem(getSpleefShovel());
			player.updateInventory();
		}
		
		if ((getAutoStart() > 1 && players.size() >= getAutoStart() && isPreLobby()) || (is1vs1() && players.size() >= 2 && isPreLobby()))
			countdown();
	}
	
	/**
	 * <b>Trys</b> to remove a player from this game with the given cause
	 * 
	 * @param player Player to remove
	 * @param cause Cause of lose
	 */
	public void removePlayer(Player player, LoseCause cause) {
		if (player == null)
			return;
		if (cause == null)
			cause = LoseCause.UNKNOWN;
		
		if (!players.contains(player.getName())) //Player can't be removed if they are not playing
			return;
		
		if (cause == LoseCause.LOSE) {
			if (is1vs1()) {
				nextRound(player);
				return;
			}
			
			//Chances stuff start
			//And put the chances that the player has left into the Map
			if (!chancesUsed.containsKey(player.getName()))
				chancesUsed.put(player.getName(), 1);
			else
				chancesUsed.put(player.getName(), chancesUsed.get(player.getName()) + 1);
		
			if (chancesUsed.get(player.getName()) < this.chances) {
				player.teleport(getNewRandomLocation(player));
				player.setFireTicks(0);
				int livesLeft = chances - chancesUsed.get(player.getName());
				
				player.sendMessage(_("chancesLeft", String.valueOf(livesLeft)));
				broadcast(_("chancesLeftBroadcast", StringUtil.colorName(player.getName()), String.valueOf(livesLeft)));
				return;
			}
			//Chances stuff end
			player.sendMessage(Game._("outOfGame"));
			StatisticManager.getStatistic(player.getName(), true).addLose();
		}
		
		players.remove(player.getName());
		if (!isPreLobby())
			player.sendMessage(_("yourKnockOuts", String.valueOf(getKnockouts(player))));
		broadcast(getLoseMessage(cause, player));
		if (getLosePoint() == null)
			player.teleport(LocationSaver.load(player));
		else
			player.teleport(getLosePoint());
		
		player.setFireTicks(0);
		
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState", true))
			PlayerStateManager.restorePlayerState(player);
		if (players.size() <= 0)
			setGameState(GameState.NOT_INGAME);
		if (state == GameState.INGAME || state == GameState.COUNTING) {
			if (players.size() != 1)
				return;
			
			cancelTasks();
			
			for (int i = 0; i < players.size(); i++) {
				Player winner = Bukkit.getPlayer(players.get(i));
				win(winner);
			}
		}
	}
	
	public void nextRound(Player loserr) {
		if (!isIngame())
			return;
		if (is1vs1()) {
			roundsPlayed++;//One round higher
			for (Player p : getPlayers()) {
				if (p.getName().equalsIgnoreCase(loserr.getName()))//If p is the same as the loser: continue;
					continue;
				
				add1vs1Win(p);//Add the win
				broadcast(_("wonRound", StringUtil.colorName(p.getName()), String.valueOf(roundsPlayed), String.valueOf(rounds)));//Broadcast the winner of the round
			}
			if (roundsPlayed < rounds) {//Next round?
				regen();//Regenerate floors
				
				for (Player p : getPlayers()) {
					p.teleport(getRandomLocation());//Teleport player to a random location
					p.setFireTicks(0);
				}
				
				broadcast(_("roundsRemaining", String.valueOf(rounds - roundsPlayed)));//Broadcast how many rounds remaining
				updateScoreBoards();//Update the scoreboards (Buggy...)
				if (HeavySpleef.instance.getConfig().getBoolean("general.countdownBetweenRound", true)) {//Do a countdown if it was so defined
					int start = HeavySpleef.instance.getConfig().getInt("general.countdownBetweenRoundLength", 5);
					this.roundTid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.instance, new RoundsCountdownTask(start, this), 0L, 20L);
				}
				
				return;
			} else if (roundsPlayed >= rounds) {//If not then end the game
				Win win = getHighestWin(); //Get the highest win
				updateScoreBoards();
				if (win == null) { //If it is null there is a draw
					endInDraw();
					return;
				}
				
				String loser = "null";
				for (Player p : getPlayers()) {
					if (p.getName().equalsIgnoreCase(win.getOwner()))//Compare player with the owner of the win
						continue;
					
					loser = p.getName();
				}
				
				win(Bukkit.getPlayer(win.getOwner()));//Win, broadcast and end the game!
				broadcast(_("wonThe1vs1", StringUtil.colorName(win.getOwner()), loser));
				return;
			}
		}
	}
	
	private Win getHighestWin() {
		//Returns null if there is a draw
		for (int i = 0; i < wins.size(); i++) {//First check if there is a same value in the wins list
			for (int a = 0; a < wins.size(); a++) {
				if (wins.get(i).equals(wins.get(a)))
					continue;
				if (wins.get(i).getWins() == wins.get(a).getWins()) {
					return null;//And return null if there is...
				}
			}
		}
		
		//If not we look for the highest win
		Win lastHighestWin = new Win(null, 0);
		
		for (Win w : wins) {//Loop around every win
			if (w.getWins() > lastHighestWin.getWins())//And check if it is higher as the lastHighestWin
				lastHighestWin = w; //We got the next higher win as before
		}
		
		//Finally we got the highestWin
		//And return it
		return lastHighestWin;
	}
	
	private void add1vs1Win(Player p) {
		Win win = null;
		
		for (Win w : wins) {//Indicate which win is owned by the player
			if (w.getOwner().equalsIgnoreCase(p.getName())) {
				win = w;
				break;
			}
		}
		
		if (win == null) {//Add a new win if there is no win...
			wins.add(new Win(p.getName()));
			return;
		}
		
		win.addWin();//Finally add the winpoint
	}
	
	//Returns a random location in the game where the floor is != Material.AIR
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
	
	//Just a method for a specified spleef shovel with a name
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
		
		if (getWinPoint() == null)
			p.teleport(LocationSaver.load(p));
		else
			p.teleport(getWinPoint());
		setGameState(GameState.NOT_INGAME);
		regen();
		players.remove(p.getName());
		
		for (String pl : players) {
			players.remove(Bukkit.getPlayer(pl));
			Player player = Bukkit.getPlayer(pl);
			
			if (getLosePoint() == null) Bukkit.getPlayer(pl).teleport(LocationSaver.load(p));
			else Bukkit.getPlayer(pl).teleport(getLosePoint());
			
			if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState", true))
				PlayerStateManager.restorePlayerState(player);
			
			player.setFireTicks(0);
			player.setFallDistance(0);
			StatisticManager.getStatistic(pl, true).addLose();
		}
		
		StatisticManager.getStatistic(p.getName(), true).addWin();
		
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState", true))
			PlayerStateManager.restorePlayerState(p);
		
		broadcast(_("hasWon", StringUtil.colorName(p.getName()), this.getName()));
		p.sendMessage(_("win"));
		p.sendMessage(_("yourKnockOuts", String.valueOf(getKnockouts(p))));
		clearAll();
		
		Bukkit.getPluginManager().callEvent(new SpleefFinishEvent(this, StopCause.WIN));
		
		if (HeavySpleef.instance.getConfig().getBoolean("sounds.levelUp", true))
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
	
	protected void endInDraw() {
		setGameState(GameState.NOT_INGAME);
		cancelTasks();
		regen();
		
		Bukkit.getPluginManager().callEvent(new SpleefFinishEvent(this, StopCause.DRAW));
		addPlayersFromQueue();
		
		for (Player p : getPlayers()) {
			if (getWinPoint() == null)
				p.teleport(LocationSaver.load(p));
			else
				p.teleport(getWinPoint());
			
			players.remove(p.getName());
			
			if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState", true))
				PlayerStateManager.restorePlayerState(p);
		}
		clearAll();
		broadcast(_("endedDraw", getName()));
	}
	
	private String getLoseMessage(LoseCause cause, Player player) {
		switch(cause) {
		case QUIT:
			return _("loseCause_" + cause.name().toLowerCase(), StringUtil.colorName(player.getName()));
		case KICK:
			return _("loseCause_" + cause.name().toLowerCase(), StringUtil.colorName(player.getName()));
		case LEAVE:
			return _("loseCause_" + cause.name().toLowerCase(), StringUtil.colorName(player.getName()), getName());
		case LOSE:
			return _("loseCause_" + cause.name().toLowerCase(), StringUtil.colorName(player.getName()), getKiller(player, true));
		case UNKNOWN:
			return _("loseCause_" + cause.name().toLowerCase(), StringUtil.colorName(player.getName()));
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
	
	public boolean hasActivity() {
		return isCounting() || isIngame() || isPreLobby();
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
	public void tellAll(String msg) {
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
		String[] keySetAsArray = keySet.toArray(new String[keySet.size()]);
		int addedPlayers = 0;
		
		//Can't use foreach loop, will cause a ConcurrentModificationException...
		//A foreach loop looks good, but will often cause problems if you modify the
		//Map / List while iterating over it.
		for (int i = 0; i < keySetAsArray.length; i++) {
			System.out.println(keySetAsArray[i]);
			
			if (GameManager.queues.get(keySetAsArray[i]).equalsIgnoreCase(getName())) {
				Player currentPlayer = Bukkit.getPlayer(keySetAsArray[i]);
				if (currentPlayer == null) {
					GameManager.queues.remove(keySetAsArray[i]);
					continue;
				}
				if (is1vs1() && addedPlayers >= 2)
					return;
				if (getMaxPlayers() > 1 && addedPlayers >= getMaxPlayers())
					return;
				LocationSaver.save(currentPlayer);
				currentPlayer.teleport(getPreGamePoint());
				addPlayer(currentPlayer);
				currentPlayer.sendMessage(_("noLongerInQueue"));
				GameManager.queues.remove(currentPlayer.getName());
				addedPlayers++;
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
		return HeavySpleef.PREFIX + ChatColor.RESET + " " + LanguageHandler._(key);
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
	 * @return An array containing all online players of this game
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
	@SuppressWarnings("deprecation")
	public void prepareGame() {
		regen();
		this.jackpot = 0;
		this.roundsPlayed = 0;

		for (Player p : getPlayers()) {
			
			//Give players the shovels...
			if (isShovels()) {
				p.getInventory().addItem(getSpleefShovel());
				p.updateInventory();
			}
			
			
		}
	}
	
	public void regen() {
		for (Floor floor : getFloors()) {
			if (!regen(floor.getId()))
				HeavySpleef.instance.getLogger().warning(getName() + ": Could not regenerate floor " + floor.getId() + "!");
		}
	}
	
	public boolean regen(int id) {
		if (!floors.containsKey(id))
			return false;
		
		floors.get(id).create();
		return true;
	}

	/**
	 * Decides wether this game is ready to play
	 * 
	 * @return True if the game is ready to play, otherwise false
	 */
	public boolean isFinal() {
		return preLobbyPoint != null && floors.size() > 0;
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
	public int getMinPlayers() {
		return minPlayers;
	}

	/**
	 * Sets the count of the players that are needed to start the game
	 * @param neededPlayers The count of needed players
	 */
	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}
	
	public int getMaxPlayers() {
		return this.maxPlayers;
	}
	
	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
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
	
	public void setRounds(int rounds) {
		this.rounds = rounds;
	}
	
	public int getRounds() {
		return this.rounds;
	}
	
	public void setAutoStart(int players) {
		this.autoStartAt = players;
	}
	
	public int getAutoStart() {
		return this.autoStartAt;
	}
	
	public void set1vs1(boolean oneVsOne) {
		this.oneVsOne = oneVsOne;
	}
	
	public boolean is1vs1() {
		return this.oneVsOne;
	}
	
	public int getCurrentRound() {
		return this.roundsPlayed + 1;//Add one
	}
	
	public int[] getWins() {
		int[] wins = new int[2];
		
		for (int i = 0; i < 2; i++) {
			if (i >= this.wins.size()) {
				wins[i] = 0;
				continue;
			}
			Win w = this.wins.get(i);
			wins[i] = w.getWins();
		}
		
		return wins;
	}
	
	public void addScoreBoard(Location loc, BlockFace face) {
		int id = 0;
		while (scoreboards.containsKey(id))
			id++;
		
		ScoreBoard board = new ScoreBoard(loc, id, this, face);
		scoreboards.put(id, board);
		
		board.draw();
	}
	
	public void addScoreBoard(ScoreBoard board) {
		scoreboards.put(board.getId(), board);
		board.draw();
	}
	
	public boolean removeScoreBoard(int id) {
		if (!scoreboards.containsKey(id))
			return false;
		
		scoreboards.remove(id);
		return true;
	}
	
	public boolean canSpleef(Location location, Player p) {
		if (!players.contains(p.getName()))
			return false;
		if (!isIngame())
			return false;
		if (!containsInner(location))
			return false;
		
		for (Floor floor : getFloors()) {
			if (!floor.contains(location))
				continue;
			return true;
		}
		
		return false;
	}
	
	public boolean canSpleef(Block block, Player p) {
		return canSpleef(block.getLocation(), p);
	}
	
	public ScoreBoard getScoreBoard(int id) {
		return scoreboards.get(id);
	}
	
	public ScoreBoard[] getScoreBoards() {
		return scoreboards.values().toArray(new ScoreBoard[scoreboards.size()]);
	}
	
	public void updateScoreBoards() {
		for (ScoreBoard board : getScoreBoards()) {
			board.draw();
		}
	}
	
	private void cancelTasks() {
		cancelSTTask();
		cancelRCTask();
	}
	
	private void cancelRCTask() {
		if (Bukkit.getScheduler().isQueued(this.roundTid) && roundTid != -1) {
			Bukkit.getScheduler().cancelTask(this.roundTid);
			this.roundTid = -1;
		}
	}
	
	private void cancelSTTask() {
		if (Bukkit.getScheduler().isQueued(this.tid) && tid != -1) {
			Bukkit.getScheduler().cancelTask(this.tid);
			this.tid = -1;
		}
	}
	
	//Just a simple class to save a 1vs1 win
	public class Win implements Cloneable {
		
		String owner;
		int wins = 1;
		
		public Win(String owner) {
			this.owner = owner;
		}
		
		public Win(String owner, int wins) {
			this.owner = owner;
			this.wins = wins;
		}
		
		public int getWins() {
			return this.wins;
		}
		
		public void addWin() {
			this.wins++;
		}
		
		public String getOwner() {
			return this.owner;
		}
		
		@Override
		public Win clone() {
			return new Win(this.owner, this.wins);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (!(o instanceof Win))
				return false;
			Win win = (Win)o;
			
			if (!win.getOwner().equalsIgnoreCase(getOwner()))
				return false;
			if (win.getWins() != getWins())
				return false;
			
			return true;
		}
		
	}
	
	
}
