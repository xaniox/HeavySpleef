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
package me.matzefratze123.heavyspleef.selection;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SelectionListener implements Listener {

	private SelectionManager selManager;
	private HeavySpleef plugin;
	public SelectionListener(HeavySpleef instance) {
		this.plugin = instance;
		this.selManager = plugin.getSelectionManager();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();
		ItemStack is = player.getItemInHand();
		
		if (block == null)
			return;
		if (!player.hasPermission(Permissions.SELECTION.getPerm()))
			return;
		if (is == null || is.getTypeId() != HeavySpleef.instance.getConfig().getInt("general.wandItem")) //TODO Add variable marker item (config)
			return;
		
		e.setCancelled(true);
		switch(e.getAction()) {
		case LEFT_CLICK_BLOCK:
			addFirstSelection(block.getLocation(), player);
			break;
		case RIGHT_CLICK_BLOCK:
			addSecondSelection(block.getLocation(), player);
			break;
		default:
			break;
		}
	}
	
	public void addFirstSelection(Location loc, Player player) {
		if (!selManager.hasSelection(player)) {
			selManager.addSelection(player, new Location[] {loc, null});
		} else {
			selManager.setFirstSelection(player, loc);
		}
		player.sendMessage(ChatColor.DARK_BLUE + "First point set (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
	}
	
	public void addSecondSelection(Location loc, Player player) {
		if (!selManager.hasSelection(player)) {
			selManager.addSelection(player, new Location[] {null, loc});
		} else {
			selManager.setSecondSelection(player, loc);
		}
		player.sendMessage(ChatColor.DARK_BLUE + "Second point set (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
	}
	
}
