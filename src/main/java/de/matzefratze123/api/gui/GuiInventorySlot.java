package de.matzefratze123.api.gui;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiInventorySlot {

	private ItemStack item;
	private Object value;
	private int x, y;
	
	public GuiInventorySlot(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public GuiInventorySlot(int x, int y, ItemStack item) {
		this(x, y);
		
		this.item = item;
	}
	
	public GuiInventorySlot(int x, int y, ItemStack item, Object value) {
		this(x, y, item);
		
		this.value = value;
	}
	
	public void setItem(ItemStack item) {
		this.item = item;
	}
	
	public void setItem(Material material) {
		this.item = new ItemStack(material);
	}
	
	public void setItem(Material material, int amount) {
		this.item = new ItemStack(material, amount);
	}
	
	public void setItem(Material material, int amount, byte data) {
		this.item = new ItemStack(material, amount, data);
	}
	
	public void setItem(Material material, int amount, byte data, String name) {
		ItemStack stack = new ItemStack(material, amount, data);
		
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);
		
		this.item = stack;
	}
	
	public void setItem(Material material, int amount, byte data, String name, String... lore) {
		ItemStack stack = new ItemStack(material, amount, data);
		List<String> loreList = Arrays.asList(lore);
		
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(loreList);
		stack.setItemMeta(meta);
		
		this.item = stack;
	}
	
	public void setItem(Material material, byte data) {
		this.item = new ItemStack(material, 1, data);
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
