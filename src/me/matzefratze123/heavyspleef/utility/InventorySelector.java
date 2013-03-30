package me.matzefratze123.heavyspleef.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class InventorySelector implements Listener {
	
	private Inventory inv;
	private String inventoryName;
	private int size;
	private static List<InventorySelectorListener> listeners = new ArrayList<InventorySelectorListener>();
	
	public InventorySelector(Plugin plugin, String inventoryName, int size) {
		this.inventoryName = inventoryName;
		
		this.size = roundToSlot(size);
		this.inv = Bukkit.createInventory(null, this.size, inventoryName);
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public String getInventoryName() {
		return this.inventoryName;
	}
	
	public int size() {
		return this.size;
	}
	
	public void setSize(int size) {
		this.size = roundToSlot(size);
		this.inv = copy(size, null);
	}
	
	public void setTitle(String title) {
		this.inventoryName = title;
		this.inv = copy(size, title);
	}
	
	public void addItemStack(ItemStack stack, int slot) {
		if (slot > size)
			slot = size;
		
		inv.setItem(slot, stack);
	}
	
	public void removeItemStack(int slot) {
		if (slot > size)
			slot = size;
		
		inv.setItem(slot, null);
	}
	
	public int getSlot(ItemStack stack) {
		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i).equals(stack))
				return i;
		}
		
		return -1;
	}
	
	public void clear() {
		inv.clear();
	}
	
	public void open(Player player) {
		player.openInventory(inv);
	}
	
	public boolean close(Player player) {
		InventoryView view = player.getOpenInventory();
		Inventory inventory = view.getTopInventory();
		
		if (inventory.getTitle().equalsIgnoreCase(inventoryName)) {
			player.closeInventory();
			return true;
		}
		
		return false;
	}
	
	private Inventory copy(int newSize, String newTitle) {
		if (newTitle == null)
			newTitle = inventoryName;
		Inventory newInv = Bukkit.createInventory(null, roundToSlot(newSize), newTitle);
		newInv.setContents(inv.getContents());
		
		return newInv;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!e.getInventory().getTitle().equalsIgnoreCase(inventoryName))
			return;
		if (e.getSlotType() != SlotType.CONTAINER)
			return;
		if (e.getSlot() >= inv.getSize())
			return;
		
		for (InventorySelectorListener listener : listeners) {
			ClickEvent event = new ClickEvent(e.getSlot(), e.getInventory().getItem(e.getSlot()), this, (Player)e.getWhoClicked());
			listener.onClick(event);
			
			if (event.isCancelled())
				e.setCancelled(true);
		}
	}
	
	public static void registerListener(InventorySelectorListener listener) {
		listeners.add(listener);
	}
	
	public static int roundToSlot(int i) {
		double rows = Math.ceil((double)i / 9.0D);
		i = (int) (rows * 9);
		
		if (i > 54)
			i = 54;
		
		return i;
	}
	
    public static interface InventorySelectorListener {
		
		public void onClick(ClickEvent event);
		
	}
	
	public static class ClickEvent implements Cancellable {
		
		private int slot;
		private ItemStack stack;
		private InventorySelector selector;
		private Player player;
		private boolean cancelled = false;
		
		public ClickEvent(int slot, ItemStack stack, InventorySelector selector, Player player) {
			this.slot = slot;
			this.stack = stack;
			this.selector = selector;
			this.player = player;
		}
		
		public int getSlot() {
			return this.slot;
		}
		
		public ItemStack getItemStack() {
			return this.stack;
		}
		
		public InventorySelector getSelector() {
			return this.selector;
		}
		
		public Player getPlayer() {
			return this.player;
		}

		@Override
		public boolean isCancelled() {
			return this.cancelled;
		}

		@Override
		public void setCancelled(boolean cancel) {
			this.cancelled = cancel;
		}
		
	}
	
}
