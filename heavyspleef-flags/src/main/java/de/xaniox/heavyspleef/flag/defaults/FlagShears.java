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
package de.xaniox.heavyspleef.flag.defaults;

import de.xaniox.heavyspleef.core.event.GameStartEvent;
import de.xaniox.heavyspleef.core.event.PlayerBlockBreakEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameProperty;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

@Flag(name = "shears", hasGameProperties = true)
public class FlagShears extends BaseFlag {

	private static final String SHEARS_DISPLAY_NAME = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Spleef Shears";
	
	private static ItemStack createShears() {
		ItemStack shearsStack = new ItemStack(Material.SHEARS);
		
		ItemMeta meta = shearsStack.getItemMeta();
		meta.setDisplayName(SHEARS_DISPLAY_NAME);
		
		shearsStack.setItemMeta(meta);
		return shearsStack;
	}
	
	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {
		properties.put(GameProperty.INSTANT_BREAK, false);
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the use of shears in spleef");
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		Game game = event.getGame();
		
		for (SpleefPlayer player : game.getPlayers()) {
			Player bukkitPlayer = player.getBukkitPlayer();
			Inventory inv = bukkitPlayer.getInventory();
			ItemStack stack = createShears();
			
			inv.addItem(stack);
			
			bukkitPlayer.updateInventory();
		}
	}
	
	@Subscribe
	public void onPlayerBreakBlock(PlayerBlockBreakEvent event) {
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			ItemStack stack = player.getBukkitPlayer().getItemInHand();
			if (stack.getType() != Material.SHEARS) {
				continue;
			}
			
			stack.setDurability((short)0);
			player.getBukkitPlayer().setItemInHand(stack);
		}
	}

}