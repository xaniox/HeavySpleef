package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.flag.NullFlag;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.ItemStackFlag;
import de.matzefratze123.heavyspleef.flag.presets.ItemStackListFlag;

@Flag(name = "itemreward", ignoreParseException = true)
public class FlagItemReward extends ItemStackListFlag {

	public FlagItemReward() {
		List<ItemStack> list = Lists.newArrayList();
		setValue(list);
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines an item reward for spleef winners");
	}
	
	@Override
	public List<ItemStack> parseInput(Player player, String input) throws InputParseException {
		throw new InputParseException("Use itemreward:add for adding an item reward and itemreward:remove for removing the recent added item reward");
	}
	
	@Subscribe
	public void onPlayerWinGame(PlayerWinGameEvent event) {
		SpleefPlayer[] winners = event.getWinners();
		
		for (SpleefPlayer player : winners) {
			Player bukkitPlayer = player.getBukkitPlayer();
			World world = bukkitPlayer.getWorld();
			
			Inventory inventory = bukkitPlayer.getInventory();
			boolean invFull = false;
			
			for (ItemStack reward : getValue()) {
				if (invFull) {
					world.dropItem(bukkitPlayer.getLocation(), reward);
				} else {
					if (isInventoryFull(inventory)) {
						invFull = true;
						world.dropItem(bukkitPlayer.getLocation(), reward);
						bukkitPlayer.sendMessage(getI18N().getString(Messages.Player.ITEMREWARD_ITEMS_DROPPED));
					} else {
						inventory.addItem(reward);
					}
				}
			}
		}
	}
	
	private static boolean isInventoryFull(Inventory inventory) {
		ItemStack[] content = inventory.getContents();
		
		for (ItemStack stack : content) {
			if (stack != null && stack.getType() != Material.AIR) {
				continue;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Flag(name = "add", parent = FlagItemReward.class)
	public static class FlagAddItemReward extends ItemStackFlag {
		
		@Override
		public void setValue(ItemStack value) {
			FlagItemReward parent = (FlagItemReward) getParent();
			parent.add(value);
		}

		@Override
		public void getDescription(List<String> description) {
			description.add("Adds an item reward to the list of item rewards");
		}
		
	}
	
	@Flag(name = "remove", parent = FlagItemReward.class)
	public static class FlagRemoveItemReward extends NullFlag {
		
		@Override
		public void setValue(Void value) {
			FlagItemReward parent = (FlagItemReward) getParent();
			int lastIndex = parent.size() - 1;
			
			parent.remove(lastIndex);
		}
		
		@Override
		public void getDescription(List<String> description) {
			description.add("Removes the recent added itemreward");
		}
		
	}

}
