package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam.TeamColor;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "leatherarmor", parent = FlagTeam.class)
public class FlagTeamLeatherArmor extends BaseFlag {

	private static final MaterialData LEATHER_HELMET_DATA = new MaterialData(Material.LEATHER_HELMET);
	private static final MaterialData LEATHER_CHESTPLATE_DATA = new MaterialData(Material.LEATHER_CHESTPLATE);
	private static final MaterialData LEATHER_LEGGINGS_DATA = new MaterialData(Material.LEATHER_LEGGINGS);
	private static final MaterialData LEATHER_BOOTS_DATA = new MaterialData(Material.LEATHER_BOOTS);
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables colored leatherarmor for teams");
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		FlagTeam flagTeam = (FlagTeam) getParent();
		Map<SpleefPlayer, TeamColor> players = flagTeam.getPlayers();
		
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			if (!players.containsKey(player)) {
				continue;
			}
			
			TeamColor color = players.get(player);
			
			ItemStack leatherHelmet = LEATHER_HELMET_DATA.toItemStack(1);
			ItemStack leatherChestplate = LEATHER_CHESTPLATE_DATA.toItemStack(1);
			ItemStack leatherLeggings = LEATHER_LEGGINGS_DATA.toItemStack(1);
			ItemStack leatherBoots = LEATHER_BOOTS_DATA.toItemStack(1);
			
			LeatherArmorMeta meta = (LeatherArmorMeta) leatherHelmet.getItemMeta();
			meta.setColor(color.getRGB());
			
			leatherHelmet.setItemMeta(meta);
			leatherChestplate.setItemMeta(meta);
			leatherLeggings.setItemMeta(meta);
			leatherBoots.setItemMeta(meta);
			
			PlayerInventory inventory = player.getBukkitPlayer().getInventory();
			inventory.setHelmet(leatherHelmet);
			inventory.setChestplate(leatherChestplate);
			inventory.setLeggings(leatherLeggings);
			inventory.setBoots(leatherBoots);
		}
	}

}
