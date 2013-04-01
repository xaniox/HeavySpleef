package me.matzefratze123.heavyspleef.database;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ItemStackHelper {

	private static String SEPERATOR = "-";
	
	public static String serialize(ItemStack stack) {
		int type = stack.getTypeId();
		int amount = stack.getAmount();
		byte data = stack.getData().getData();
		
		return type + SEPERATOR + amount + SEPERATOR + data;
	}
	
	public static ItemStack deserialize(String str) {
		String[] split = str.split(SEPERATOR);
		
		if (split.length < 3)
			return null;
		
		int type = 0;
		int amount = 1;
		byte data = 0;
		
		try {
			type = Integer.parseInt(split[0]);
			amount = Integer.parseInt(split[1]);
			data = Byte.parseByte(split[2]);
		} catch (NumberFormatException e) {
			HeavySpleef.instance.getLogger().warning("Could not read itemstack reward!");
		}
		
		ItemStack item = new ItemStack(type, amount);
		
		MaterialData mData = item.getData();
		mData.setData(data);
		
		item.setData(mData);
		
		return item;
	}
}
