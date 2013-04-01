package me.matzefratze123.heavyspleef.core.flag;

import me.matzefratze123.heavyspleef.core.Game;
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
		
		for (int i = 0; i < parts.length; i++)
			System.out.println(i + " " + parts[i]);
		
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

}
