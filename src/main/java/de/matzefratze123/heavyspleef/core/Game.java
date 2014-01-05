/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.core;

import static de.matzefratze123.heavyspleef.core.flag.FlagType.ITEMREWARD;
import static de.matzefratze123.heavyspleef.core.flag.FlagType.REWARD;
import static de.matzefratze123.heavyspleef.core.flag.FlagType.SPECTATE;
import static de.matzefratze123.heavyspleef.core.flag.FlagType.TEAM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.api.IGame;
import de.matzefratze123.heavyspleef.api.IGameComponents;
import de.matzefratze123.heavyspleef.api.event.SpleefFinishEvent;
import de.matzefratze123.heavyspleef.api.event.SpleefJoinEvent;
import de.matzefratze123.heavyspleef.api.event.SpleefLoseEvent;
import de.matzefratze123.heavyspleef.api.event.SpleefStartEvent;
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.core.flag.ListFlagItemstack.SerializeableItemStack;
import de.matzefratze123.heavyspleef.core.queue.GameQueue;
import de.matzefratze123.heavyspleef.core.region.FloorCuboid;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.core.task.PlayerTeleportTask;
import de.matzefratze123.heavyspleef.core.task.RegenerationTask;
import de.matzefratze123.heavyspleef.core.task.RoundsCountdownTask;
import de.matzefratze123.heavyspleef.core.task.StartCountdownTask;
import de.matzefratze123.heavyspleef.core.task.TimeoutTask;
import de.matzefratze123.heavyspleef.database.DatabaseSerializeable;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.hooks.Hook;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.VaultHook;
import de.matzefratze123.heavyspleef.objects.Region;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.RegionCylinder;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.stats.StatisticModule;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.SpleefLogger;
import de.matzefratze123.heavyspleef.util.SpleefLogger.LogType;
import de.matzefratze123.heavyspleef.util.Util;
import de.matzefratze123.heavyspleef.util.ViPManager;

public abstract class Game implements IGame, DatabaseSerializeable {

	// Persistent data
	private String                  name;
	private World                   world;
	private final GameComponents    components;
	private Map<Flag<?>, Object>    flags       = new HashMap<Flag<?>, Object>();
	private final GameQueue         queue;
	private GameState               state;

	// Per game
	/* A map which saves all task id's */
	private Map<String, Integer>    tasks       = new HashMap<String, Integer>();
	private List<SpleefPlayer>      inPlayers   = new ArrayList<SpleefPlayer>();
	private List<OfflinePlayer>     outPlayers  = new ArrayList<OfflinePlayer>();
	private List<SpleefPlayer>      spectating  = new ArrayList<SpleefPlayer>();
	private int                     countLeft;
	private int                     roundsPlayed;
	private int                     jackpot;

	// Temporary
	private PlayerTeleportTask      teleportTask;

	public Game(String name) {
		this.name = name;
		this.components = new GameComponents(this);
		this.queue = new GameQueue(this);
		this.state = GameState.JOINABLE;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public World getWorld() {
		return world;
	}

	protected void setWorld(World world) {
		this.world = world;
	}

	public abstract Location getRandomLocation();

	public abstract void broadcast(String message, BroadcastType type);

	public abstract Region getRegion();

	public abstract GameType getType();

	@Override
	public void broadcast(String message) {
		broadcast(message, BroadcastType.RADIUS);
	}

	@Override
	public void rename(String newName) {
		if (GameManager.hasGame(newName)) {
			return;
		}

		this.name = newName;
		HeavySpleef.getInstance().getGameDatabase().save();
	}

	@Override
	public IGameComponents getComponents() {
		return components;
	}

	@Override
	public void start() {
		cancelTask(StartCountdownTask.TASK_ID_KEY);

		state = GameState.INGAME;
		HeavySpleef.getInstance().getJoinGUI().refresh();
		removeBoxes();

		if (getFlag(FlagType.TIMEOUT) > 0) {
			TimeoutTask timeoutTask = new TimeoutTask(getFlag(FlagType.TIMEOUT), this);
			int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), timeoutTask, 0L, 20L);
			tasks.put(TimeoutTask.TASK_ID_KEY, id);
		}

		if (getFlag(FlagType.REGEN_INTERVALL) > 0) {
			RegenerationTask regenTask = new RegenerationTask(this);
			long intervall = getFlag(FlagType.REGEN_INTERVALL) * 20L;

			int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), regenTask, intervall, intervall);
			tasks.put(RegenerationTask.TASK_ID_KEY, id);
		}

		// Subtract entry fee
		if (HookManager.getInstance().getService(VaultHook.class).hasHook() && getFlag(FlagType.ENTRY_FEE) > 0) {
			Hook<Economy> hook = HookManager.getInstance().getService(VaultHook.class);

			for (SpleefPlayer player : inPlayers) {
				hook.getHook().withdrawPlayer(player.getName(), getFlag(FlagType.ENTRY_FEE));
				player.sendMessage(_("paidIntoJackpot", hook.getHook().format(getFlag(FlagType.ENTRY_FEE))));
				jackpot += getFlag(FlagType.ENTRY_FEE);
			}
		}

		for (SpleefPlayer player : inPlayers) {
			giveItems(player);
			player.getStatistic().addGame();
		}

		StatisticModule.pushAsync();
		broadcast(_("gameHasStarted"), BroadcastType.INGAME);
		broadcast(_("gameOnArenaHasStarted", getName()), ConfigUtil.getBroadcast(MessageType.GAME_START_INFO));
		broadcast(_("startedGameWith", String.valueOf(inPlayers.size())), ConfigUtil.getBroadcast(MessageType.GAME_START_INFO));
		components.updateWalls();
	}

	@SuppressWarnings("deprecation")
	private void giveItems(SpleefPlayer player) {
		// Give items
		List<ItemStack> items = new ArrayList<ItemStack>();

		if (getFlag(FlagType.SHOVELS)) {
			ItemStack shovel = new ItemStack(Material.DIAMOND_SPADE);
			ItemMeta meta = shovel.getItemMeta();
			meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Spleef Shovel");
			shovel.setItemMeta(meta);

			items.add(shovel);
		}

		if (getFlag(FlagType.SHEARS)) {
			ItemStack shear = new ItemStack(Material.SHEARS);
			ItemMeta meta = shear.getItemMeta();
			meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Spleef Shear");
			shear.setItemMeta(meta);

			items.add(shear);
		}

		if (getFlag(FlagType.SPLEGG)) {
			ItemStack spleggLauncher = new ItemStack(Material.DIAMOND_SPADE);
			ItemMeta meta = spleggLauncher.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Splegg Launcher");

			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.YELLOW + "Right click to " + ChatColor.RED + "fire" + ChatColor.YELLOW + "!");
			meta.setLore(lore);

			spleggLauncher.setItemMeta(meta);

			items.add(spleggLauncher);
		}

		if (getFlag(FlagType.BOWSPLEEF)) {
			ItemStack bow = new ItemStack(Material.BOW);
			ItemStack arrow = new ItemStack(Material.ARROW);

			bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
			bow.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 10);

			ItemMeta bowMeta = bow.getItemMeta();
			bowMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Bow");
			bow.setItemMeta(bowMeta);

			ItemMeta arrowMeta = arrow.getItemMeta();
			arrowMeta.setDisplayName(ChatColor.GREEN + "Arrow");
			arrow.setItemMeta(arrowMeta);

			items.add(bow);
			items.add(arrow);
		}

		for (ItemStack item : items) {
			player.getBukkitPlayer().getInventory().addItem(item);
		}

		player.getBukkitPlayer().updateInventory();
	}

	@Override
	public void countdown() {
		//Don't count when we're already counting
		if (state == GameState.COUNTING) {
			return;
		}
		
		SpleefStartEvent event = new SpleefStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		state = GameState.COUNTING;
		HeavySpleef.getInstance().getJoinGUI().refresh();
		components.regenerateFloors();

		StartCountdownTask countdownTask = new StartCountdownTask(getFlag(FlagType.COUNTDOWN), this);
		int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), countdownTask, 0L, 20L);
		tasks.put(StartCountdownTask.TASK_ID_KEY, id);

		teleportTask = new PlayerTeleportTask(this);
		Bukkit.getScheduler().runTask(HeavySpleef.getInstance(), teleportTask);

		components.updateWalls();
		components.updateScoreBoards();
	}

	@Override
	public void stop() {
		stop(StopCause.STOP);
	}

	@Override
	public void stop(StopCause cause) {
		stop(cause, null);
	}

	public void stop(StopCause cause, SpleefPlayer winner) {
		// Cancel all tasks
		for (String task : tasks.keySet()) {
			cancelTask(task);
		}

		SpleefFinishEvent event = new SpleefFinishEvent(this, cause, winner);
		Bukkit.getPluginManager().callEvent(event);

		Iterator<SpleefPlayer> iterator = inPlayers.iterator();
		while (iterator.hasNext()) {
			SpleefPlayer player = iterator.next();
			iterator.remove();
			
			if (components.getTeam(player) != null) {
				components.getTeam(player).leave(player);
			}

			if (winner != null && winner != player) {
				safeTeleport(player, getFlag(FlagType.LOSE));
			}

			player.restoreState();
			player.clearGameData();
			player.getBukkitPlayer().setFireTicks(0);
			player.getBukkitPlayer().setFallDistance(0);
		}

		if (winner != null) {
			broadcast(_("hasWon", ViPManager.colorName(winner.getName()), this.getName()), ConfigUtil.getBroadcast(MessageType.WIN));
			winner.sendMessage(_("win"));
			winner.getStatistic().addWin();
			StatisticModule.pushAsync();
			safeTeleport(winner, getFlag(FlagType.WIN));
			giveRewards(winner, true, 0);
			SpleefLogger.log(LogType.WIN, this, winner);

			if (HeavySpleef.getSystemConfig().getSoundsSection().isPlayLevelUpSound()) {
				winner.getBukkitPlayer().playSound(winner.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 1.0F, 2.0F);
			}
		} else if (cause == StopCause.DRAW) {
			broadcast(_("endedDraw", getName()), ConfigUtil.getBroadcast(MessageType.WIN));
		}

		state = GameState.JOINABLE;
		HeavySpleef.getInstance().getJoinGUI().refresh();
		
		outPlayers.clear();
		removeBoxes();

		components.updateScoreBoards();
		components.updateWalls();
		components.regenerateFloors();
		roundsPlayed = 0;
		jackpot = 0;

		queue.flushQueue();
	}

	/* Extra method to stop the game per a winner team */
	private void stop(Team winnerTeam) {
		// Cancel all tasks
		for (String task : tasks.keySet()) {
			cancelTask(task);
		}

		int teamSize = winnerTeam.getPlayers().size();

		SpleefFinishEvent event = new SpleefFinishEvent(this, StopCause.WIN, null);
		Bukkit.getPluginManager().callEvent(event);

		Iterator<SpleefPlayer> iterator = inPlayers.iterator();
		while (iterator.hasNext()) {
			SpleefPlayer player = iterator.next();

			Team playerTeam = components.getTeam(player);
			iterator.remove();

			if (playerTeam == winnerTeam) {
				giveRewards(player, false, teamSize);
				player.getStatistic().addWin();
			}

			safeTeleport(player, getFlag(FlagType.LOSE));
			player.restoreState();

			playerTeam.leave(player);

			player.clearGameData();
			player.getBukkitPlayer().setFireTicks(0);
			player.getBukkitPlayer().setFallDistance(0);
		}

		broadcast(_("hasWon", "Team " + winnerTeam.getColor().toMessageColorString(), this.getName()), ConfigUtil.getBroadcast(MessageType.WIN));

		StatisticModule.pushAsync();
		state = GameState.JOINABLE;
		HeavySpleef.getInstance().getJoinGUI().refresh();
		
		outPlayers.clear();
		removeBoxes();

		components.updateScoreBoards();
		components.updateWalls();
		components.regenerateFloors();
		roundsPlayed = 0;
		jackpot = 0;

		queue.flushQueue();
	}

	@Override
	public void enable() {
		if (state != GameState.DISABLED) {
			return;
		}

		state = GameState.JOINABLE;
		HeavySpleef.getInstance().getJoinGUI().refresh();
		
		components.updateWalls();
	}

	@Override
	public void disable() {
		if (state == GameState.DISABLED) {
			return;
		}

		if (state == GameState.COUNTING || state == GameState.INGAME || state == GameState.LOBBY) {
			stop();
		}

		state = GameState.DISABLED;
		HeavySpleef.getInstance().getJoinGUI().refresh();
		
		components.updateWalls();
	}

	public void spectate(SpleefPlayer player) {
		if (player.isActive()) {
			player.sendMessage(_("cannotSpectateWhilePlaying"));
			return;
		}

		Location spectate = getFlag(SPECTATE);

		player.saveLocation();
		player.getBukkitPlayer().teleport(spectate);
		player.sendMessage(_("welcomeToSpectate"));
		player.setGame(this);

		spectating.add(player);
	}

	public void leaveSpectate(SpleefPlayer player) {
		if (!isSpectating(player))
			return;

		spectating.remove(player);
		player.clearGameData();
		player.getBukkitPlayer().teleport(player.getLastLocation());
	}

	public List<SpleefPlayer> getSpectating() {
		return spectating;
	}

	@Override
	public void join(SpleefPlayer player) {
		if (player.isActive()) {
			return;
		}

		if (getFlag(FlagType.ONEVSONE) && inPlayers.size() >= 2) {
			return;
		}

		SpleefJoinEvent event = new SpleefJoinEvent(this, player.getBukkitPlayer());
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		broadcast(_("playerJoinedGame", ViPManager.colorName(player.getName())), ConfigUtil.getBroadcast(MessageType.PLAYER_JOIN));

		player.saveState();
		player.setGame(this);
		
		inPlayers.add(player);
		
		HeavySpleef.getInstance().getJoinGUI().refresh();
		SpleefLogger.log(LogType.JOIN, this, player);

		if (state == GameState.JOINABLE) {
			state = GameState.LOBBY;
			
			HeavySpleef.getInstance().getJoinGUI().refresh();
		}

		player.saveLocation();

		if (state == GameState.COUNTING || state == GameState.INGAME) {
			Location spawnpoint = getFlag(FlagType.SPAWNPOINT) == null ? getRandomLocation() : getFlag(FlagType.SPAWNPOINT);

			player.getBukkitPlayer().teleport(spawnpoint);
		} else {
			Location lobby = getFlag(FlagType.LOBBY) == null ? getRandomLocation() : getFlag(FlagType.LOBBY);

			player.getBukkitPlayer().teleport(lobby);
		}

		if (HeavySpleef.getSystemConfig().getSoundsSection().isPlayPlingSound()) {
			for (SpleefPlayer inPlayer : inPlayers)
				inPlayer.getBukkitPlayer().playSound(inPlayer.getBukkitPlayer().getLocation(), Sound.NOTE_PLING, 4.0F, inPlayer.getBukkitPlayer().getLocation().getPitch());
		}

		components.updateWalls();

		// Autostart
		if ((getFlag(FlagType.AUTOSTART) > 1 && inPlayers.size() >= getFlag(FlagType.AUTOSTART)) || (getFlag(FlagType.ONEVSONE) && inPlayers.size() >= 2)) {
			countdown();
		}
	}

	@Override
	public void leave(SpleefPlayer player) {
		leave(player, LoseCause.PLUGIN);
	}

	@Override
	public void leave(SpleefPlayer player, LoseCause cause) {
		if (!inPlayers.contains(player)) {
			return;
		}

		if (cause == null) {
			cause = LoseCause.UNKNOWN;
		}

		if (cause == LoseCause.LOSE && getFlag(FlagType.ONEVSONE)) {
			// Indicate the winner
			SpleefPlayer winner = null;

			for (SpleefPlayer inPlayer : inPlayers) {
				HeavySpleef.getInstance().getAntiCampingTask().resetTimer(player.getBukkitPlayer());

				if (inPlayer != player) {
					winner = inPlayer;
					break;
				}
			}

			if (winner == null) {
				winner = player;
				roundsPlayed = getFlag(FlagType.ROUNDS);
			}

			winner.addWin();
			winner.addKnockout();
			roundsPlayed++;

			if (roundsPlayed < getFlag(FlagType.ROUNDS)) {
				// Play one round more

				components.regenerateFloors();

				teleportTask = new PlayerTeleportTask(this);
				Bukkit.getScheduler().runTask(HeavySpleef.getInstance(), teleportTask);

				int countdown = getFlag(FlagType.COUNTDOWN);
				RoundsCountdownTask task = new RoundsCountdownTask(countdown, this);
				int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), task, 0L, 20L);
				tasks.put(RoundsCountdownTask.TASK_ID_KEY, id);

				broadcast(_("wonRound", ViPManager.colorName(winner.getName()), String.valueOf(roundsPlayed), String.valueOf(getFlag(FlagType.ROUNDS))), ConfigUtil.getBroadcast(MessageType.WIN));
				broadcast(_("roundsRemaining", String.valueOf(getFlag(FlagType.ROUNDS) - roundsPlayed)), ConfigUtil.getBroadcast(MessageType.WIN));

				components.updateWalls();
				components.updateScoreBoards();
			} else {
				// Game end, indicate the final winner
				SpleefPlayer finalWinner = null;

				if (inPlayers.size() == 2) {
					finalWinner = inPlayers.get(0).getWins() > inPlayers.get(1).getWins() ? inPlayers.get(0) : inPlayers.get(0).getWins() == inPlayers.get(1).getWins() ? null : inPlayers.get(1);
				}

				if (finalWinner != null) {
					stop(StopCause.WIN, finalWinner);
				} else {
					stop(StopCause.DRAW);
				}
			}
		} else {
			inPlayers.remove(player);
			HeavySpleef.getInstance().getJoinGUI().refresh();
			
			if (components.getTeam(player) != null) {
				components.getTeam(player).leave(player);
			}

			player.restoreState();

			SpleefPlayer killer = detectKiller(player);

			if (cause == LoseCause.LOSE) {
				player.sendMessage(_("outOfGame"));
				player.getStatistic().addLose();
				
				SpleefLoseEvent event = new SpleefLoseEvent(this, player.getBukkitPlayer(), killer, cause);
				Bukkit.getPluginManager().callEvent(event);
				
				SpleefLogger.log(LogType.LOSE, this, player);

				if (killer != null) {
					killer.addKnockout();
					killer.getStatistic().addKnockout();

					broadcast(_("loseCause_lose", ViPManager.colorName(player.getName()), ViPManager.colorName(killer.getName())), ConfigUtil.getBroadcast(MessageType.PLAYER_LOSE));
				} else {
					broadcast(_("loseCause_lose_unknown", ViPManager.colorName(player.getName())), ConfigUtil.getBroadcast(MessageType.PLAYER_LOSE));
				}

				StatisticModule.pushAsync();
			} else {
				SpleefLogger.log(LogType.LEAVE, this, player);
				broadcast(_("loseCause_leave", ViPManager.colorName(player.getName()), name), ConfigUtil.getBroadcast(MessageType.PLAYER_LOSE));
			}

			safeTeleport(player, getFlag(FlagType.LOSE));

			if (state == GameState.INGAME || state == GameState.COUNTING) {
				outPlayers.add(player.getBukkitPlayer());
				broadcast(_("remaining", String.valueOf(inPlayers.size())), ConfigUtil.getBroadcast(MessageType.KNOCKOUTS));
			}

			player.clearGameData();
			player.getBukkitPlayer().setFireTicks(0);
			player.getBukkitPlayer().setFallDistance(0);

			components.updateScoreBoards();
			components.updateWalls();

			if (inPlayers.size() <= 1 && !getFlag(FlagType.TEAM)) {
				if (state == GameState.INGAME) {
					stop(StopCause.WIN, inPlayers.get(0));
				} else if (state != GameState.LOBBY) {
					stop();
				}
			} else if (getFlag(FlagType.TEAM)) {
				if (state == GameState.INGAME) {
					List<Team> active = components.getActiveTeams();

					if (active.size() <= 1) {
						stop(active.get(0));
					}
				} else if (state != GameState.LOBBY) {
					stop();
				}
			}
		}
	}

	private SpleefPlayer detectKiller(SpleefPlayer player) {
		List<IFloor> floors = components.getFloors();
		Collections.sort(floors);

		IFloor floor = floors.get(0);

		Location playerLocation = player.getBukkitPlayer().getLocation();
		Block above = player.getBukkitPlayer().getWorld().getBlockAt(playerLocation.getBlockX(), floor.getY(), playerLocation.getBlockZ());

		for (Player bukkitPlayer : Bukkit.getOnlinePlayers()) {
			SpleefPlayer spleefPlayer = HeavySpleef.getInstance().getSpleefPlayer(bukkitPlayer);

			List<Block> brokenBlocks = spleefPlayer.getBrokenBlocks();
			if (brokenBlocks == null || brokenBlocks.isEmpty()) {
				continue;
			}

			for (Block block : brokenBlocks) {
				if (block.equals(above)) {
					// We got the killer!
					return spleefPlayer;
				}
			}
		}

		return null;
	}

	@Override
	public boolean hasPlayer(SpleefPlayer player) {
		return inPlayers.contains(player);
	}

	public boolean isSpectating(SpleefPlayer player) {
		return spectating.contains(player);
	}

	@Override
	public List<SpleefPlayer> getIngamePlayers() {
		return inPlayers;
	}

	@Override
	public List<OfflinePlayer> getOutPlayers() {
		return outPlayers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Flag<V>, V> V getFlag(T flag) {
		Object o = flags.get(flag);
		V value = null;

		if (o == null) {
			value = HeavySpleef.getSystemConfig().getFlagDefaultsSection().getFlagDefault(flag);
			if (value == null)
				return flag.getAbsoluteDefault();
		} else {
			value = (V) o;
		}

		return value;
	}

	@Override
	public <T extends Flag<V>, V> void setFlag(T flag, V value) {
		if (value == null) {
			flags.remove(flag);
		} else {
			flags.put(flag, value);
		}
	}

	@Override
	public boolean hasFlag(Flag<?> flag) {
		return flags.containsKey(flag);
	}

	@Override
	public Map<Flag<?>, Object> getFlags() {
		return flags;
	}

	public void setFlags(Map<Flag<?>, Object> flags) {
		this.flags = flags;
	}

	@Override
	public boolean canSpleef(SpleefPlayer player, Location location) {
		if (!inPlayers.contains(player)) {
			return false;
		}
		if (state != GameState.INGAME) {
			return false;
		}

		for (IFloor floor : components.getFloors()) {
			if (floor.contains(location)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public GameState getGameState() {
		return state;
	}

	@Override
	public void setGameState(GameState state) {
		this.state = state;
		
		HeavySpleef.getInstance().getJoinGUI().refresh();
	}

	public void cancelTask(String key) {
		if (!tasks.containsKey(key)) {
			return;
		}

		BukkitScheduler scheduler = Bukkit.getScheduler();

		int id = tasks.get(key);
		if (!scheduler.isCurrentlyRunning(id) && !scheduler.isQueued(id)) {
			return;
		}

		scheduler.cancelTask(id);
	}

	public void setCountLeft(int countLeft) {
		this.countLeft = countLeft;
	}

	public int getCountLeft() {
		return countLeft;
	}

	public int getRoundsPlayed() {
		return roundsPlayed;
	}

	private static String _(String... key) {
		return I18N._(key);
	}

	private void safeTeleport(SpleefPlayer player, Location location) {
		if (location == null) {
			Location last = player.getLastLocation();
			player.getBukkitPlayer().teleport(last);
		} else {
			player.getBukkitPlayer().teleport(location);
		}
	}

	public boolean isReadyToPlay() {
		return getFlag(FlagType.LOBBY) != null && components.getFloors().size() > 0;
	}

	public void removeBoxes() {
		if (teleportTask != null) {
			teleportTask.removeBoxes();
		}
	}

	@Override
	public GameQueue getQueue() {
		return queue;
	}

	/**
	 * Gives spleef rewards to the given player Possible rewards: Money reward,
	 * Jackpot reward, Item reward
	 * 
	 * @param player
	 *            Player who should receive rewards
	 * @param clearJackpot
	 *            If the jackpot should be cleared (resets the jackpot)
	 * @param winnersTeamSize
	 *            How many players won the game. This parameter makes sure that
	 *            the money reward is splitted up equally
	 */
	public void giveRewards(SpleefPlayer player, boolean clearJackpot, int winnersTeamSize) {
		if (getFlag(ITEMREWARD) != null) {
			for (SerializeableItemStack stack : getFlag(ITEMREWARD)) {
				ItemStack bukkitStack = stack.toBukkitStack();
				player.getBukkitPlayer().getInventory().addItem(bukkitStack);
				player.sendMessage(_("itemRewardReceived", String.valueOf(stack.getAmount()), Util.formatMaterial(stack.getMaterial())));
			}
		}

		if (HookManager.getInstance().getService(VaultHook.class).hasHook()) {
			Economy econ = HookManager.getInstance().getService(VaultHook.class).getHook();
			if (this.jackpot > 0) {
				// Split the reward between the winning teams
				double prize;

				if (getFlag(TEAM)) {
					prize = (double) jackpot / winnersTeamSize;
				} else {
					prize = jackpot;
				}

				EconomyResponse r = econ.depositPlayer(player.getName(), prize);
				player.sendMessage(_("jackpotReceived", econ.format(r.amount)));

				if (clearJackpot) {
					jackpot = 0;
				}
			}

			int reward = getFlag(REWARD);

			if (reward > 0) {
				EconomyResponse r = econ.depositPlayer(player.getName(), reward);
				player.sendMessage(_("rewardReceived", econ.format(r.amount)));
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Game other = (Game) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public ConfigurationSection serialize() {
		MemorySection section = new MemoryConfiguration();

		section.set("type", getType().name());
		section.set("name", getName());

		ConfigurationSection currentSection;
		/* Serialize game components */

		currentSection = section.createSection("floors");
		for (IFloor floor : components.getFloors()) {
			String id = String.valueOf(floor.getId());
			ConfigurationSection serialized = floor.serialize();

			currentSection.createSection(id, serialized.getValues(true));
		}

		currentSection = section.createSection("losezones");
		for (LoseZone zone : components.getLoseZones()) {
			String id = String.valueOf(zone.getId());

			currentSection.createSection(id, zone.serialize().getValues(true));
		}

		currentSection = section.createSection("scoreboards");
		for (ScoreBoard board : components.getScoreBoards()) {
			String id = String.valueOf(board.getId());

			currentSection.createSection(id, board.serialize().getValues(true));
		}

		currentSection = section.createSection("signwalls");
		for (SignWall wall : components.getSignWalls()) {
			String id = String.valueOf(wall.getId());

			currentSection.createSection(id, wall.serialize().getValues(true));
		}

		currentSection = section.createSection("teams");

		for (Team team : components.getTeams()) {
			Color color = team.getColor();
			int minPlayers = team.getMinPlayers();
			int maxPlayers = team.getMaxPlayers();

			ConfigurationSection teamSection = currentSection.createSection(color.name());
			teamSection.set("color", color.name());
			teamSection.set("min-players", minPlayers);
			teamSection.set("max-players", maxPlayers);
		}

		/* Serialize flags */
		currentSection = section.createSection("flags");

		for (Entry<Flag<?>, Object> entry : flags.entrySet()) {
			Flag<?> flag = entry.getKey();
			Object value = entry.getValue();

			ConfigurationSection flagSection = currentSection.createSection(flag.getName());
			flagSection.set("value", flag.serialize(value));
		}

		return section;
	}

	public static Game deserialize(ConfigurationSection section) {
		GameType type = GameType.valueOf(section.getString("type"));
		String name = section.getString("name");

		Game game = null;

		if (type == GameType.CUBOID) {
			Location first = Parser.convertStringtoLocation(section.getString("first"));
			Location second = Parser.convertStringtoLocation(section.getString("second"));

			RegionCuboid region = new RegionCuboid(-1, first, second);

			game = new GameCuboid(name, region);
		} else if (type == GameType.CYLINDER) {
			Location center = Parser.convertStringtoLocation(section.getString("center"));
			int radius = section.getInt("radius");
			int min = section.getInt("min");
			int max = section.getInt("max");

			RegionCylinder region = new RegionCylinder(-1, center, radius, min, max);

			game = new GameCylinder(name, region);
		}

		ConfigurationSection currentSection;

		currentSection = section.getConfigurationSection("floors");
		for (String key : currentSection.getKeys(false)) {
			ConfigurationSection floorSection = currentSection.getConfigurationSection(key);

			IFloor floor = FloorCuboid.deserialize(floorSection, game);

			game.components.addFloor(floor, false);
		}

		currentSection = section.getConfigurationSection("losezones");
		for (String key : currentSection.getKeys(false)) {
			ConfigurationSection losezoneSection = currentSection.getConfigurationSection(key);

			LoseZone zone = LoseZone.deserialize(losezoneSection);
			game.getComponents().addLoseZone(zone);
		}

		currentSection = section.getConfigurationSection("scoreboards");
		for (String key : currentSection.getKeys(false)) {
			ConfigurationSection scoreboardSection = currentSection.getConfigurationSection(key);

			ScoreBoard board = ScoreBoard.deserialize(scoreboardSection);

			game.getComponents().addScoreBoard(board);
		}

		currentSection = section.getConfigurationSection("signwalls");
		for (String key : currentSection.getKeys(false)) {
			ConfigurationSection wallSection = currentSection.getConfigurationSection(key);

			SignWall wall = SignWall.deserialize(wallSection);
			wall.setGame(game);

			game.getComponents().addSignWall(wall);
		}

		currentSection = section.getConfigurationSection("teams");
		for (String key : currentSection.getKeys(false)) {
			ConfigurationSection teamSection = currentSection.getConfigurationSection(key);

			Color color = Color.byName(teamSection.getString("color"));
			int minPlayers = teamSection.getInt("min-players");
			int maxPlayers = teamSection.getInt("max-players");

			Team team = new Team(color);
			team.setMinPlayers(minPlayers);
			team.setMaxPlayers(maxPlayers);

			game.getComponents().addTeam(team);
		}

		currentSection = section.getConfigurationSection("flags");
		Map<Flag<?>, Object> flags = new HashMap<Flag<?>, Object>();

		for (String key : currentSection.getKeys(false)) {
			ConfigurationSection flagSection = currentSection.getConfigurationSection(key);

			Flag<?> flag = FlagType.byName(key);
			if (flag == null) {
				continue;
			}
			
			Object value = flag.deserialize(flagSection.getString("value"));

			flags.put(flag, value);
		}

		game.setFlags(flags);

		return game;
	}

}
