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

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiInventorySlot {

	private ItemStack			item;
	private Object				value;
	private GuiInventoryPoint	point;
	private GuiInventory		inventory;

	public GuiInventorySlot(GuiInventory inventory, int x, int y) {
		this.inventory = inventory;
		this.point = new GuiInventoryPoint(x, y);
	}

	public GuiInventorySlot(GuiInventory inventory, int x, int y, ItemStack item) {
		this(inventory, x, y);

		this.item = item;
	}

	public GuiInventorySlot(GuiInventory inventory, int x, int y, ItemStack item, Object value) {
		this(inventory, x, y, item);

		this.value = value;
	}

	public void setItem(ItemStack item) {
		this.item = item;

		inventory.refreshOpenInventories();
	}

	public void setItem(Material material) {
		this.item = new ItemStack(material);

		inventory.refreshOpenInventories();
	}

	public void setItem(Material material, int amount) {
		this.item = new ItemStack(material, amount);

		inventory.refreshOpenInventories();
	}

	public void setItem(Material material, int amount, byte data) {
		this.item = new ItemStack(material, amount, data);

		inventory.refreshOpenInventories();
	}

	public void setItem(Material material, int amount, byte data, String name) {
		ItemStack stack = new ItemStack(material, amount, data);

		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);

		this.item = stack;

		inventory.refreshOpenInventories();
	}

	public void setItem(Material material, int amount, byte data, String name, String... lore) {
		ItemStack stack = new ItemStack(material, amount, data);
		List<String> loreList = Arrays.asList(lore);

		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(loreList);
		stack.setItemMeta(meta);

		this.item = stack;

		inventory.refreshOpenInventories();

	}

	public void setItem(Material material, byte data) {
		this.item = new ItemStack(material, 1, data);

		inventory.refreshOpenInventories();
	}

	public ItemStack getItem() {
		return item;
	}

	public GuiInventoryPoint getPoint() {
		return point;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
