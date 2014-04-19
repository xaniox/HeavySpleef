/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
package de.matzefratze123.heavyspleef.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.CommandVote;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.stats.AccountException;
import de.matzefratze123.heavyspleef.stats.StatisticModule;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Logger;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefPlayer {

	private Player			bukkitPlayer;
	private boolean			isOnline;

	private Game			game;
	private boolean			ready			= false;
	private List<Block>		brokenBlocks	= new ArrayList<Block>();
	private int				knockouts;
	private int				wins;

	private PlayerState		state;
	private Location		lastLocation;
	private StatisticModule	statistic;
	private boolean			statisticsLoaded;

	public SpleefPlayer(Player bukkitPlayer) {
		this.bukkitPlayer = bukkitPlayer;
		this.isOnline = true;
		this.statistic = new StatisticModule(bukkitPlayer.getName());
	}

	public Player getBukkitPlayer() {
		return bukkitPlayer;
	}

	public String getRawName() {
		return bukkitPlayer.getName();
	}

	public String getName() {
		String name = getRawName();

		if (bukkitPlayer.hasPermission(Permissions.VIP.getPerm())) {
			name = HeavySpleef.getSystemConfig().getGeneralSection().getVipPrefix() + name;
		} else if (getRawName().equalsIgnoreCase("matzefratze123")) {
			name = ChatColor.DARK_RED + name;
		}

		return name;
	}

	public void sendMessage(String message) {
		bukkitPlayer.sendMessage(message);
	}

	public boolean hasPermission(Permissions permission) {
		return bukkitPlayer.hasPermission(permission.getPerm());
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public boolean isActive() {
		return game != null && game.hasPlayer(this);
	}

	public boolean isIngame() {
		return game != null && game.hasPlayer(this) && game.getGameState() == GameState.INGAME;
	}

	public boolean isSpectating() {
		return game != null && game.isSpectating(this);
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;

		CommandVote.tryStart(game);
	}

	public void addBrokenBlock(Block block) {
		brokenBlocks.add(block);
	}

	public List<Block> getBrokenBlocks() {
		return brokenBlocks;
	}

	public int getKnockouts() {
		return knockouts;
	}

	public void setKnockouts(int knockouts) {
		this.knockouts = knockouts;
	}

	public void addKnockout() {
		this.knockouts += 1;
	}

	public void clearGameData() {
		game = null;
		ready = false;
		brokenBlocks.clear();
		knockouts = 0;
		wins = 0;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean online) {
		this.isOnline = online;
	}

	public void setLastLocation(Location location) {
		this.lastLocation = location;
	}

	public void saveLocation() {
		this.lastLocation = bukkitPlayer.getLocation();
	}

	public Location getLastLocation() {
		return lastLocation;
	}

	public StatisticModule getStatistic() {
		if (statistic == null) {
			statistic = new StatisticModule(getRawName());
		}

		return statistic;
	}

	public void setStatistic(StatisticModule module) {
		this.statistic = module;
	}

	public boolean statisticsWereLoaded() {
		return statisticsLoaded;
	}

	public void loadStatistics() {
		Runnable loader = new Runnable() {

			@Override
			public void run() {
				if (statisticsLoaded) {
					try {
						HeavySpleef.getInstance().getStatisticDatabase().saveAccounts();
					} catch (AccountException e) {
						Logger.severe("Failed to save accounts. Will not try to load account of player " + getRawName() + ": " + e.getMessage());
						return;
					}
				}

				statisticsLoaded = false;

				try {
					StatisticModule module = HeavySpleef.getInstance().getStatisticDatabase().loadAccount(getRawName());

					if (module != null) {
						synchronized (statistic) {
							statistic.merge(module);
						}
					}

					statisticsLoaded = true;
				} catch (AccountException e) {
					Logger.severe("Failed to load account of player " + getRawName() + ": " + e.getMessage());
				}
			}
		};

		Bukkit.getScheduler().runTaskAsynchronously(HeavySpleef.getInstance(), loader);
	}

	public PlayerState getState() {
		return state;
	}

	@SuppressWarnings("deprecation")
	public void saveState() {
		bukkitPlayer.setGameMode(GameMode.SURVIVAL);// Set to survival

		// Define player states variables
		ItemStack[] contents = bukkitPlayer.getInventory().getContents();

		ItemStack helmet = bukkitPlayer.getInventory().getHelmet();
		ItemStack chestplate = bukkitPlayer.getInventory().getChestplate();
		ItemStack leggings = bukkitPlayer.getInventory().getLeggings();
		ItemStack boots = bukkitPlayer.getInventory().getBoots();

		float exhaustion = bukkitPlayer.getExhaustion();
		float saturation = bukkitPlayer.getSaturation();

		int foodLevel = bukkitPlayer.getFoodLevel();
		double health = bukkitPlayer.getHealth();

		GameMode gm = bukkitPlayer.getGameMode();
		Collection<PotionEffect> potionEffects = bukkitPlayer.getActivePotionEffects();

		float exp = bukkitPlayer.getExp();
		int level = bukkitPlayer.getLevel();

		boolean fly = bukkitPlayer.getAllowFlight();
		Scoreboard board = bukkitPlayer.getScoreboard();
		
		List<SpleefPlayer> hiddenPlayers = new ArrayList<SpleefPlayer>();
		
		for (SpleefPlayer onlinePlayer : HeavySpleef.getInstance().getOnlineSpleefPlayers()) {
			if (!bukkitPlayer.canSee(onlinePlayer.getBukkitPlayer())) {
				hiddenPlayers.add(onlinePlayer);
				bukkitPlayer.showPlayer(onlinePlayer.getBukkitPlayer());
			}
		}

		// Save state
		state = new PlayerState(contents, helmet, chestplate, leggings, boots, exhaustion, saturation, foodLevel, health, gm, potionEffects, exp, level, fly, board, hiddenPlayers);

		// Set to default state
		bukkitPlayer.setFoodLevel(20);
		bukkitPlayer.setHealth(20.0);
		bukkitPlayer.setAllowFlight(false);// Disable fly mode (Essentials etc.)
		bukkitPlayer.setFireTicks(0);
		bukkitPlayer.getInventory().clear();
		bukkitPlayer.getInventory().setArmorContents(new ItemStack[4]);
		bukkitPlayer.setLevel(0);
		bukkitPlayer.setExp(0);

		for (PotionEffect effect : bukkitPlayer.getActivePotionEffects()) {
			bukkitPlayer.removePotionEffect(effect.getType());
		}

		bukkitPlayer.sendMessage(I18N._("stateSaved"));
		bukkitPlayer.updateInventory();
	}

	@SuppressWarnings("deprecation")
	public void restoreState() {
		if (state == null) {
			return;
		}

		bukkitPlayer.getInventory().setContents(state.getContents());
		bukkitPlayer.getInventory().setHelmet(state.getHelmet());
		bukkitPlayer.getInventory().setChestplate(state.getChestplate());
		bukkitPlayer.getInventory().setLeggings(state.getLeggings());
		bukkitPlayer.getInventory().setBoots(state.getBoots());

		bukkitPlayer.setExhaustion(state.getExhaustion());
		bukkitPlayer.setSaturation(state.getSaturation());

		bukkitPlayer.setFoodLevel(state.getFoodLevel());
		bukkitPlayer.setHealth(state.getHealth());

		bukkitPlayer.addPotionEffects(state.getPotioneffects());

		bukkitPlayer.setLevel(state.getLevel());
		bukkitPlayer.setExp(state.getExp());
		bukkitPlayer.setAllowFlight(state.isFly());
		bukkitPlayer.setGameMode(state.getGamemode());
		
		List<SpleefPlayer> hiddenPlayers = state.getCantSee();
		
		for (SpleefPlayer hiddenPlayer : hiddenPlayers) {
			if (!hiddenPlayer.isOnline()) {
				continue;
			}
			
			bukkitPlayer.hidePlayer(hiddenPlayer.getBukkitPlayer());
		}
		
		sendMessage(I18N._("stateRestored"));
		bukkitPlayer.updateInventory();
		state = null;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public void addWin() {
		this.wins += 1;
	}

}
