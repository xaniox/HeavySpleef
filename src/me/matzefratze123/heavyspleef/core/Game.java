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

import static me.matzefratze123.heavyspleef.core.flag.FlagType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.api.GameData;
import me.matzefratze123.heavyspleef.api.event.SpleefFinishEvent;
import me.matzefratze123.heavyspleef.api.event.SpleefJoinEvent;
import me.matzefratze123.heavyspleef.api.event.SpleefLoseEvent;
import me.matzefratze123.heavyspleef.api.event.SpleefStartEvent;
import me.matzefratze123.heavyspleef.core.flag.Flag;
import me.matzefratze123.heavyspleef.core.flag.enums.Difficulty;
import me.matzefratze123.heavyspleef.core.region.Floor;
import me.matzefratze123.heavyspleef.core.region.FloorType;
import me.matzefratze123.heavyspleef.core.region.LoseZone;
import me.matzefratze123.heavyspleef.core.task.RoundsCountdownTask;
import me.matzefratze123.heavyspleef.core.task.StartCountdownTask;
import me.matzefratze123.heavyspleef.core.task.TimeoutTask;
import me.matzefratze123.heavyspleef.hooks.VaultHook;
import me.matzefratze123.heavyspleef.hooks.WorldEditHook;
import me.matzefratze123.heavyspleef.stats.StatisticManager;
import me.matzefratze123.heavyspleef.util.ArrayHelper;
import me.matzefratze123.heavyspleef.util.LanguageHandler;
import me.matzefratze123.heavyspleef.util.LocationSaver;
import me.matzefratze123.heavyspleef.util.PlayerStateManager;
import me.matzefratze123.heavyspleef.util.SimpleBlockData;
import me.matzefratze123.heavyspleef.util.Util;
import me.matzefratze123.heavyspleef.util.ViPManager;

import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public abstract class Game {

	protected Map<Integer, Floor> floors = new HashMap<Integer, Floor>();
	protected Map<Integer, LoseZone> loseZones = new HashMap<Integer, LoseZone>();
	protected Map<String, Integer> knockouts = new HashMap<String, Integer>();
	protected Map<Team, Integer> teamKnockouts = new HashMap<Team, Integer>();
	protected Map<String, Integer> chancesUsed = new HashMap<String, Integer>();
	protected Map<Integer, ScoreBoard> scoreboards = new HashMap<Integer, ScoreBoard>();
	protected Map<Integer, SignWall> walls = new HashMap<Integer, SignWall>();
	protected Map<Flag<?>, Object> flags = new HashMap<Flag<?>, Object>();
	protected Map<String, Integer> multiKnockoutTaskIds = new HashMap<String, Integer>();
	protected Map<String, Integer> multiKnockouts = new HashMap<String, Integer>();
	protected Map<String, Scoreboard> previousScoreboards = new HashMap<String, Scoreboard>();

	public static Map<String, Integer> pvpTimerTasks = new HashMap<String, Integer>();

	public Map<String, List<Block>> brokenBlocks = new HashMap<String, List<Block>>();

	protected GameState state;

	// The minecraft sidebar scoreboard
	protected Scoreboard board = null;

	public int tid = -1;
	public int roundTid = -1;
	public int timeoutTid = -1;

	protected int jackpot = 0;
	protected int currentCountdown = 0;
	protected int roundsPlayed = 0;

	protected ConfigurationSection gameSection;

	protected String name;

	public List<String> voted = new ArrayList<String>();
	public List<String> players = new ArrayList<String>();
	public List<String> outPlayers = new ArrayList<String>();
	public List<String> wereOffline = new ArrayList<String>();
	public List<Block> modfiedBlocks = new ArrayList<Block>();
	public List<Team> teams = new ArrayList<Team>();
	public List<Queue> queues = new ArrayList<Queue>();

	private List<SimpleBlockData> toUndo = new ArrayList<SimpleBlockData>();

	public ArrayList<Game.Win> wins = new ArrayList<Game.Win>();

	/**
	 * Constructs a new game with the given name
	 * 
	 * @param name
	 *            The name of the game
	 */
	public Game(String name) {
		this.name = name;

		this.state = GameState.JOINABLE;
		this.gameSection = HeavySpleef.instance.database
				.getConfigurationSection(name);
	}

	/**
	 * Gets the type of this game
	 * 
	 * @return The type of the game
	 */
	public abstract Type getType();

	/**
	 * Checks if a Location is inside the game
	 * 
	 * @param l
	 *            The location to check
	 * @return True if the game contains the location
	 */
	public abstract boolean contains(Location l);

	/**
	 * Checks if a Block is inside the game
	 * 
	 * @param b
	 *            The block to check
	 * @return True if the game contains the block
	 */
	public boolean contains(Block b) {
		return contains(b.getLocation());
	}

	/**
	 * Broadcasts a message to this game
	 * 
	 * @param msg
	 *            The message to send
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
	 * @param m
	 *            The material of the floor
	 * @param data
	 *            The data of the floor
	 * @param type
	 *            The floortype of the floor
	 * @param locations
	 *            The locations that this floor should contain (Center at a
	 *            cylindergame, Two Corners at a cuboidgame)
	 */
	public abstract int addFloor(int blockID, byte data, FloorType type,
			Location... locations);

	/**
	 * Removes a floor from this game
	 */
	public abstract void removeFloor(int id);

	/**
	 * Adds a losezone to this game
	 * 
	 * @param locations
	 *            The two corners of the losezones
	 */
	public abstract int addLoseZone(Location... locations);

	/**
	 * Countdowns the game
	 */
	public void countdown() {
		int countdown = getFlag(COUNTDOWN) == null ? HeavySpleef
				.getSystemConfig().getInt("general.countdownFrom", 15)
				: getFlag(COUNTDOWN);

		prepareGame();// Prepare the game
		Bukkit.getPluginManager().callEvent(
				new SpleefStartEvent(new GameData(this))); // Call our spleef
															// start event
		this.tid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				HeavySpleef.instance, new StartCountdownTask(countdown, this),
				20L, 20L);// Let the countdown begin

		// Scoreboard stuff
		createSidebarScoreboard();
		buildBoxesAndTeleportPlayers();

		setGameState(GameState.COUNTING);// Set our gamestate to counting
		updateScoreBoards();
		updateWalls();
		updateSidebarScoreboard();
	}

	/**
	 * Starts the game
	 */
	public void start() {
		updateScoreBoards();
		setGameState(GameState.INGAME);
		removeBoxes();

		for (Player p : getPlayers()) {
			StatisticManager.getStatistic(p.getName(), true).addGame();
		}

		for (Player p : getPlayers()) {
			if (getFlag(DIFFICULTY) == Difficulty.EASY)
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.FAST_DIGGING, 60 * 60 * 20, getFlag(
								DIFFICULTY).getHasteAmplifier()));
			if (getFlag(DIFFICULTY) == Difficulty.HARD)
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.FAST_DIGGING, 60 * 60 * 20, getFlag(
								DIFFICULTY).getHasteAmplifier()));
		}

		int timeout = getFlag(TIMEOUT) == null ? 0 : getFlag(TIMEOUT);
		int jackpotToPay = getFlag(JACKPOTAMOUNT) == null ? HeavySpleef
				.getSystemConfig().getInt("general.defaultToPay", 5)
				: getFlag(JACKPOTAMOUNT);

		if (timeout > 0)
			this.timeoutTid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
					HeavySpleef.instance, new TimeoutTask(timeout, this), 0L,
					20L);

		// Withdraw jackpot money
		if (HeavySpleef.hooks.getService(VaultHook.class).hasHook()
				&& jackpotToPay > 0) {
			for (Player p : getPlayers()) {
				HeavySpleef.hooks.getService(VaultHook.class).getHook()
						.withdrawPlayer(p.getName(), jackpotToPay);
				p.sendMessage(_("paidIntoJackpot",
						HeavySpleef.hooks.getService(VaultHook.class).getHook()
								.format(jackpotToPay)));
				this.jackpot += jackpotToPay;
			}
		}

		tellAll(_("gameHasStarted"));
		broadcast(_("gameOnArenaHasStarted", getName()));
		broadcast(_("startedGameWith", String.valueOf(players.size())));
		updateWalls();
		cancelSTTask();
	}

	/**
	 * Stops this game
	 */
	public void stop() {
		cancelTasks();

		for (Player p : getPlayers()) {

			if (getFlag(LOSE) == null)
				p.teleport(LocationSaver.load(p));
			else
				p.teleport(getFlag(LOSE));

			PlayerStateManager.restorePlayerState(p);
		}

		Bukkit.getPluginManager()
				.callEvent(
						new SpleefFinishEvent(new GameData(this),
								StopCause.STOP, null));
		broadcast(_("gameStopped"));
		setGameState(GameState.JOINABLE);
		removeAllFromQueue();
		removeBoxes();

		regen();
		clearAll();
		updateScoreBoards();
		updateWalls();
		updateSidebarScoreboard();
	}

	/**
	 * Clears all data from the game
	 */
	protected void clearAll() {
		roundsPlayed = 0;
		voted.clear();
		knockouts.clear();
		wins.clear();
		brokenBlocks.clear();
		players.clear();
		outPlayers.clear();
		chancesUsed.clear();
		multiKnockouts.clear();
	}

	/**
	 * Disables this game
	 * 
	 * @param disabler
	 *            The disabler of this game (can be null)
	 */
	public void disable(String disabler) {
		if (isDisabled())
			return;
		if (hasActivity())
			stop();
		setGameState(GameState.DISABLED);
		updateWalls();
		if (disabler != null)
			broadcast(_("gameDisabled", getName(),
					ViPManager.colorName(disabler)));
	}

	/**
	 * Enables this game
	 */
	public void enable() {
		if (!isDisabled())
			return;
		setGameState(GameState.JOINABLE);
		updateWalls();
	}

	// addPlayer() -> joinGame() -> join()

	/**
	 * Adds a player to this game
	 * 
	 * @param player
	 *            The player to add
	 */
	public void addPlayer(Player player, ChatColor teamColor) {
		int jackpotToPay = getFlag(JACKPOTAMOUNT) == null ? HeavySpleef
				.getSystemConfig().getInt("general.defaultToPay", 0)
				: getFlag(JACKPOTAMOUNT);

		if (isDisabled()) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		if (!isFinal()) {
			player.sendMessage(_("isntReadyToPlay"));
			return;
		}
		if (getType() == Type.CYLINDER
				&& !HeavySpleef.hooks.getService(WorldEditHook.class).hasHook()) {
			player.sendMessage(_("noWorldEdit"));
			return;
		}

		if (HeavySpleef.hooks.getService(VaultHook.class).hasHook()) {
			if (HeavySpleef.hooks.getService(VaultHook.class).getHook()
					.getBalance(player.getName()) < jackpotToPay) {
				player.sendMessage(_("notEnoughMoneyToJoin"));
				return;
			}
		}

		if (GameManager.isInAnyGame(player)) {
			player.sendMessage(_("cantJoinMultipleGames"));
			return;
		}
		if (isIngame()) {
			player.sendMessage(_("gameAlreadyRunning"));
			addToQueue(player, teamColor);
			return;
		}

		Team team = getTeam(teamColor);
		boolean is1vs1 = getFlag(ONEVSONE) == null ? false : getFlag(ONEVSONE);

		int maxplayers = getFlag(MAXPLAYERS) == null ? -1 : getFlag(MAXPLAYERS);
		if (maxplayers > 0 && getPlayers().length >= maxplayers) {
			player.sendMessage(_("maxPlayersReached"));
			addToQueue(player, team != null ? team.getColor() : null);
			return;
		}
		if (isCounting()
				&& HeavySpleef.getSystemConfig().getBoolean(
						"general.joinAtCountdown") && !is1vs1) {
			joinGame(player, team);
			return;
		} else if (isCounting()) {
			player.sendMessage(_("gameAlreadyRunning"));
			addToQueue(player, teamColor);
			return;
		}

		joinGame(player, team);
	}

	private void joinGame(final Player player, final Team team) {
		int pvptimer = HeavySpleef.getSystemConfig().getInt("general.pvptimer");

		if (pvpTimerTasks.containsKey(player.getName()))
			Bukkit.getScheduler().cancelTask(
					pvpTimerTasks.get(player.getName()));

		if (pvptimer > 0) {
			player.sendMessage(_("teleportWillCommence", getName(),
					String.valueOf(pvptimer)));
			player.sendMessage(_("dontMove"));
		}

		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
				HeavySpleef.instance, new Runnable() {

					@Override
					public void run() {
						if (getFlag(TEAM)) {
							if (team != null) {
								team.join(player);
								System.out.println("Joining team "
										+ team.getColor());
							} else {
								player.sendMessage(_("specifieTeam",
										getTeamColors().toString()));
								return;
							}
						}

						join(player);
						player.sendMessage(_("playerJoinedToPlayer", getName()));

						removeFromQueue(player);
						pvpTimerTasks.remove(player.getName());
					}
				}, Math.abs(pvptimer) * 20L);

		pvpTimerTasks.put(player.getName(), taskId);
	}

	@SuppressWarnings("deprecation")
	private void join(Player player) {
		if (isDisabled())
			return;

		boolean is1vs1 = getFlag(ONEVSONE);
		boolean shovels = getFlag(SHOVELS);

		int autostart = getFlag(AUTOSTART);

		if (is1vs1 && players.size() >= 2)
			return;
		players.add(player.getName());
		Location lobby = getFlag(LOBBY) == null ? getRandomLocation()
				: getFlag(LOBBY);
		LocationSaver.save(player);

		if (isCounting() || isIngame())
			player.teleport(getRandomLocation());
		else
			player.teleport(lobby);

		PlayerStateManager.savePlayerState(player);

		if (HeavySpleef.getSystemConfig().getBoolean("sounds.plingSound", true)) {
			for (Player p : getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 4.0F, p
						.getLocation().getPitch());
		}

		if (isWaiting()) // Activate the lobby mode
			setGameState(GameState.LOBBY);

		tellAll(_("playerJoinedGame", ViPManager.colorName(player.getName())));
		Bukkit.getPluginManager().callEvent(
				new SpleefJoinEvent(new GameData(this), player));
		player.setAllowFlight(false);
		player.setFireTicks(0);
		updateWalls();

		if (isCounting() && shovels) {
			player.getInventory().addItem(getSpleefShovel());
			player.updateInventory();
		}

		if ((autostart > 1 && players.size() >= autostart && isPreLobby())
				|| (is1vs1 && players.size() >= 2 && isPreLobby()))
			countdown();
	}

	/**
	 * <b>Trys</b> to remove a player from this game with the given cause
	 * 
	 * @param player
	 *            Player to remove
	 * @param cause
	 *            Cause of lose
	 */
	public void removePlayer(Player player, LoseCause cause) {
		if (player == null)
			return;
		if (cause == null)
			cause = LoseCause.UNKNOWN;

		if (!players.contains(player.getName())) // Player can't be removed if they are not playing
			return;

		boolean is1vs1 = getFlag(ONEVSONE);
		int chances = getFlag(CHANCES);

		if (cause == LoseCause.LOSE) {
			if (is1vs1) {
				nextRound(player);
				return;
			}

			// Chances stuff start
			// And put the chances that the player has left into the Map
			if (!chancesUsed.containsKey(player.getName()))
				chancesUsed.put(player.getName(), 1);
			else
				chancesUsed.put(player.getName(),
						chancesUsed.get(player.getName()) + 1);

			if (chancesUsed.get(player.getName()) < chances) {
				player.teleport(getNewRandomLocation(player));
				player.setFireTicks(0);
				int livesLeft = chances - chancesUsed.get(player.getName());

				player.sendMessage(_("chancesLeft", String.valueOf(livesLeft)));
				broadcast(_("chancesLeftBroadcast",
						ViPManager.colorName(player.getName()),
						String.valueOf(livesLeft)));
				return;
			}
			// Chances stuff end
			player.sendMessage(Game._("outOfGame"));
			StatisticManager.getStatistic(player.getName(), true).addLose();
		}

		players.remove(player.getName());
		removePlayerFromTeam(player);

		if (isIngame()) {
			outPlayers.add(player.getName());
			broadcast(_("remaining", String.valueOf(players.size())));
			Bukkit.getPluginManager().callEvent(
					new SpleefLoseEvent(new GameData(this), player, cause));
		}

		if (!isPreLobby())
			player.sendMessage(_("yourKnockOuts",
					String.valueOf(getKnockouts(player))));
		broadcast(getLoseMessage(cause, player));

		if (getFlag(LOSE) == null)
			player.teleport(LocationSaver.load(player));
		else
			player.teleport(getFlag(LOSE));

		player.setFireTicks(0);
		voted.remove(player.getName());
		updateSidebarScoreboard();

		for (Team team : teams) {
			if (team.hasPlayer(player))
				team.leave(player);
		}

		PlayerStateManager.restorePlayerState(player);
		if (cause == LoseCause.LOSE) {
			for (ItemStack stack : getFlag(LOSEREWARD)) {
				ItemStack newStack = stack.getData().toItemStack(
						stack.getAmount());
				player.getInventory().addItem(newStack);
				player.sendMessage(_("loserewardReceived",
						String.valueOf(newStack.getAmount()),
						Util.getName(newStack.getType().name())));
			}
		}

		if (players.size() <= 0)
			setGameState(GameState.JOINABLE);
		updateWalls();
		if (state == GameState.INGAME || state == GameState.COUNTING) {
			if (getFlag(TEAM)) {
				int teamsLeft = 0;

				for (Team team : teams) {
					if (team.hasPlayersLeft())
						teamsLeft++;
				}

				if (teamsLeft <= 1) {
					teamWin(getWinnerTeam());
				}
			}

			if (players.size() != 1)
				return;

			cancelTasks();
			win(Bukkit.getPlayer(players.get(0)));

		}
	}

	private Team getWinnerTeam() {
		Team winnerTeam = null;

		for (Team team : teams) {
			if (team.hasPlayersLeft())
				winnerTeam = team;
		}

		return winnerTeam;
	}

	/**
	 * Internal method. Only use this if you know what you are doing</br> Next
	 * round for the 1vs1 game with the given loser
	 * 
	 * @param loserr
	 *            The loser of the round
	 */
	public void nextRound(Player loserr) {
		if (!isIngame())
			return;

		boolean is1vs1 = getFlag(ONEVSONE);
		int rounds = getFlag(ROUNDS);
		if (!is1vs1)
			return;

		roundsPlayed++;// One round higher
		for (Player p : getPlayers()) {
			if (p.getName().equalsIgnoreCase(loserr.getName()))
				continue;

			add1vs1Win(p);// Add the win
			addKnockout(p.getName());
			broadcast(_("wonRound", ViPManager.colorName(p.getName()),
					String.valueOf(roundsPlayed), String.valueOf(rounds)));
		}
		if (roundsPlayed < rounds) {// Next round?
			regen();// Regenerate floors
			boolean countdown = HeavySpleef.getSystemConfig().getBoolean(
					"general.countdownBetweenRound", true);

			buildBoxesAndTeleportPlayers();
			broadcast(_("roundsRemaining", String.valueOf(rounds - roundsPlayed)));
			updateWalls();
			if (countdown) {// Do a countdown if it was so defined
				int start = HeavySpleef.getSystemConfig().getInt(
						"general.countdownBetweenRoundLength", 5);
				this.roundTid = Bukkit.getScheduler()
						.scheduleSyncRepeatingTask(HeavySpleef.instance,
								new RoundsCountdownTask(start, this), 0L, 20L);
			}

			return;
		} else if (roundsPlayed >= rounds) {// If not then end the game
			Win win = getHighestWin(); // Get the highest win
			removeBoxes();

			if (win == null) { // If it is null there is a draw
				endInDraw();
				return;
			}

			String loser = "null";
			for (Player p : getPlayers()) {
				if (p.getName().equalsIgnoreCase(win.getOwner()))
					//Compare player with the owner of the win
					continue;

				loser = p.getName();
			}

			win(Bukkit.getPlayer(win.getOwner()));// Win, broadcast and end the
													// game!
			broadcast(_("wonThe1vs1", ViPManager.colorName(win.getOwner()),
					ViPManager.colorName(loser)));
			return;
		}

	}

	// Gets the highest win of the game
	// 1vs1 method
	private Win getHighestWin() {
		// Returns null if there is a draw
		for (int i = 0; i < wins.size(); i++) {// First check if there is a same
												// value in the wins list
			for (int a = 0; a < wins.size(); a++) {
				if (wins.get(i).equals(wins.get(a)))
					continue;
				if (wins.get(i).getWins() == wins.get(a).getWins()) {
					return null;// And return null if there is...
				}
			}
		}

		List<Win> wins = new ArrayList<Game.Win>(this.wins);
		Collections.sort(wins);

		return wins.get(wins.size() - 1);
	}

	// Adds a 1vs1 win to the given player
	// 1vs1 method
	private void add1vs1Win(Player p) {
		Win win = null;

		for (Win w : wins) {// Indicate which win is owned by the player
			if (w.getOwner().equalsIgnoreCase(p.getName())) {
				win = w;
				break;
			}
		}

		if (win == null) {// Add a new win if there is no win...
			wins.add(new Win(p.getName()));
			return;
		}

		win.addWin();// Finally add the winpoint
	}

	// Returns a random location in the game where the floor under the player is
	// not air
	// Chances method
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

	// Just a method for a specified spleef shovel with a name
	// Shovels method
	private static ItemStack getSpleefShovel() {
		ItemStack shovel = new ItemStack(Material.DIAMOND_SPADE, 1);
		ItemMeta meta = shovel.getItemMeta();

		meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
				+ "Spleef Shovel");

		shovel.setItemMeta(meta);

		return shovel;
	}

	/**
	 * Ends the game with the given player as the winner
	 */
	public void win(Player p) {
		if (p == null) // No NPEs
			return;

		if (getFlag(WIN) == null)
			p.teleport(LocationSaver.load(p));
		else
			p.teleport(getFlag(WIN));

		regen();
		players.remove(p.getName());
		removePlayerFromTeam(p);

		for (Player player : getPlayers()) {
			if (getFlag(LOSE) == null)
				player.teleport(LocationSaver.load(player));
			else
				player.teleport(getFlag(LOSE));

			player.setFireTicks(0);
			player.setFallDistance(0);
			StatisticManager.getStatistic(player.getName(), true).addLose();
			players.remove(player.getName());
			removePlayerFromTeam(player);

			PlayerStateManager.restorePlayerState(player);
		}

		StatisticManager.getStatistic(p.getName(), true).addWin();

		PlayerStateManager.restorePlayerState(p);

		broadcast(_("hasWon", ViPManager.colorName(p.getName()), this.getName()));
		p.sendMessage(_("win"));
		p.sendMessage(_("yourKnockOuts", String.valueOf(getKnockouts(p))));
		clearAll();
		removeBoxes();

		Bukkit.getPluginManager().callEvent(
				new SpleefFinishEvent(new GameData(this), StopCause.WIN, p));

		if (HeavySpleef.getSystemConfig().getBoolean("sounds.levelUp", true))
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);

		giveRewards(p, true);
		setGameState(GameState.JOINABLE);
		updateScoreBoards();
		updateSidebarScoreboard();
		updateWalls();
		addPlayersFromQueue();
	}

	/**
	 * Ends the game with the given team as the winner
	 * 
	 * @param team
	 *            The team to win
	 */
	public void teamWin(Team team) {
		if (team == null) {
			stop();
			return;
		}
		if (!getFlag(TEAM))
			return;
		if (!hasActivity())
			return;

		regen();

		for (Player winner : team.getPlayers()) {
			if (getFlag(WIN) == null)
				winner.teleport(LocationSaver.load(winner));
			else
				winner.teleport(getFlag(WIN));

			players.remove(winner.getName());
			winner.sendMessage(_("yourKnockOuts",
					String.valueOf(getKnockouts(winner))));

			PlayerStateManager.restorePlayerState(winner);

			StatisticManager.getStatistic(winner.getName(), true).addWin();
			giveRewards(winner, false);

			removePlayerFromTeam(winner);
		}

		for (Player leftPlayer : getPlayers()) {
			if (getFlag(LOSE) == null)
				leftPlayer.teleport(LocationSaver.load(leftPlayer));
			else
				leftPlayer.teleport(getFlag(WIN));

			players.remove(leftPlayer.getName());
			leftPlayer.sendMessage(_("yourKnockOuts",
					String.valueOf(getKnockouts(leftPlayer))));

			PlayerStateManager.restorePlayerState(leftPlayer);

			StatisticManager.getStatistic(leftPlayer.getName(), true).addLose();
			removePlayerFromTeam(leftPlayer);
		}

		Bukkit.getPluginManager().callEvent(
				new SpleefFinishEvent(new GameData(this), StopCause.WIN, null));
		clearJackpot();
		clearAll();
		removeBoxes();
		broadcast(_("teamWin", Util.getName(team.getColor().name()), getName()));
		cancelTasks();
		setGameState(GameState.JOINABLE);
		updateScoreBoards();
		updateWalls();
		updateSidebarScoreboard();
		addPlayersFromQueue();
	}

	/**
	 * Internal method. Don't call this
	 */
	public void endInDraw() {
		regen();

		Bukkit.getPluginManager()
				.callEvent(
						new SpleefFinishEvent(new GameData(this),
								StopCause.DRAW, null));

		for (Player p : getPlayers()) {
			if (getFlag(WIN) == null)
				p.teleport(LocationSaver.load(p));
			else
				p.teleport(getFlag(WIN));

			players.remove(p.getName());
			removePlayerFromTeam(p);
			PlayerStateManager.restorePlayerState(p);
		}

		clearAll();
		removeBoxes();
		broadcast(_("endedDraw", getName()));
		cancelTasks();
		setGameState(GameState.JOINABLE);
		updateScoreBoards();
		updateWalls();
		updateSidebarScoreboard();
		addPlayersFromQueue();
	}

	private String getLoseMessage(LoseCause cause, Player player) {
		switch (cause) {
		case QUIT:
			return _("loseCause_" + cause.name().toLowerCase(),
					ViPManager.colorName(player.getName()));
		case KICK:
			return _("loseCause_" + cause.name().toLowerCase(),
					ViPManager.colorName(player.getName()));
		case LEAVE:
			return _("loseCause_" + cause.name().toLowerCase(),
					ViPManager.colorName(player.getName()), getName());
		case LOSE:
			Team team = getTeam(player);
			if (team == null)
				return _("loseCause_" + cause.name().toLowerCase(),
						ViPManager.colorName(player.getName()),
						getKiller(player, true));

			String killerName = "";

			Player killer = Bukkit.getPlayer(getKiller(player, true));
			if (killer == null)
				return _("loseCause_" + cause.name().toLowerCase(),
						team.getColor() + player.getName(), "");

			killerName = killer.getName();
			Team killerTeam = getTeam(killer);
			if (killerTeam == null)
				return _("loseCause_" + cause.name().toLowerCase(),
						team.getColor() + player.getName(),
						ViPManager.colorName(killerName));

			return _("loseCause_" + cause.name().toLowerCase(), team.getColor()
					+ player.getName(), killerTeam.getColor() + killerName);
		case UNKNOWN:
			return _("loseCause_" + cause.name().toLowerCase(),
					ViPManager.colorName(player.getName()));
		default:
			return "null...";
		}
	}

	/**
	 * Gets the killer of a player
	 * 
	 * @param player
	 *            The player that was knocked out
	 * @param addKnockout
	 *            Wether a knockout should be added to the killer
	 * 
	 * @return The name of the killer
	 */
	public String getKiller(Player player, boolean addKnockout) {
		Floor lowerMost = getLowestFloor();

		for (String name : brokenBlocks.keySet()) {
			List<Block> blocks = brokenBlocks.get(name);
			for (Block block : blocks) {
				if (block.getY() != lowerMost.getY())
					continue;

				int differenceX = block.getX() < player.getLocation()
						.getBlockX() ? player.getLocation().getBlockX()
						- block.getX() : block.getX()
						- player.getLocation().getBlockX();
				int differenceZ = block.getZ() < player.getLocation()
						.getBlockZ() ? player.getLocation().getBlockZ()
						- block.getZ() : block.getZ()
						- player.getLocation().getBlockZ();

				if (differenceX == 0 && differenceZ == 0) {
					if (addKnockout)
						addKnockout(name);
					return name;
				}

			}
		}

		return new String();
	}

	/**
	 * Adds a knockout to a player
	 * 
	 * @param player
	 *            The playername to add
	 */
	public void addKnockout(final String player) {
		if (knockouts.containsKey(player))
			knockouts.put(player, knockouts.get(player) + 1);
		else
			knockouts.put(player, 1);
		StatisticManager.getStatistic(player, true).addKnockout();

		boolean previousTask = multiKnockoutTaskIds.containsKey(player);
		int previousTaskId = previousTask ? multiKnockoutTaskIds.get(player)
				: -1;

		int task = Bukkit.getScheduler().scheduleSyncDelayedTask(
				HeavySpleef.instance, new MultiKnockoutTask(player), 100L);// Run a new task
		if (previousTask)// Cancel our previous task
			Bukkit.getScheduler().cancelTask(previousTaskId);
		multiKnockoutTaskIds.put(player, task);// Add the task id to our map

		if (!multiKnockouts.containsKey(player))
			multiKnockouts.put(player, 1);
		else {
			int newKnockouts = multiKnockouts.get(player) + 1;
			multiKnockouts.put(player, newKnockouts);
			switch (newKnockouts) {
			case 2:
				broadcast(_("doubleKnockout", ViPManager.colorName(player)));
				break;
			case 3:
				broadcast(_("tripleKnockout", ViPManager.colorName(player)));
				break;
			case 4:
				broadcast(_("superKnockout", ViPManager.colorName(player)));
				break;
			case 5:
				broadcast(_("unbelievableKnockout",
						ViPManager.colorName(player)));
				break;
			}
		}
	}

	/**
	 * Gets the knockouts of a player
	 * 
	 * @param player
	 *            The player to get
	 * @return The knockouts
	 */
	public int getKnockouts(Player player) {
		if (knockouts.containsKey(player.getName()))
			return knockouts.get(player.getName());
		return 0;
	}

	/**
	 * Sets the game to the given state
	 * 
	 * @param state
	 *            The state to set
	 */
	public void setGameState(GameState state) {
		this.state = state;
	}

	/**
	 * Gets the gamestate of this game
	 * 
	 * @return The gamestate of the game
	 */
	public GameState getGameState() {
		return state;
	}

	/**
	 * Checks wether this game is ingame
	 * 
	 * @return True if the game is ingame
	 */
	public boolean isIngame() {
		return state == GameState.INGAME;
	}

	/**
	 * Checks wether this game is counting
	 * 
	 * @return True if the game is counting
	 */
	public boolean isCounting() {
		return state == GameState.COUNTING;
	}

	/**
	 * Checks wether this game is waiting
	 * 
	 * @return True if the game is waiting
	 */
	public boolean isWaiting() {
		return state == GameState.JOINABLE;
	}

	/**
	 * Checks wether this game is in the lobby state
	 * 
	 * @return True if the game is in the lobby state
	 */
	public boolean isPreLobby() {
		return state == GameState.LOBBY;
	}

	/**
	 * Checks wether this game is disabled
	 * 
	 * @return True if the game is disabled
	 */
	public boolean isDisabled() {
		return state == GameState.DISABLED;
	}

	/**
	 * Checks if this game has activity (is lobby, counting or ingame)
	 * 
	 * @return True if the game has activity
	 */
	public boolean hasActivity() {
		return isCounting() || isIngame() || isPreLobby();
	}

	/**
	 * Adds a existing floor to this game
	 * 
	 * @param floor
	 *            The floor to add
	 * @param create
	 *            Wether this floor should be created
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
	 * @param id
	 *            The id to check
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
	 * Gets the floor with the given id
	 * 
	 * @param id
	 *            The id of the floor to get
	 * @return The floor or null
	 */
	public Floor getFloor(int id) {
		return floors.get(id);
	}

	/**
	 * Adds a existing losezone to this game
	 * 
	 * @param loseZone
	 *            The losezone to add
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
	 * @param id
	 *            The id of the losezone
	 */
	public void removeLoseZone(int id) {
		loseZones.remove(id);
	}

	/**
	 * Checks wether this game has a losezone with the given id
	 * 
	 * @param id
	 *            The id to check
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
	 * 
	 * @return A collection containing all floors
	 */
	public Collection<Floor> getFloors() {
		return floors.values();
	}

	/**
	 * Gets all losezones of this game
	 * 
	 * @return A collection containing all losezones
	 */
	public Collection<LoseZone> getLoseZones() {
		return loseZones.values();
	}

	/**
	 * Tells a message to all ingame players
	 * 
	 * @param msg
	 *            The message to send
	 */
	public void tellAll(String msg) {
		for (Player p : getPlayers()) {
			p.sendMessage(msg);
		}
	}

	/**
	 * Gets the lowermost floor of this game
	 * 
	 * @return The lowermost floor
	 */
	public Floor getLowestFloor() {
		Map<Integer, Floor> floorsWithY = new HashMap<Integer, Floor>();

		for (Floor f : getFloors()) {
			int minY = f.getY();
			floorsWithY.put(minY, f);
		}

		Integer[] keySet = floorsWithY.keySet().toArray(
				new Integer[floorsWithY.size()]);
		Arrays.sort(keySet);
		return floorsWithY.get(keySet[0]);
	}

	/**
	 * Gets the highest floor of this game
	 * 
	 * @return The highest game
	 */
	public Floor getHighestFloor() {
		Map<Integer, Floor> floorsWithY = new HashMap<Integer, Floor>();

		for (Floor f : getFloors()) {
			int minY = f.getY();
			floorsWithY.put(minY, f);
		}

		Integer[] keySet = floorsWithY.keySet().toArray(
				new Integer[floorsWithY.size()]);
		Arrays.sort(keySet);
		return floorsWithY.get(keySet[keySet.length - 1]);
	}

	/**
	 * Stylizes the value of this keystring
	 * 
	 * @param key
	 *            The key of the message (language)
	 * @return The messagevalue with a HeavySpleef prefix
	 */
	public static String _(String... key) {
		return HeavySpleef.PREFIX + ChatColor.RESET + " "
				+ LanguageHandler._(key);
	}

	/**
	 * Gets the message of the given key
	 * 
	 * @param key
	 *            The key of this message
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
	 * Gets an array of all players out of the game
	 */
	public Player[] getOutPlayers() {
		String[] playersAsString = outPlayers.toArray(new String[outPlayers
				.size()]);
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
	 * Setup the floors of this game and creates all of this floors
	 */
	private void prepareGame() {
		regen();
		this.jackpot = 0;
		this.roundsPlayed = 0;

		if (getFlag(SHOVELS)) {
			for (Player p : getPlayers()) {
				// Give players the shovels...
				p.getInventory().addItem(getSpleefShovel());
			}
		}
	}

	/**
	 * Regenerates all floors of this game
	 */
	public void regen() {
		for (Floor floor : getFloors()) {
			if (!regen(floor.getId()))
				HeavySpleef.instance.getLogger().warning(
						getName() + ": Could not regenerate floor "
								+ floor.getId() + "!");
		}
	}

	/**
	 * Regenerates the floor with the given id
	 * 
	 * @param id
	 *            The id of the floor which should be regenerated
	 * @return True if the floor was regenerated
	 */
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
		return getFlag(LOBBY) != null && floors.size() > 0;
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
	 * Sets the name of the game
	 */
	public boolean rename(String newName) {
		if (GameManager.hasGame(newName))
			return false;

		HeavySpleef.instance.database.db.set(getName(), null);
		HeavySpleef.instance.database.save();
		this.name = newName;
		return true;
	}

	/**
	 * Gets the configurationsection of this game on the disk
	 * 
	 * @return The configurationsection of this game
	 */
	public ConfigurationSection getGameSection() {
		return gameSection;
	}

	/**
	 * Adds a broken block to the game
	 * 
	 * @param p
	 *            The player that broke this block
	 * @param b
	 *            The block that was broken
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

	public int getCurrentRound() {
		return this.roundsPlayed + 1;// Add one
	}

	/**
	 * Internal method.</br></br>
	 * 
	 * Gets an array containing the 1vs1 wins of the current game
	 */
	protected int[] getWins() {
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

	/**
	 * Adds a realtime scoreboard to the game A realtime scoreboard has a size
	 * of 19x7 blocks and only works on 1vs1 games
	 * 
	 * @param loc
	 *            The location of the right upper corner
	 * @param face
	 *            The direction of this scoreboard
	 */
	public void addScoreBoard(Location loc, BlockFace face) {
		int id = 0;
		while (scoreboards.containsKey(id))
			id++;

		ScoreBoard board = new ScoreBoard(loc, id, this, face);
		scoreboards.put(id, board);

		board.draw();
	}

	/**
	 * Adds a realtime scoreboard to this game from the given ScoreBoard Object
	 * 
	 * @param board
	 *            The ScoreBoard object to add
	 */
	public void addScoreBoard(ScoreBoard board) {
		scoreboards.put(board.getId(), board);
		board.draw();
	}

	/**
	 * Removes a realtime scoreboard with the given id
	 * 
	 * @param id
	 *            The id of the scoreboard to remove
	 * @return True if the scoreboard was removed
	 */
	public boolean removeScoreBoard(int id) {
		if (!scoreboards.containsKey(id))
			return false;

		scoreboards.remove(id);
		return true;
	}

	/**
	 * Checks if the player can spleef at the given location
	 * 
	 * @param location
	 *            The location to check
	 * @param p
	 *            The player to check
	 */
	public boolean canSpleef(Location location, Player p) {
		if (!players.contains(p.getName()))
			return false;
		if (!isIngame())
			return false;

		for (Floor floor : getFloors()) {
			if (!floor.contains(location))
				continue;
			return true;
		}

		return false;
	}

	/**
	 * Checks if the player can spleef the given block
	 * 
	 * @see #canSpleef(Location, Player)
	 * 
	 * @param block
	 *            The block to check
	 * @param p
	 *            The player to check
	 */
	public boolean canSpleef(Block block, Player p) {
		return canSpleef(block.getLocation(), p);
	}

	/**
	 * Gets the scoreboard with the given id
	 * 
	 * @param id
	 *            The id of the scoreboard to get
	 */
	public ScoreBoard getScoreBoard(int id) {
		return scoreboards.get(id);
	}

	/**
	 * Gets all realtime scoreboards of this game
	 */
	public ScoreBoard[] getScoreBoards() {
		return scoreboards.values().toArray(new ScoreBoard[scoreboards.size()]);
	}

	private void updateScoreBoards() {
		for (ScoreBoard board : getScoreBoards()) {
			board.draw();
		}
	}

	protected void cancelTasks() {
		cancelSTTask();
		cancelRCTask();
		cancelTOTask();
	}

	protected void cancelRCTask() {
		if ((Bukkit.getScheduler().isQueued(this.roundTid) || Bukkit
				.getScheduler().isCurrentlyRunning(this.roundTid))
				&& roundTid != -1) {
			Bukkit.getScheduler().cancelTask(this.roundTid);
			this.roundTid = -1;
		}
	}

	/**
	 * Internal method. Don't call this
	 */
	public void cancelSTTask() {
		if ((Bukkit.getScheduler().isQueued(this.tid) || Bukkit.getScheduler()
				.isCurrentlyRunning(this.tid)) && tid != -1) {
			Bukkit.getScheduler().cancelTask(this.tid);
			this.tid = -1;
		}
	}

	protected void cancelTOTask() {
		if ((Bukkit.getScheduler().isQueued(this.timeoutTid) || Bukkit
				.getScheduler().isCurrentlyRunning(this.timeoutTid))
				&& timeoutTid != -1) {
			Bukkit.getScheduler().cancelTask(this.timeoutTid);
			this.timeoutTid = -1;
		}
	}

	private void buildBoxesAndTeleportPlayers() {
		int count = 1;
		for (Player p : getPlayers()) {
			if (getFlag(ONEVSONE)) {
				if (count == 1 && getFlag(SPAWNPOINT1) != null) {
					p.teleport(getFlag(SPAWNPOINT1));
					buildBox(Material.GLASS, getFlag(SPAWNPOINT1));
					return;
				} else if (count == 2 && getFlag(SPAWNPOINT2) != null) {
					p.teleport(getFlag(SPAWNPOINT2));
					buildBox(Material.GLASS, getFlag(SPAWNPOINT2));
					return;
				}
			}

			p.teleport(getRandomLocation()); // Should not be fired
			count++;
		}
	}

	private void buildBox(Material mat, Location location) {
		BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH_EAST,
				BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST,
				BlockFace.SOUTH_WEST, BlockFace.SELF };
		Location loc = location.clone();

		for (int i = 0; i < 3; i++) {
			if (i < 2) {
				for (BlockFace face : faces) {
					if (face == BlockFace.SELF)
						continue;
					Block block = loc
							.getWorld()
							.getBlockAt(loc.getBlockX(), loc.getBlockY() + i,
									loc.getBlockZ()).getRelative(face);
					if (block.getType() != Material.AIR)
						toUndo.add(new SimpleBlockData(block));

					block.setType(mat);
					modfiedBlocks.add(block);
				}
			} else {
				for (BlockFace face : faces) {
					Block block = loc
							.getWorld()
							.getBlockAt(loc.getBlockX(), loc.getBlockY() + i,
									loc.getBlockZ()).getRelative(face);
					if (block.getType() != Material.AIR)
						toUndo.add(new SimpleBlockData(block));

					block.setType(mat);
					modfiedBlocks.add(block);
				}
			}
		}
	}

	/**
	 * Internal method. Don't call this
	 */
	public void removeBoxes() {
		for (Block block : modfiedBlocks) {
			block.setType(Material.AIR);
		}

		for (SimpleBlockData data : this.toUndo) {
			Block block = data.getWorld().getBlockAt(data.getLocation());
			block.setTypeIdAndData(data.getMaterial().getId(), data.getData(),
					false);
		}

		toUndo.clear();
		modfiedBlocks.clear();
	}

	/**
	 * Adds a signwall with the given SignWall Object to this game</br> Note
	 * that it has to be a wall, with signs on it!</br> If you don't observe
	 * this exceptions will be throwed
	 * 
	 * @param loc1
	 *            The first location of this wall
	 * @param loc2
	 *            The second location of this wall
	 * 
	 * @return The signwall
	 */
	public SignWall addWall(Location loc1, Location loc2) {
		int id = 0;
		while (walls.containsKey(id))
			id++;

		SignWall wall = new SignWall(loc1, loc2, this, id);
		walls.put(id, wall);
		updateWalls();
		return wall;
	}

	/**
	 * Adds a signwall with the given SignWall Object to this game</br> Note
	 * that it has to be a wall, with signs on it! (only one row)</br> If you
	 * don't observe this exceptions will be throwed
	 * 
	 * @param wall
	 *            The wall to add
	 * @return The signwall itself
	 */
	public SignWall addWall(SignWall wall) {
		walls.put(wall.getId(), wall);
		wall.update();
		return wall;
	}

	/**
	 * Removes a signwall of this game
	 * 
	 * @param id
	 *            The id of the wall
	 */
	public void removeWall(int id) {
		if (!walls.containsKey(id))
			return;

		walls.remove(id);
	}

	/**
	 * Gets all SignWalls of this game
	 * 
	 * @see SignWall
	 * 
	 * @return A Collection containing all SignWalls
	 */
	public Collection<SignWall> getWalls() {
		return walls.values();
	}

	private void updateWalls() {
		for (SignWall wall : walls.values()) {
			wall.update();
		}
	}

	/**
	 * Gets the current count of this game Only works if the game is counting
	 */
	public int getCurrentCount() {
		return this.currentCountdown;
	}

	/**
	 * This is an internal method, it is NOT recommended that you use this
	 * method</br> Sets the current count for this game
	 * 
	 * @param i
	 *            The count in seconds
	 */
	public void setCurrentCount(int i) {
		this.currentCountdown = i;
		updateWalls();
	}

	private void createSidebarScoreboard() {
		if (!HeavySpleef.getSystemConfig().getBoolean(
				"general.showSidebarScoreboard"))
			return;
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();

		if (board == null)
			return;

		board.registerNewObjective("showKnockouts", "Knockouts");
		Objective objective = board.getObjective("showKnockouts");

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		if (getFlag(ONEVSONE))
			objective.setDisplayName(ChatColor.GREEN + "Status 0:0");
		else
			objective.setDisplayName(ChatColor.GREEN + "Knockouts");

		if (!getFlag(TEAM)) {
			for (Player player : getPlayers()) {
				Score score = objective.getScore(player);
				score.setScore(0);
			}
		} else {
			org.bukkit.scoreboard.Team teams[] = new org.bukkit.scoreboard.Team[this.teams
					.size()];
			for (int i = 0; i < this.teams.size(); i++) {
				teams[i] = board.registerNewTeam(this.teams.get(i).getColor()
						.name());
			}

		}

		for (Player player : getPlayers()) {
			previousScoreboards.put(player.getName(), player.getScoreboard());

			player.setScoreboard(board);
		}
	}

	private void updateSidebarScoreboard() {
		if (!HeavySpleef.getSystemConfig().getBoolean(
				"general.showSidebarScoreboard"))
			return;
		if (board == null)
			return;

		Player[] out = getOutPlayers();
		Player[] in = getPlayers();

		List<Player> allPlayers = ArrayHelper.mergeArrays(in, out);

		if (isWaiting()) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				Scoreboard playerBoard = player.getScoreboard();

				if (playerBoard.getPlayers().contains(player))
					player.setScoreboard(previousScoreboards.get(player
							.getName()));
			}

			board.getObjective("showKnockouts").unregister();
			board = null;
			return;
		}
		for (Player player : allPlayers) {// Loop around all players
			if (!players.contains(player.getName()))// If the player is out we remove the ingame item
				board.resetScores(player);
			Objective objective = board.getObjective("showKnockouts");

			objective.getScore(player).setScore(getKnockouts(player)); // Set the score

			if (!players.contains(player.getName()))// Only show scoreboard if they are not out
				player.setScoreboard(previousScoreboards.get(player.getName()));
		}
	}

	/**
	 * Adds a vote to the game
	 * 
	 * @param player
	 *            The voting player
	 * @return True if the vote was added, otherwise false
	 */
	public boolean addVote(Player player) {
		if (hasVote(player))
			return false;
		voted.add(player.getName());
		if (canBegin())
			countdown();
		return true;
	}

	/**
	 * Checks if a player has voted to start this game
	 * 
	 * @param player
	 *            The player to check
	 */
	public boolean hasVote(Player player) {
		return voted.contains(player.getName());
	}

	private boolean canBegin() {
		int procent = HeavySpleef.getSystemConfig().getInt(
				"general.autostart-vote");

		double oneProcent = (double) players.size() / 100.0;
		double neededProcent = oneProcent * procent;

		int minplayers = getFlag(MINPLAYERS);

		if (voted.size() < minplayers)
			return false;
		if (voted.size() >= neededProcent && players.size() > 1)
			return true;

		return false;
	}

	/* Team stuff */
	public void addTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color) {
				teams.remove(team);
				break;
			}
		}

		Team team = new Team(color, this);
		teams.add(team);
	}

	public void addTeam(Team team) {
		addTeam(team.getColor());
	}

	public Team getTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color)
				return team;
		}

		return null;
	}

	public Team getTeam(Player player) {
		for (Team team : teams) {
			for (Player p : team.getPlayers()) {
				if (player.getName().equalsIgnoreCase(p.getName())) {
					return team;
				}
			}
		}

		return null;
	}

	public boolean removeTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color) {
				teams.remove(team);
				return true;
			}
		}

		return false;
	}

	public boolean removePlayerFromTeam(Player player) {
		boolean removed = false;

		for (Team team : teams) {
			if (team.hasPlayer(player)) {
				team.leave(player);
				removed = true;
			}
		}

		return removed;
	}

	public boolean hasTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color)
				return true;
		}

		return false;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public Set<String> getTeamColors() {
		Set<String> set = new HashSet<String>();
		for (Team team : teams)
			set.add(team.getColor() + Util.getName(team.getColor().name()));

		return set;
	}

	/* Team stuff end */

	/**
	 * Gives spleef rewards to the given player Possible rewards: Money reward,
	 * Jackpot reward, Item reward
	 * 
	 * @param player
	 *            Player who should receive rewards
	 * @param clearJackpot
	 *            If the jackpot should be cleared (resets the jackpot)
	 */
	public void giveRewards(Player player, boolean clearJackpot) {
		for (ItemStack stack : getFlag(ITEMREWARD)) {
			//We need to convert the data to a new stack (Bukkit ItemData bug?)
			ItemStack newStack = stack.getData().toItemStack(stack.getAmount());
			player.getInventory().addItem(newStack);
			player.sendMessage(_("itemRewardReceived",
					String.valueOf(stack.getAmount()),
					Util.getName(stack.getType().name())));
		}
		if (HeavySpleef.hooks.getService(VaultHook.class).hasHook()) {
			Economy econ = HeavySpleef.hooks.getService(VaultHook.class)
					.getHook();
			if (this.jackpot > 0) {
				EconomyResponse r = econ.depositPlayer(player.getName(),
						this.jackpot);
				player.sendMessage(_("jackpotReceived", econ.format(r.amount)));
				if (clearJackpot)
					clearJackpot();
			}
			int reward = getFlag(REWARD);

			if (reward > 0) {
				EconomyResponse r = econ
						.depositPlayer(player.getName(), reward);
				player.sendMessage(_("rewardReceived", econ.format(r.amount)));
			}
		}
	}

	private void clearJackpot() {
		this.jackpot = 0;
	}

	/**
	 * Gets a flag of this game</br> If there is no flag with the given
	 * parameter,</br> the default value in the config will be returned</br>
	 * This can never return null </br></br>
	 * 
	 * @see me.matzefratze123.heavyspleef.core.flag.FlagType
	 * 
	 * @param flag
	 *            The flagtype to get
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V getFlag(T flag) {
		Object o = flags.get(flag);
		V value = null;

		if (o == null) {
			String str = HeavySpleef.getSystemConfig().getString(
					"flag-defaults." + flag.getName());
			if (str == null)
				return flag.getAbsolutDefault();
			return flag.parse(null, str);
		} else
			value = (V) o;

		return value;
	}

	/**
	 * Sets a flag for this game
	 * 
	 * @see me.matzefratze123.heavyspleef.core.flag.FlagType
	 * 
	 * @param flag
	 *            The flag to set
	 * @param value
	 *            The value for this flag
	 */
	public <T extends Flag<V>, V> void setFlag(T flag, V value) {
		if (value == null) {
			flags.remove(flag);
		} else {
			flags.put(flag, value);
		}
	}

	/**
	 * Sets all flags for this game
	 * 
	 * @param flags
	 *            A map containing all flags for this game
	 */
	public void setFlags(Map<Flag<?>, Object> flags) {
		this.flags = flags;
	}

	/**
	 * Gets all flags of this game
	 * 
	 * @return A map with all flags
	 */
	public Map<Flag<?>, Object> getFlags() {
		return this.flags;
	}

	/* Queues system start */
	public void addToQueue(Player player, ChatColor color) {
		if (!HeavySpleef.getSystemConfig().getBoolean("queues.useQueues", true))
			return;
		for (Game game : GameManager.getGames()) {
			game.removeFromQueue(player);
		}

		Team team = getTeam(color);
		queues.add(new Queue(player, this, team));
		player.sendMessage(_("addedToQueue", getName()));
	}

	public void removeFromQueue(Player player) {
		Queue toRemove = null;

		for (Queue queue : new ArrayList<Queue>(queues)) {
			if (queue.getOwner().getName().equalsIgnoreCase(player.getName())) {
				toRemove = queue;
				break;
			}
		}

		if (toRemove == null)
			return;

		queues.remove(toRemove);
		player.sendMessage(_("noLongerInQueue", getName()));
	}

	public void removeAllFromQueue() {
		for (Queue queue : new ArrayList<Queue>(queues)) {
			removeFromQueue(queue.getOwner());
		}
	}

	public Queue getQueue(Player player) {
		for (Queue queue : new ArrayList<Queue>(queues)) {
			if (queue.getOwner().getName().equalsIgnoreCase(player.getName()))
				return queue;
		}

		return null;
	}

	public boolean hasQueue(Player player) {
		for (Queue queue : queues) {
			Player owner = queue.getOwner();
			if (owner == null)
				continue;
			if (player == null)
				continue;

			if (owner.getName().equalsIgnoreCase(player.getName()))
				return true;
		}

		return false;
	}

	/**
	 * Gets all queues of this game
	 */
	public List<Queue> getQueues() {
		return queues;
	}

	/**
	 * Adds all players from the queue to this game
	 */
	public void addPlayersFromQueue() {
		List<Queue> copyOfQueues = new ArrayList<Queue>(queues); 
		// We need a copy of the queues to prevent a ConcurrentModificationException...

		int maxToAdd = getFlag(ONEVSONE) ? 2 : getFlag(MAXPLAYERS);
		int added = 0;

		for (Queue queue : copyOfQueues) {
			Player player = queue.getOwner();
			if (player == null)
				continue;
			if (maxToAdd > 0 && added >= maxToAdd)
				continue;

			removeFromQueue(player);
			ChatColor color = queue.getTeam() == null ? null : queue.getTeam()
					.getColor();

			addPlayer(player, color);
			added++;
		}
	}

	/* Queues system end */

	// Just a simple class to save a 1vs1 win
	public class Win implements Cloneable, Comparable<Win> {

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
			Win win = (Win) o;

			if (!win.getOwner().equalsIgnoreCase(getOwner()))
				return false;
			if (win.getWins() != getWins())
				return false;

			return true;
		}

		@Override
		public int compareTo(Win o) {
			return ((Integer) getWins()).compareTo(o.getWins());
		}

	}

	private class MultiKnockoutTask implements Runnable {

		private String name;

		public MultiKnockoutTask(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			multiKnockoutTaskIds.remove(name);
			multiKnockouts.remove(name);
		}

	}

}
