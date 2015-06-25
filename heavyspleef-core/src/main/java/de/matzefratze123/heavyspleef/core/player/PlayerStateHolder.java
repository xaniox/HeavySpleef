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
package de.matzefratze123.heavyspleef.core.player;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import lombok.Data;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Lists;

@Data
public class PlayerStateHolder {
	
	/* Saving the inventory and the armor contents */
	private static final int SIMPLE_INVENTORY_SIZE = 4 * 9;
	private static final int ARMOR_INVENTORY_SIZE = 4;
	private static final int INVENTORY_SIZE = SIMPLE_INVENTORY_SIZE + ARMOR_INVENTORY_SIZE; 
	
	private ItemStack[] inventory;
	private GameMode gamemode;
	private double health;
	private int foodLevel;
	private int level;
	private float experience;
	private boolean allowFlight;
	private boolean isFlying;
	private Collection<PotionEffect> activeEffects;
	private float exhaustion;
	private float saturation;
	private float fallDistance;
	private int fireTicks;
	private List<WeakReference<Player>> cantSee;
	private Scoreboard scoreboard;
	private Location compassTarget;
	
	private Location location;
	
	/* Post initialization via #create(Player) */
	private PlayerStateHolder() {}
	
	public static PlayerStateHolder create(Player player) {
		PlayerStateHolder stateHolder = new PlayerStateHolder();
		
		PlayerInventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		ItemStack[] armor = inventory.getArmorContents();
		
		ItemStack[] inventoryArray = new ItemStack[INVENTORY_SIZE];
		System.arraycopy(contents, 0, inventoryArray, 0, contents.length);
		System.arraycopy(armor, 0, inventoryArray, inventoryArray.length - ARMOR_INVENTORY_SIZE - 1, armor.length);
		
		/* Initialize the state with the current player state */
		stateHolder.setInventory(inventoryArray);
		stateHolder.setGamemode(player.getGameMode());
		stateHolder.setHealth(player.getHealth());
		stateHolder.setFoodLevel(player.getFoodLevel());
		stateHolder.setLevel(player.getLevel());
		stateHolder.setExperience(player.getExp());
		stateHolder.setAllowFlight(player.getAllowFlight());
		stateHolder.setFlying(player.isFlying());
		stateHolder.setActiveEffects(player.getActivePotionEffects());
		stateHolder.setExhaustion(player.getExhaustion());
		stateHolder.setSaturation(player.getSaturation());
		stateHolder.setFallDistance(player.getFallDistance());
		stateHolder.setFireTicks(player.getFireTicks());
		stateHolder.setLocation(player.getLocation());
		stateHolder.setScoreboard(player.getScoreboard());
		stateHolder.setCompassTarget(player.getCompassTarget());
		
		List<WeakReference<Player>> cantSee = Lists.newArrayList();
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (player.canSee(onlinePlayer)) {
				continue;
			}
			
			WeakReference<Player> ref = new WeakReference<Player>(onlinePlayer);
			cantSee.add(ref);
		}
		
		stateHolder.setCantSee(cantSee);
		
		return stateHolder;
	}

	/**
	 * Applies the default state to the player
	 * and discards the current one<br><br>
	 * 
	 * Warning: This deletes the entire inventory
	 * and all other various player attributes
	 * 
	 * It is recommended to save the player state
	 * with {@link PlayerStateHolder#create(Player)}
	 * and store a reference to it before invoking this method
	 * 
	 * @param player
	 */
	@SuppressWarnings("deprecation")
	public static void applyDefaultState(Player player) {
		player.getInventory().clear();
		player.updateInventory();
		player.setHealth(20.0);
		player.setGameMode(GameMode.SURVIVAL);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0f);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setFallDistance(0);
		player.setFireTicks(0);
		
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		for (PotionEffect effect : effects) {
			player.removePotionEffect(effect.getType());
		}
		
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (player.canSee(player)) {
				continue;
			}
			
			player.showPlayer(onlinePlayer);
		}
	}
	
	public void apply(Player player) {
		apply(player, true);
	}
	
	@SuppressWarnings("deprecation")
	public void apply(Player player, boolean teleport) {
		PlayerInventory playerInv = player.getInventory();
		
		ItemStack[] inventoryContents = new ItemStack[SIMPLE_INVENTORY_SIZE];
		ItemStack[] armorContents = new ItemStack[ARMOR_INVENTORY_SIZE];
		
		System.arraycopy(inventory, 0, inventoryContents, 0, inventoryContents.length);
		System.arraycopy(inventory, inventory.length - 5, armorContents, 0, armorContents.length);
		
		playerInv.setContents(inventoryContents);
		playerInv.setArmorContents(armorContents);
		player.updateInventory();
		
		player.setGameMode(gamemode);
		player.setHealth(health);
		player.setFoodLevel(foodLevel);
		player.setLevel(level);
		player.setExp(experience);
		player.setAllowFlight(allowFlight);
		player.setFlying(isFlying);
		
		/* Remove current potion effects */
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		for (PotionEffect effect : effects) {
			player.removePotionEffect(effect.getType());
		}
		player.addPotionEffects(activeEffects);
		
		player.setExhaustion(exhaustion);
		player.setSaturation(saturation);
		player.setFallDistance(fallDistance);
		player.setFireTicks(fireTicks);
		
		if (scoreboard != player.getScoreboard()) {
			Scoreboard showBoard = scoreboard;
			if (scoreboard == null) {
				showBoard = Bukkit.getScoreboardManager().getMainScoreboard();
			}
			
			player.setScoreboard(showBoard);
		}
		
		if (teleport) {
			player.teleport(location);
		}
		
		Location compassTarget = this.compassTarget;
		if (compassTarget == null) {
			compassTarget = player.getWorld().getSpawnLocation();
		}
		
		player.setCompassTarget(compassTarget);
		
		for (WeakReference<Player> ref : cantSee) {
			Player cantSeePlayer = ref.get();
			
			if (cantSeePlayer == null) {
				// Player object has been garbage-collected
				continue;
			}
			
			if (!cantSeePlayer.isOnline()) {
				continue;
			}
			
			player.hidePlayer(cantSeePlayer);
		}
	}
	
}