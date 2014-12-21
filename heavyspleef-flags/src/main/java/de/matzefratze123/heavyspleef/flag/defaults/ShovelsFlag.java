package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.GamePropertyPriority;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = "shovels")
public abstract class ShovelsFlag extends BooleanFlag {
	
	private static final String SHOVEL_DISPLAYNAME = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Spleef Shovel";
	private static final ItemStack shovelStack;
	
	static {
		shovelStack = new ItemStack(Material.DIAMOND_SPADE);
		
		ItemMeta meta = shovelStack.getItemMeta();
		meta.setDisplayName(SHOVEL_DISPLAYNAME);
		
		shovelStack.setItemMeta(meta);
	}
	
	@GamePropertyPriority
	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {
		properties.put(GameProperty.INSTANT_BREAK, false);
	}
	
	@Override
	public boolean hasGameProperties() {
		return true;
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the use of shovels in spleef");
	}
	
	@SuppressWarnings("deprecation")
	@GameListener
	public void onGameStart(GameStartEvent event) {
		Game game = event.getGame();
		
		for (SpleefPlayer player : game.getPlayers()) {
			Player bukkitPlayer = player.getBukkitPlayer();
			Inventory inv = bukkitPlayer.getInventory();
			inv.addItem(shovelStack);
			
			bukkitPlayer.updateInventory();
		}
	}
	
}
