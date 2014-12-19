package de.matzefratze123.heavyspleef.core.player;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class PlayerStateHolder {
	
	/* Saving the inventory and the armor contents */
	private static final int SIMPLE_INVENTORY_SIZE = 3 * 9;
	private static final int ARMOR_INVENTORY_SIZE = 4;
	private static final int INVENTORY_SIZE = SIMPLE_INVENTORY_SIZE + ARMOR_INVENTORY_SIZE; 
	
	private ItemStack[] inventory;
	private GameMode gamemode;
	private double health;
	private int foodLevel;
	private float experience;
	private boolean allowFlight;
	private boolean isFlying;
	private Collection<PotionEffect> activeEffects;
	private float exhaustion;
	private float saturation;
	private float fallDistance;
	private int fireTicks;
	private List<WeakReference<Player>> cantSee;
	
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
		System.arraycopy(armor, 0, inventoryArray, inventoryArray.length - 5, armor.length);
		
		/* Initialize the state with the current player state */
		stateHolder.setInventory(inventoryArray);
		stateHolder.setGamemode(player.getGameMode());
		stateHolder.setHealth(player.getHealth());
		stateHolder.setFoodLevel(player.getFoodLevel());
		stateHolder.setExperience(player.getExp());
		stateHolder.setAllowFlight(player.getAllowFlight());
		stateHolder.setFlying(player.isFlying());
		stateHolder.setActiveEffects(player.getActivePotionEffects());
		stateHolder.setExhaustion(player.getExhaustion());
		stateHolder.setSaturation(player.getSaturation());
		stateHolder.setFallDistance(player.getFallDistance());
		stateHolder.setFireTicks(player.getFireTicks());
		
		List<WeakReference<Player>> cantSee = Arrays.stream(Bukkit.getOnlinePlayers()).filter(p -> !player.canSee(p))
				.map(p -> new WeakReference<Player>(p)).collect(Collectors.toList());
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
		player.setExp(0);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.getActivePotionEffects().clear();
		player.setFallDistance(0);
		player.setFireTicks(0);
		
		Arrays.stream(Bukkit.getOnlinePlayers()).filter(p -> !player.canSee(p)).forEach(player::showPlayer);
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
		player.setExhaustion(experience);
		player.setAllowFlight(allowFlight);
		player.setFlying(isFlying);
		
		/* Remove current potion effects */
		player.getActivePotionEffects().clear();
		player.addPotionEffects(activeEffects);
		
		player.setExhaustion(exhaustion);
		player.setSaturation(saturation);
		player.setFallDistance(fallDistance);
		player.setFireTicks(fireTicks);
		
		if (teleport) {
			player.teleport(location);
		}
		
		cantSee.stream().filter(ref -> ref.get() != null).map(WeakReference::get).filter(Player::isOnline).forEach(player::hidePlayer);
	}
	
	public ItemStack[] getInventory() {
		return inventory;
	}

	public void setInventory(ItemStack[] inventory) {
		this.inventory = inventory;
	}

	public GameMode getGamemode() {
		return gamemode;
	}

	public void setGamemode(GameMode gamemode) {
		this.gamemode = gamemode;
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public List<WeakReference<Player>> getCantSee() {
		return cantSee;
	}
	
	public void setCantSee(List<WeakReference<Player>> cantSee) {
		this.cantSee = cantSee;
	}
	
}