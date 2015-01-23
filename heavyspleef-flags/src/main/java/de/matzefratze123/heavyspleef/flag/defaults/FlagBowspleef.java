/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = "bowspleef")
public class FlagBowspleef extends BooleanFlag {

	private static final String BOW_DISPLAYNAME = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Spleef-Bow";
	private static final ItemStack BOW_ITEMSTACK;
	private static final String BOWSPLEEF_METADATA_KEY = "bowspleef";
	
	static {
		BOW_ITEMSTACK = new ItemStack(Material.BOW);
		BOW_ITEMSTACK.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		
		ItemMeta meta = BOW_ITEMSTACK.getItemMeta();
		meta.setDisplayName(BOW_DISPLAYNAME);
		
		BOW_ITEMSTACK.setItemMeta(meta);
	}
	
	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {
		properties.put(GameProperty.DISABLE_FLOOR_BREAK, true);
	}

	@Override
	public boolean hasGameProperties() {
		return true;
	}

	@Override
	public boolean hasBukkitListenerMethods() {
		return true;
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the BowSpleef gamemode");
	}
	
	@SuppressWarnings("deprecation")
	@GameListener
	public void onGameStart(GameStartEvent event) {
		Game game = event.getGame();
		
		ItemStack arrow = new ItemStack(Material.ARROW);
		
		for (SpleefPlayer player : game.getPlayers()) {
			Inventory inventory = player.getBukkitPlayer().getInventory();
			inventory.addItem(BOW_ITEMSTACK);
			inventory.addItem(arrow);
			
			player.getBukkitPlayer().updateInventory();
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (!getValue()) {
			return;
		}
		
		Projectile projectile = event.getEntity();
		if (!(projectile instanceof Arrow)) {
			return;
		}
		
		ProjectileSource source = projectile.getShooter();
		
		if (!(source instanceof Player)) {
			return;
		}
		
		SpleefPlayer shooter = getHeavySpleef().getSpleefPlayer(source);
		Game game = getHeavySpleef().getGameManager().getGame(shooter);
		
		if (game == null || game.getGameState() != GameState.INGAME) {
			return;
		}
		
		// Use a BlockIterator to determine where the arrow has hit the ground
		BlockIterator blockIter = new BlockIterator(projectile.getWorld(), 
				projectile.getLocation().toVector(), 
				projectile.getVelocity().normalize(), 
				0, 4);
		
		Block blockHit = null;
		
		while (blockIter.hasNext()) {
			blockHit = blockIter.next();
			
			if (blockHit.getType() != Material.AIR) {
				break;
			}
		}
		
		if (!game.canSpleef(blockHit)) {
			//Cannot remove this block
			return;
		}
		
		projectile.remove();
		game.addBlockBroken(shooter, blockHit);
		
		if (blockHit.getType() == Material.TNT) {
			return;
		}
		
		// Play an animation
		FallingBlock block = projectile.getWorld().spawnFallingBlock(blockHit.getLocation(), blockHit.getType(), blockHit.getData());
		block.setMetadata(BOWSPLEEF_METADATA_KEY, new FixedMetadataValue(getHeavySpleef().getPlugin(), true));
		
		blockHit.setType(Material.AIR);
	}
	
	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof FallingBlock)) {
			return;
		}
		
		boolean isBowspleefEntity = false;
		List<MetadataValue> metadatas = entity.getMetadata(BOWSPLEEF_METADATA_KEY);
		for (MetadataValue value : metadatas) {
			if (value.getOwningPlugin() != getHeavySpleef().getPlugin()) {
				continue;
			}
			
			isBowspleefEntity = value.asBoolean();
		}
		
		if (isBowspleefEntity) {
			entity.remove();
			event.setCancelled(true);
		}
	}
	
}
