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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.GamePropertyPriority;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = "shovels")
public class FlagShovels extends BooleanFlag {
	
	private static final String SHOVEL_DISPLAYNAME = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Spleef Shovel";
	
	private static ItemStack createShovel() {
		ItemStack shovelStack = new ItemStack(Material.DIAMOND_SPADE);
		
		ItemMeta meta = shovelStack.getItemMeta();
		meta.setDisplayName(SHOVEL_DISPLAYNAME);
		
		shovelStack.setItemMeta(meta);
		return shovelStack;
	}
	
	@GamePropertyPriority
	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {
		properties.put(GameProperty.INSTANT_BREAK, false);
	}
	
	@Override
	public boolean hasGameProperties() {
		return true;
	}
	
	@Override
	public boolean hasBukkitListenerMethods() {
		return false;
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the use of shovels in spleef");
	}
	
	@SuppressWarnings("deprecation")
	@GameListener
	public void onGameStart(GameStartEvent event) {
		Game game = event.getGame();
		
		for (SpleefPlayer player : game.getPlayers()) {
			Player bukkitPlayer = player.getBukkitPlayer();
			Inventory inv = bukkitPlayer.getInventory();
			ItemStack stack = createShovel();
			
			inv.addItem(stack);
			
			bukkitPlayer.updateInventory();
		}
	}
	
}
