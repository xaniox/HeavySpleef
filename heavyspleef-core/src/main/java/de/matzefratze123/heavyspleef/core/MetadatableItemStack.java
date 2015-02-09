package de.matzefratze123.heavyspleef.core;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

public class MetadatableItemStack extends ItemStack {

	private static final String HIDDEN_PREFIX = "Hidden:";
	private static final String KEY_VALUE_DELIMITER = "\0";
	
	public MetadatableItemStack(ItemStack stack) {
		super(stack);
	}

	public MetadatableItemStack(Material type, int amount, short damage) {
		super(type, amount, damage);
	}

	public MetadatableItemStack(Material type, int amount) {
		super(type, amount);
	}

	public MetadatableItemStack(Material type) {
		super(type);
	}

	public void setMetadata(String key, String value) {
		ItemMeta meta = getItemMeta();
		List<String> lore;
		
		if (meta.hasLore()) {
			lore = meta.getLore();
		} else {
			lore = Lists.newArrayList();
		}
		
		String loreString = HIDDEN_PREFIX + key + (value != null ? KEY_VALUE_DELIMITER + value : "");
		loreString = hideString(loreString);
		lore.add(loreString);
		
		meta.setLore(lore);
		setItemMeta(meta);
	}
	
	public String getMetadata(String key) {
		if (!hasItemMeta()) {
			throw new IllegalStateException("ItemStack does not have any metadata");
		}
		
		ItemMeta meta = getItemMeta();
		if (!meta.hasLore()) {
			throw new IllegalStateException("ItemStack's metadata does not have a lore");
		}
		
		List<String> lore = meta.getLore();
		for (String loreString : lore) {
			loreString = unhideString(loreString);
			
			if (!loreString.startsWith(HIDDEN_PREFIX)) {
				continue;
			}
			
			String[] keyValuePair = loreString.substring(HIDDEN_PREFIX.length()).split(KEY_VALUE_DELIMITER);
			String loreKey = keyValuePair[0];
			
			if (!loreKey.equals(key)) {
				continue;
			}
			
			return keyValuePair.length > 1 ? keyValuePair[1] : "";
		}
		
		return null;
	}
	
	public boolean hasMetadata(String key) {
		return getMetadata(key) != null;
	}
	
	private String hideString(String base) {
		StringBuilder hidden = new StringBuilder();
		
		for (char c : base.toCharArray()) {
			hidden.append(ChatColor.COLOR_CHAR)
				.append(c);
		}
		
		return hidden.toString();
	}
	
	private String unhideString(String hidden) {
		return hidden.replace("§", " ");
	}
	
}
