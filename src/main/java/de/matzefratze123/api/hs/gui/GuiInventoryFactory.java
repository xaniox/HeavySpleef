package de.matzefratze123.api.hs.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class GuiInventoryFactory {

	static final int				MAX_SIZE	= 56;
	static final int				MIN_SIZE	= 9;

	private String					title;
	private int						size;
	private GuiInventorySlot[][]	slots;

	private GuiInventoryFactory() {
	}

	public static GuiInventoryFactory newBuilder() {
		return new GuiInventoryFactory();
	}

	public GuiInventoryFactory setTitle(String title) {
		this.title = title;

		return this;
	}

	public GuiInventoryFactory setSize(int lines) {
		this.size = lines * GuiInventory.SLOTS_PER_LINE;

		return this;
	}

	public GuiInventoryFactory setSlots(GuiInventorySlot[][] slots) {
		this.slots = slots;

		return this;
	}

	public Inventory build() {
		if (size > MAX_SIZE) {
			size = MAX_SIZE;
		}

		if (size < MIN_SIZE) {
			size = MIN_SIZE;
		}

		Inventory inventory;

		if (title == null) {
			inventory = Bukkit.createInventory(null, size);
		} else {
			inventory = Bukkit.createInventory(null, size, title);
		}

		for (int y = 0; y < slots.length; y++) {
			for (int x = 0; x < slots[y].length; x++) {
				GuiInventorySlot slot = slots[y][x];

				int mcSlot = GuiInventoryUtil.toMinecraftSlot(slot.getPoint());
				inventory.setItem(mcSlot, slot.getItem());
			}
		}

		return inventory;
	}

}
