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
package de.xaniox.heavyspleef.core.player;

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PlayerStateHolder {
	
	/* Saving the inventory and the armor contents */
	private static final int SIMPLE_INVENTORY_SIZE = 4 * 9;
	private static final int ARMOR_INVENTORY_SIZE = 4;
	private static final int INVENTORY_SIZE = SIMPLE_INVENTORY_SIZE + ARMOR_INVENTORY_SIZE;
	
	private ItemStack[] inventory;
	private ItemStack onCursor;
	private GameMode gamemode;
    private double maxHealth;
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
	
	public PlayerStateHolder() {}
	
	public static PlayerStateHolder create(Player player, GameMode gameMode) {
		PlayerStateHolder stateHolder = new PlayerStateHolder();
		stateHolder.updateState(player, true, gameMode);
		
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
	 * with {@link PlayerStateHolder#create(Player, GameMode)}
	 * and store a reference to it before invoking this method
	 * 
	 * @param player
	 */
	public static void applyDefaultState(Player player) {
		player.setGameMode(GameMode.SURVIVAL);
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);
		player.setItemOnCursor(null);
		player.updateInventory();
        player.setMaxHealth(20.0);
		player.setHealth(20.0);
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
	
	public void apply(Player player, boolean teleport) {
		PlayerInventory playerInv = player.getInventory();
		boolean is1_9 = MinecraftVersion.getImplementationVersion() >= MinecraftVersion.V1_9;
        boolean isSimpleSize = playerInv.getSize() <= SIMPLE_INVENTORY_SIZE;

        ItemStack[] inventoryContents = new ItemStack[is1_9 && !isSimpleSize ? playerInv.getSize() : SIMPLE_INVENTORY_SIZE];
        System.arraycopy(inventory, 0, inventoryContents, 0, inventoryContents.length);

        if (!is1_9 || isSimpleSize) {
            ItemStack[] armorContents = new ItemStack[ARMOR_INVENTORY_SIZE];
            System.arraycopy(inventory, inventory.length - ARMOR_INVENTORY_SIZE, armorContents, 0, armorContents.length);
            playerInv.setArmorContents(armorContents);
        }
		
		playerInv.setContents(inventoryContents);

		player.setItemOnCursor(null);
		Map<Integer, ItemStack> exceeded = playerInv.addItem(onCursor);
		for (ItemStack stack : exceeded.values()) {
			player.getWorld().dropItem(player.getLocation(), stack);
		}
		
		player.updateInventory();

        player.setMaxHealth(maxHealth);
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
		
		player.setGameMode(gamemode);
	}
	
	public void updateState(Player player, boolean location, GameMode mode) {
		PlayerInventory inventory = player.getInventory();
        boolean is1_9 = MinecraftVersion.getImplementationVersion() >= MinecraftVersion.V1_9;
        boolean isSimpleSize = inventory.getSize() <= SIMPLE_INVENTORY_SIZE;

        ItemStack[] contents = inventory.getContents();
        ItemStack[] inventoryArray = new ItemStack[is1_9 && !isSimpleSize ? inventory.getSize() : INVENTORY_SIZE];

        if (!is1_9 || isSimpleSize) {
            ItemStack[] armor = inventory.getArmorContents();
            System.arraycopy(contents, 0, inventoryArray, 0, contents.length);
            System.arraycopy(armor, 0, inventoryArray, inventoryArray.length - ARMOR_INVENTORY_SIZE, armor.length);
        } else {
            System.arraycopy(contents, 0, inventoryArray, 0, contents.length);
        }
		
		/* Initialize the state with the current player state */
		setInventory(inventoryArray);
		setOnCursor(player.getItemOnCursor());
		setGamemode(mode != null ? mode : player.getGameMode());
        setMaxHealth(player.getMaxHealth());
		setHealth(player.getHealth());
		setFoodLevel(player.getFoodLevel());
		setLevel(player.getLevel());
		setExperience(player.getExp());
		setAllowFlight(player.getAllowFlight());
		setFlying(player.isFlying());
		setActiveEffects(player.getActivePotionEffects());
		setExhaustion(player.getExhaustion());
		setSaturation(player.getSaturation());
		setFallDistance(player.getFallDistance());
		setFireTicks(player.getFireTicks());
		
		if (location) {
			setLocation(player.getLocation());
		}
		
		setScoreboard(player.getScoreboard());
		setCompassTarget(player.getCompassTarget());
		
		List<WeakReference<Player>> cantSee = Lists.newArrayList();
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (player.canSee(onlinePlayer)) {
				continue;
			}
			
			WeakReference<Player> ref = new WeakReference<Player>(onlinePlayer);
			cantSee.add(ref);
		}
		
		setCantSee(cantSee);
	}

	public ItemStack[] getInventory() {
		return inventory;
	}

	public void setInventory(ItemStack[] inventory) {
		this.inventory = inventory;
	}
	
	public ItemStack getOnCursor() {
		return onCursor;
	}
	
	public void setOnCursor(ItemStack onCursor) {
		this.onCursor = onCursor;
	}

	public GameMode getGamemode() {
		return gamemode;
	}

	public void setGamemode(GameMode gamemode) {
		this.gamemode = gamemode;
	}

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public int getFoodLevel() {
		return foodLevel;
	}

	public void setFoodLevel(int foodLevel) {
		this.foodLevel = foodLevel;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public float getExperience() {
		return experience;
	}

	public void setExperience(float experience) {
		this.experience = experience;
	}

	public boolean isAllowFlight() {
		return allowFlight;
	}

	public void setAllowFlight(boolean allowFlight) {
		this.allowFlight = allowFlight;
	}

	public boolean isFlying() {
		return isFlying;
	}

	public void setFlying(boolean isFlying) {
		this.isFlying = isFlying;
	}

	public Collection<PotionEffect> getActiveEffects() {
		return activeEffects;
	}

	public void setActiveEffects(Collection<PotionEffect> activeEffects) {
		this.activeEffects = activeEffects;
	}

	public float getExhaustion() {
		return exhaustion;
	}

	public void setExhaustion(float exhaustion) {
		this.exhaustion = exhaustion;
	}

	public float getSaturation() {
		return saturation;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public float getFallDistance() {
		return fallDistance;
	}

	public void setFallDistance(float fallDistance) {
		this.fallDistance = fallDistance;
	}

	public int getFireTicks() {
		return fireTicks;
	}

	public void setFireTicks(int fireTicks) {
		this.fireTicks = fireTicks;
	}

	public List<WeakReference<Player>> getCantSee() {
		return cantSee;
	}

	public void setCantSee(List<WeakReference<Player>> cantSee) {
		this.cantSee = cantSee;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public Location getCompassTarget() {
		return compassTarget;
	}

	public void setCompassTarget(Location compassTarget) {
		this.compassTarget = compassTarget;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeEffects == null) ? 0 : activeEffects.hashCode());
		result = prime * result + (allowFlight ? 1231 : 1237);
		result = prime * result + ((cantSee == null) ? 0 : cantSee.hashCode());
		result = prime * result + ((compassTarget == null) ? 0 : compassTarget.hashCode());
		result = prime * result + Float.floatToIntBits(exhaustion);
		result = prime * result + Float.floatToIntBits(experience);
		result = prime * result + Float.floatToIntBits(fallDistance);
		result = prime * result + fireTicks;
		result = prime * result + foodLevel;
		result = prime * result + ((gamemode == null) ? 0 : gamemode.hashCode());
		long temp;
		temp = Double.doubleToLongBits(health);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(inventory);
		result = prime * result + (isFlying ? 1231 : 1237);
		result = prime * result + level;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + Float.floatToIntBits(saturation);
		result = prime * result + ((scoreboard == null) ? 0 : scoreboard.hashCode());
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
		PlayerStateHolder other = (PlayerStateHolder) obj;
		if (activeEffects == null) {
			if (other.activeEffects != null)
				return false;
		} else if (!activeEffects.equals(other.activeEffects))
			return false;
		if (allowFlight != other.allowFlight)
			return false;
		if (cantSee == null) {
			if (other.cantSee != null)
				return false;
		} else if (!cantSee.equals(other.cantSee))
			return false;
		if (compassTarget == null) {
			if (other.compassTarget != null)
				return false;
		} else if (!compassTarget.equals(other.compassTarget))
			return false;
		if (Float.floatToIntBits(exhaustion) != Float.floatToIntBits(other.exhaustion))
			return false;
		if (Float.floatToIntBits(experience) != Float.floatToIntBits(other.experience))
			return false;
		if (Float.floatToIntBits(fallDistance) != Float.floatToIntBits(other.fallDistance))
			return false;
		if (fireTicks != other.fireTicks)
			return false;
		if (foodLevel != other.foodLevel)
			return false;
		if (gamemode != other.gamemode)
			return false;
		if (Double.doubleToLongBits(health) != Double.doubleToLongBits(other.health))
			return false;
		if (!Arrays.equals(inventory, other.inventory))
			return false;
		if (isFlying != other.isFlying)
			return false;
		if (level != other.level)
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (Float.floatToIntBits(saturation) != Float.floatToIntBits(other.saturation))
			return false;
		if (scoreboard == null) {
			if (other.scoreboard != null)
				return false;
		} else if (!scoreboard.equals(other.scoreboard))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlayerStateHolder [inventory=" + Arrays.toString(inventory) + ", gamemode=" + gamemode + ", health=" + health + ", foodLevel="
				+ foodLevel + ", level=" + level + ", experience=" + experience + ", allowFlight=" + allowFlight + ", isFlying=" + isFlying
				+ ", activeEffects=" + activeEffects + ", exhaustion=" + exhaustion + ", saturation=" + saturation + ", fallDistance=" + fallDistance
				+ ", fireTicks=" + fireTicks + ", cantSee=" + cantSee + ", scoreboard=" + scoreboard + ", compassTarget=" + compassTarget
				+ ", location=" + location + "]";
	}
	
}