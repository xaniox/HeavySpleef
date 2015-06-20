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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "bowspleef", hasGameProperties = true)
@BukkitListener
public class FlagBowspleef extends BaseFlag {

	private static final String BOW_DISPLAYNAME = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Spleef-Bow";
	private static final ItemStack BOW_ITEMSTACK;
	private static final String BOWSPLEEF_METADATA_KEY = "bowspleef";
	private static final double BLOCK_PADDING = 0.4;
	
	static {
		BOW_ITEMSTACK = new ItemStack(Material.BOW);
		BOW_ITEMSTACK.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		
		ItemMeta meta = BOW_ITEMSTACK.getItemMeta();
		meta.setDisplayName(BOW_DISPLAYNAME);
		
		BOW_ITEMSTACK.setItemMeta(meta);
	}
	
	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {
		properties.put(GameProperty.INSTANT_BREAK, false);
		properties.put(GameProperty.DISABLE_FLOOR_BREAK, true);
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the BowSpleef gamemode");
	}
	
	@SuppressWarnings("deprecation")
	@Subscribe
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
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
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
		
		Location location = projectile.getLocation();
		
		// Use a BlockIterator to determine where the arrow has hit the ground
		BlockIterator blockIter = new BlockIterator(projectile.getWorld(), 
				location.toVector(), 
				projectile.getVelocity().normalize(), 
				0, 4);
		
		Block blockHit = null;
		
		while (blockIter.hasNext()) {
			blockHit = blockIter.next();
			
			if (blockHit.getType() != Material.AIR) {
				break;
			}
		}
		
		if (blockHit == null || !game.canSpleef(blockHit)) {
			//Cannot remove this block
			return;
		}
		
		double xc = location.getX() - (int) location.getX();
		double zc = location.getZ() - (int) location.getZ();
		
		location.setX(blockHit.getX() + xc);
		location.setY(blockHit.getY());
		location.setZ(blockHit.getZ() + zc);
		
		int modX = 0;
		int modZ = 0;
		
		if (xc < BLOCK_PADDING) {
			modX = -1;
		} else if (xc > 1 - BLOCK_PADDING) {
			modX = 1;
		}
		
		if (zc < BLOCK_PADDING) {
			modZ = -1;
		} else if (zc > 1 - BLOCK_PADDING) {
			modZ = 1;
		}
		
		float yaw = location.getYaw();
		ArrowDirection[] dirs = ArrowDirection.getDirections(yaw, modX, modZ);
		
		List<Block> blocks = Lists.newArrayList();
		blocks.add(blockHit);
		
		Vector v1 = null;
		Vector v2 = null;
		
		if (dirs[0] != null) {
			v1 = new Vector(0, 0, modZ);
			blocks.add(location.clone().add(v1).getBlock());
		}
		
		if (dirs[1] != null) {
			v2 = new Vector(modX, 0, 0);
			blocks.add(location.clone().add(v2).getBlock());
		}
		
		if (v1 != null && v2 != null) {
			v1.add(v2);
			blocks.add(location.clone().add(v1).getBlock());
		}
		
		projectile.remove();
		
		for (Block block : blocks) {
			if (!game.canSpleef(blockHit)) {
				//Cannot remove this block
				continue;
			}
			
			game.addBlockBroken(shooter, block);
			
			// Play an animation
			dropBlock(block);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void dropBlock(Block block) {
		Plugin plugin = getHeavySpleef().getPlugin();
		Location location = block.getLocation();
		World world = location.getWorld();
		
		if (block.getType() == Material.TNT) {
			TNTPrimed tntEntity = (TNTPrimed) world.spawnEntity(location, EntityType.PRIMED_TNT);
			tntEntity.setMetadata(BOWSPLEEF_METADATA_KEY, new FixedMetadataValue(plugin, true));
		} else {
			FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(location, block.getType(), block.getData());
			fallingBlock.setMetadata(BOWSPLEEF_METADATA_KEY, new FixedMetadataValue(plugin, true));
		}
		
		block.setType(Material.AIR);
	}
	
	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof FallingBlock)) {
			return;
		}
		
		cancelBowSpleefEntityEvent(entity, event);
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof TNTPrimed)) {
			return;
		}
		
		cancelBowSpleefEntityEvent(entity, event);
	}
	
	private void cancelBowSpleefEntityEvent(Entity entity, Cancellable cancellable) {
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
			cancellable.setCancelled(true);
		}
	}
	
	private enum ArrowDirection {
		
		NORTH(90f, 180f, -180f, -90f, 0, -1),
		SOUTH(-90f, 0f, 0f, 90f, 0, 1),
		WEST(-180f, -90f, -90f, 0, -1, 0),
		EAST(0f, 90f, 90f, 180f, 1, 0);
		
		private float firstFrom;
		private float firstTo;
		private float secondFrom;
		private float secondTo;
		private int modX;
		private int modZ;
		
		private ArrowDirection(float firstFrom, float firstTo, float secondFrom, float secondTo, int modX, int modZ) {
			this.firstFrom = firstFrom;
			this.firstTo = firstTo;
			this.secondFrom = secondFrom;
			this.secondTo = secondTo;
			this.modX = modX;
			this.modZ = modZ;
		}
		
		static ArrowDirection[] getDirections(float yaw, int modX, int modZ) {
			ArrowDirection[] dirs = new ArrowDirection[2];
			
			for (ArrowDirection dir : values()) {
				boolean matches = false;
				
				if ((yaw > dir.firstFrom && yaw < dir.firstTo) || (yaw > dir.secondFrom && yaw < dir.secondTo)) {
					matches = true;
				}
				
				if ((dir.modX != 0 && dir.modX != modX) || dir.modZ != 0 && dir.modZ != modZ) {
					matches = false;
				}
				
				if (matches) {
					int index = dir == WEST || dir == EAST ? 1 : 0;
					dirs[index] = dir;
				}
			}
			
			return dirs;
		}
		
	}
	
}
