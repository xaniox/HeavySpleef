package de.matzefratze123.heavyspleef.core.flag;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.database.ItemStackHelper;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Util;

public class SingleItemStackFlag extends Flag<ItemStack> {

	public SingleItemStackFlag(String name, ItemStack defaulte) {
		super(name, defaulte);
	}

	@Override
	public String serialize(Object object) {
		ItemStack stack = (ItemStack)object;
		
		return getName() + ":" + ItemStackHelper.serialize(stack);
	}

	@Override
	public ItemStack deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2)
			return null;
		
		this.name = parts[0];
		return ItemStackHelper.deserialize(parts[1]);
	}

	@Override
	public ItemStack parse(Player player, String input) {
		String parts[] = input.split(" ");
		
		if (parts.length <= 0)
			return null;
		
		int amount = 1;
		SimpleBlockData data = Util.getMaterialFromString(parts[0], false);
		
		if (parts.length > 1) {
			try {
				amount = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
		ItemStack stack = new ItemStack(data.getMaterial(), amount);
		MaterialData stackData = stack.getData();
		stackData.setData(data.getData());
		stack.setData(stackData);
		
		return stack.getData().toItemStack(amount);
	}

	@Override
	public String toInfo(Object value) {
		ItemStack stack = (ItemStack) value;
		
		return getName() + ":" + stack.getAmount() + " " + Util.toFriendlyString(stack.getType().name());
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + ChatColor.RED + " /spleef flag <name> " + getName() + " <id:data> [amount]";
	}

	@Override
	public FlagType getType() {
		return FlagType.SINGLE_ITEMsTACK_FLAG;
	}

}
