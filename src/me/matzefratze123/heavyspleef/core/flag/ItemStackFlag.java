package me.matzefratze123.heavyspleef.core.flag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.database.ItemStackHelper;
import me.matzefratze123.heavyspleef.utility.MaterialHelper;
import me.matzefratze123.heavyspleef.utility.SimpleBlockData;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ItemStackFlag extends Flag<ItemStack[]> {

	public ItemStackFlag(String name) {
		super(name);
	}

	@Override
	public ItemStack[] parse(Player player, String input) {
		String[] parts = input.split(" ");
		ItemStack[] stacks = new ItemStack[parts.length / 2];
		
		int count = 0;
		for (int i = 0; i + 1 < parts.length; i += 2) {
			SimpleBlockData datas = MaterialHelper.fromString(parts[i], false);
			int amount = 0;
			
			try {
				amount = Integer.parseInt(parts[i + 1]);
			} catch (NumberFormatException e) {
				player.sendMessage(Game._("notANumber", parts[i + 1]));
				return null;
			}
			if (datas == null)
				continue;
			
			ItemStack stack = new ItemStack(datas.getMaterial(), amount);
			MaterialData data = stack.getData();
			data.setData(datas.getData());
			stack.setData(data);
			stacks[count] = stack;
			count++;
		}
		
		return stacks;
	}

	@Override
	public String getHelp() {
		return "/spleef flag <name> " + getName() + " <id:data>";
	}

	@Override
	public String serialize(Object value) {
		ItemStack[] i = (ItemStack[])value;
		
		Set<String> stacks = new HashSet<String>();
		
		for (ItemStack stack : i) {
			String serialized = ItemStackHelper.serialize(stack);
			stacks.add(serialized);
		}
		
		String toString = stacks.toString();
		
		toString = toString.replace("[", "");
		toString = toString.replace("]", "");
		toString = toString.replace(",", "~");
		
		return getName() + ":" + toString;
	}

	@Override
	public ItemStack[] deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2)
			return null;
		
		this.name = parts[0];
		String value = parts[1];
		
		String[] stacks = value.split("~");
		ItemStack[] array = new ItemStack[stacks.length];
		
		for (int i = 0; i < stacks.length; i++) {
			ItemStack is = ItemStackHelper.deserialize(stacks[i]);
			array[i] = is;
		}
		
		return array;
	}

	@Override
	public String toInfo(Object value) {
		List<String> list = new ArrayList<String>();
		ItemStack[] stacks = (ItemStack[])value;
		
		for (ItemStack stack : stacks) {
			list.add(stack.getAmount() + " " + MaterialHelper.getName(stack.getType().name()));
		}
		
		Set<String> asSet = new HashSet<String>(list);
		return asSet.toString();
	}

}
