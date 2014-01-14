package de.matzefratze123.api.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

/**
 * This class represents a clickable inventory. Events are triggered if someone
 * clicks the inventory. By default you should extend this class and overwrite
 * the following method:
 * 
 * <pre>
 * &#064;Override
 * public void onClick(Player player, GuiInventorySlot slot) {
 * 	// Your code here
 * }
 * </pre>
 * 
 * @author matzefratze123
 */
public abstract class GuiInventory implements Listener {

	public static final int			SLOTS_PER_LINE		= 9;
	public static final int			MAX_TITLE_LENGTH	= 32;

	private Plugin					plugin;

	private GuiInventorySlot[][]	slots;
	private String					title;
	private int						lines;

	private List<GuiInventoryView>	views;

	public GuiInventory(Plugin plugin, int lines) {
		this.plugin = plugin;
		this.lines = lines;
		this.views = new ArrayList<GuiInventoryView>();

		slots = new GuiInventorySlot[lines][];
		for (int y = 0; y < lines; y++) {
			slots[y] = new GuiInventorySlot[SLOTS_PER_LINE];

			for (int x = 0; x < SLOTS_PER_LINE; x++) {
				slots[y][x] = new GuiInventorySlot(this, x, y);
			}
		}
	}

	public GuiInventory(Plugin plugin) {
		this(plugin, 1);
	}

	public GuiInventory(Plugin plugin, int lines, String title) {
		this(plugin, lines);

		this.title = title;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public String getTitle() {
		return title;
	}

	public void setLines(int lines) {
		this.lines = lines;

		// Copy old values and put them into the new array if possible
		GuiInventorySlot[][] oldSlots = slots.clone();
		slots = new GuiInventorySlot[lines][SLOTS_PER_LINE];

		for (int y = 0; y < slots.length; y++) {
			for (int x = 0; x < slots[y].length; x++) {
				if (y < oldSlots.length && x < oldSlots[y].length) {
					slots[y][x] = oldSlots[y][x];
				} else {
					slots[y][x] = new GuiInventorySlot(this, x, y);
				}
			}
		}
	}

	public int getLines() {
		return lines;
	}

	public List<GuiInventoryView> getViews() {
		return views;
	}

	public void setTitle(String title) {
		Validate.notNull(title, "title cannot be null");
		
		if (title.length() > 32) {
			title = title.substring(0, 32);
		}

		this.title = title;
	}

	public GuiInventorySlot getSlot(int x, int y) {
		Validate.isTrue(y < slots.length && y >= 0, "y out of bounds: " + y);
		Validate.isTrue(x <= 9 && x >= 0, "x out of bounds: " + x);
		
		return slots[y][x];
	}

	public GuiInventorySlot getSlotByValue(Object value) {
		for (int y = 0; y < slots.length; y++) {
			for (int x = 0; x < slots[y].length; x++) {
				GuiInventorySlot slot = slots[y][x];

				if (slot.getValue() == value) {
					return slot;
				}
			}
		}

		return null;
	}

	public void open(Player player) {
		// Build inventory
		Inventory inventory = GuiInventoryFactory.newBuilder().setTitle(title).setSize(lines).setSlots(slots).build();

		player.openInventory(inventory);

		if (views.isEmpty()) {
			Bukkit.getPluginManager().registerEvents(this, plugin);
		}

		Iterator<GuiInventoryView> iterator = views.iterator();

		while (iterator.hasNext()) {
			GuiInventoryView view = iterator.next();
			if (view.getPlayer() == player) {
				iterator.remove();
				break;
			}
		}

		views.add(new GuiInventoryView(inventory, player));
	}

	public void close(final Player player) {
		final GuiInventoryView view = getView(player);

		if (view == null) {
			return;
		}

		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				player.closeInventory();
				views.remove(view);

				if (views.isEmpty()) {
					HandlerList.unregisterAll(GuiInventory.this);
				}
			}
		});
	}

	public GuiInventoryView getView(Player player) {
		for (GuiInventoryView view : views) {
			if (view.getPlayer() == player) {
				return view;
			}
		}

		return null;
	}
	
	@SuppressWarnings("deprecation")
	protected void refreshOpenInventories() {
		for (GuiInventoryView view : views) {
			Player player = view.getPlayer();
			Inventory inv = view.getInventory();
			
			for (int y = 0; y < slots.length; y++) {
				for (int x = 0; x < slots[y].length; x++) {
					GuiInventorySlot slot = slots[y][x];
					
					int vanillaSlot = GuiInventoryUtil.toMinecraftSlot(slot.getPoint());
					
					inv.setItem(vanillaSlot, slot.getItem());
				}
			}
			
			player.updateInventory();
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();

		close(player);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		SlotType type = e.getSlotType();
		int slot = e.getSlot();

		GuiInventoryView view = getView(player);

		if (view == null) {
			return;
		}

		if (type != SlotType.CONTAINER) {
			return;
		}
		if (slot < 0 || slot >= lines * SLOTS_PER_LINE) {
			return;
		}

		GuiInventoryPoint point = GuiInventoryUtil.toGuiSlot(slot);
		GuiInventorySlot guiSlot = slots[point.getY()][point.getX()];
		ClickType clickType = ClickType.byEvent(e);

		GuiClickEvent event = new GuiClickEvent(player, guiSlot, clickType);

		try {
			onClick(event);
		} catch (Throwable t) {
			plugin.getLogger().severe("Exception occured while dispatching click event onClick by plugin " + plugin.getName() + ": " + t.getMessage());
			t.printStackTrace();
		}

		if (event.isCancelled()) {
			e.setCancelled(true);
		}
	}

	public abstract void onClick(GuiClickEvent event);

	public static class GuiInventoryView {

		private Inventory	inventory;
		private Player		player;

		public GuiInventoryView(Inventory inventory, Player player) {
			this.inventory = inventory;
			this.player = player;
		}

		public Inventory getInventory() {
			return inventory;
		}

		public Player getPlayer() {
			return player;
		}

	}

	public static class GuiClickEvent {

		private Player				player;
		private GuiInventorySlot	slot;
		private ClickType			clickType;

		private boolean				isCancelled;

		public GuiClickEvent(Player player, GuiInventorySlot slot, ClickType clickType) {
			this.player = player;
			this.slot = slot;
			this.clickType = clickType;
		}

		public Player getPlayer() {
			return player;
		}

		public GuiInventorySlot getSlot() {
			return slot;
		}

		public ClickType getClickType() {
			return clickType;
		}

		public boolean isCancelled() {
			return isCancelled;
		}

		public void setCancelled(boolean cancel) {
			this.isCancelled = cancel;
		}

	}

}
