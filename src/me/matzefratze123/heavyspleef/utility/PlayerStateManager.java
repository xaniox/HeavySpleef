package me.matzefratze123.heavyspleef.utility;

import java.util.HashMap;
import java.util.Map;

import me.matzefratze123.heavyspleef.core.Game;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerStateManager {

	public static Map<String, PlayerState> states = new HashMap<String, PlayerState>();
	
	@SuppressWarnings("deprecation")
	public static void savePlayerState(Player p) {
		states.put(p.getName(), new PlayerState(p.getInventory().getContents(), p.getInventory().getHelmet(), p.getInventory().getChestplate(),
				  p.getInventory().getLeggings(), p.getInventory().getBoots(),p.getExhaustion(), p.getSaturation(),
				  p.getFoodLevel(), p.getHealth(),p.getGameMode(), p.getActivePotionEffects(), p.getExp(), p.getLevel()));

		clearInventorySavely(p.getInventory());
		p.getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
		p.setLevel(0);
		p.setExp(0);
		p.setGameMode(GameMode.SURVIVAL);
		p.setFireTicks(0);
		p.setHealth(20);
		p.setFoodLevel(20);
		
		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		
		p.updateInventory();
	}
	
	@SuppressWarnings("deprecation")
	public static void restorePlayerState(Player p) {
		PlayerState state = states.get(p.getName());
		if (state == null) {
			p.sendMessage(Game._("errorOnState"));
			return;
		}
		
		p.getInventory().clear(Material.DIAMOND_SPADE.getId(), (byte)0);
		p.getInventory().setContents(state.getContents());
		p.getInventory().setHelmet(state.getHelmet());
		p.getInventory().setChestplate(state.getChestplate());
		p.getInventory().setLeggings(state.getLeggings());
		p.getInventory().setBoots(state.getBoots());
		p.setLevel(state.getLevel());
		p.setExp(state.getExp());
		p.setGameMode(state.getGm());
		p.setHealth(state.getHealth());
		p.setFoodLevel(state.getFoodLevel());
		p.addPotionEffects(state.getPotioneffects());
		p.setExhaustion(state.getExhaustion());
		p.setSaturation(state.getSaturation());
		p.sendMessage(Game._("stateRestored"));
		p.updateInventory();
		states.remove(p.getName());
	}
	
	public static Map<String, PlayerState> getPlayerStates() {
		return states;
	}
	
	public static void clearInventorySavely(Inventory inv) {
		//Call clear() method two times because of mistakes...
		inv.clear();
		inv.clear();
	}

}
