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
package me.matzefratze123.heavyspleef.listener;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.matzefratze123.heavyspleef.util.InventorySelector.ClickEvent;
import me.matzefratze123.heavyspleef.util.InventorySelector.InventorySelectorListener;

public class InventoryListener implements InventorySelectorListener {

	@Override
	public void onClick(ClickEvent e) {
		Player player = e.getPlayer();
		ItemStack stack = e.getItemStack();
		
		if (stack == null)
			return;
		
		ItemMeta meta = stack.getItemMeta();
		String displayName = meta.getDisplayName();
		
		displayName = displayName.substring(9);
		player.performCommand("hs join " + displayName);
		e.getSelector().close(player);
	}

}
