/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
