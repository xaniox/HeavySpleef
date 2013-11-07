/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.util.Util;

public class JoinGUI implements Listener {
	
	private static final String infinity = "\u221E";
	private boolean unregistered;
	private String title;
	
	private Set<String> viewing = new HashSet<String>();
	
	public JoinGUI(String title, Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		this.title = title;
	}
	
	public void open(final Player player) {
		validateState();
		
		int size = calculateSize(GameManager.getGames().size());
		List<Game> games = GameManager.getGames();
		Collections.sort(games, new GameSorter());
		
		final Inventory inv = Bukkit.createInventory(null, size, title);
		
		for (Game game : games) {
			ItemStack icon = game.getFlag(FlagType.ICON);
			icon = icon.getData().toItemStack(icon.getAmount());
			
			if (icon == null)
				icon = new ItemStack(Material.DIAMOND_SPADE);
			
			ItemMeta meta = icon.getItemMeta();
			ChatColor color = game.getGameState() == GameState.JOINABLE || game.getGameState() == GameState.LOBBY ? ChatColor.GREEN : ChatColor.RED;
			
			meta.setDisplayName(color + "Join " + game.getName());
			List<String> lore = new ArrayList<String>();
			
			String maxPlayers = String.valueOf(game.getFlag(FlagType.MAXPLAYERS) < 2 ? infinity : game.getFlag(FlagType.MAXPLAYERS));
			
			lore.add(color + "" + game.getIngamePlayers().size() + ChatColor.DARK_GRAY + ChatColor.BOLD + " / " + ChatColor.RED + maxPlayers);
			lore.add(ChatColor.AQUA + Util.formatMaterialName(game.getGameState().name()));
			
			meta.setLore(lore);
			icon.setItemMeta(meta);
			
			inv.addItem(icon);
		}
		
		player.closeInventory();
		Bukkit.getScheduler().runTask(HeavySpleef.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				player.openInventory(inv);
				viewing.add(player.getName());
			}
		});
	}
	
	public void unregister() {
		HandlerList.unregisterAll(this);
		title = null;
		unregistered = true;
	}
	
	private void validateState() {
		if (unregistered)
			throw new IllegalStateException("Cannot perform inventory menu options while unregistered!");
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();
		if (viewing.contains(player.getName()))
			viewing.remove(player.getName());
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();
		
		if (!inv.getTitle().equalsIgnoreCase(title))
			return;
		if (!viewing.contains(player.getName()))
			return;
		
		e.setCancelled(true);
		if (e.getSlotType() != SlotType.CONTAINER)
			return;
		int slot = e.getSlot();
		if (slot >= inv.getSize()) //Prevent ArrayIndexOutOfBoundsExceptions...
			return;
		
		ItemStack item = inv.getItem(slot);
		if (item == null) //No NPE's
			return;
		
		ItemMeta meta = item.getItemMeta();
		if (meta.getDisplayName() == null)
			return;
		
		String displayName = ChatColor.stripColor(meta.getDisplayName());
		if (displayName.length() < 5)
			return;
		
		String gameName = displayName.substring(5);
		player.closeInventory();
		
		player.performCommand("spleef join " + gameName);
	}
	
	static int calculateSize(int base) {
		base = Math.abs(base);
		
		while (base % 9 != 0)
			base++;
		
		if (base > 54)
			base = 54;
		if (base <= 0)
			base = 9;
		
		return base;
	}
	
	private class GameSorter implements Comparator<Game> {

		@Override
		public int compare(Game o1, Game o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	}
	
}
