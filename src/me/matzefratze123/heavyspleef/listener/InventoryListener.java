package me.matzefratze123.heavyspleef.listener;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.matzefratze123.heavyspleef.utility.InventorySelector.ClickEvent;
import me.matzefratze123.heavyspleef.utility.InventorySelector.InventorySelectorListener;

public class InventoryListener implements InventorySelectorListener {

	@Override
	public void onClick(ClickEvent e) {
		Player player = e.getPlayer();
		ItemStack stack = e.getItemStack();
		
		if (stack == null)
			return;
		
		ItemMeta meta = stack.getItemMeta();
		String displayName = meta.getDisplayName();
		
		displayName = displayName.substring(9);
		player.performCommand("hs join " + displayName);
		e.getSelector().close(player);
	}

}
