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
package de.matzefratze123.heavyspleef.util;

import java.util.HashMap;
import java.util.Map;


import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameCuboid;

public class PlayerStateManager {

	public static Map<String, PlayerState> states = new HashMap<String, PlayerState>();
	
	@SuppressWarnings("deprecation")
	public static void savePlayerState(Player p) {
		if (!HeavySpleef.getSystemConfig().getBoolean("general.savePlayerState", true)) {
			p.setGameMode(GameMode.SURVIVAL);//Set to survival
			p.setFoodLevel(20);
			p.setHealth(20);
			p.setAllowFlight(false);//Disable fly mode (Essentials etc.)
			p.setFireTicks(0);
			
			return;
		}
		
		states.put(p.getName(), new PlayerState(p.getInventory().getContents(), p.getInventory().getHelmet(), p.getInventory().getChestplate(),
				  p.getInventory().getLeggings(), p.getInventory().getBoots(),p.getExhaustion(), p.getSaturation(),
				  p.getFoodLevel(), p.getHealth(),p.getGameMode(), p.getActivePotionEffects(), p.getExp(), p.getLevel(), p.getAllowFlight()));

		p.setGameMode(GameMode.SURVIVAL);//Set to survival
		p.setFoodLevel(20);
		p.setHealth(20);
		p.setAllowFlight(false);//Disable fly mode (Essentials etc.)
		p.setFireTicks(0);
		p.getInventory().clear();
		p.getInventory().setArmorContents(new ItemStack[4]);
		p.setLevel(0);
		p.setExp(0);
		
		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		
		p.sendMessage(Game._("stateSaved"));
		p.updateInventory();
	}
	
	@SuppressWarnings("deprecation")
	public static void restorePlayerState(Player p) {
		if (p.isDead())
			return;
		p.setFireTicks(0);//We don't want that the player is burning because of lava or something similar...
		
		//Remove the haste effects (difficulty)
		for (PotionEffect effect : p.getActivePotionEffects())
			p.removePotionEffect(effect.getType());
		p.getInventory().remove(Material.SHEARS);
		p.getInventory().remove(Material.DIAMOND_SPADE);
		
		if (!HeavySpleef.getSystemConfig().getBoolean("general.savePlayerState", true))
			return;
		
		PlayerState state = states.get(p.getName());
		if (state == null) {
			p.sendMessage(GameCuboid._("errorOnState"));
			return;
		}
		
		p.getInventory().clear();
		p.getInventory().setContents(state.getContents());
		p.getInventory().setHelmet(state.getHelmet());
		p.getInventory().setChestplate(state.getChestplate());
		p.getInventory().setLeggings(state.getLeggings());
		p.getInventory().setBoots(state.getBoots());
		p.setGameMode(state.getGm());
		p.setAllowFlight(state.isFly());
		p.setLevel(state.getLevel());
		p.setExp(state.getExp());
		
		if (p.getGameMode() != GameMode.CREATIVE)
			p.setHealth(state.getHealth());
		
		p.setFoodLevel(state.getFoodLevel());
		p.addPotionEffects(state.getPotioneffects());
		p.setExhaustion(state.getExhaustion());
		p.setSaturation(state.getSaturation());
		p.sendMessage(GameCuboid._("stateRestored"));
		p.updateInventory();
		states.remove(p.getName());
	}
	
	public static Map<String, PlayerState> getPlayerStates() {
		return states;
	}

}
