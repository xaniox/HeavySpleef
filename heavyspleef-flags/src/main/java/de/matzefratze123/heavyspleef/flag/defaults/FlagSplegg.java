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
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerInteractGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = "splegg", hasGameProperties = true)
@BukkitListener
public class FlagSplegg extends BooleanFlag {

	private static final String SPLEGG_LAUNCHER_DISPLAYNAME = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Splegg Launcher";
	private static final List<String> SPLEGG_LAUNCHER_LORE = Lists.newArrayList(ChatColor.GRAY + "Right-Click to launch an egg");
	private static final ItemStack SPLEGG_LAUNCHER_ITEMSTACK;
	
	static {
		SPLEGG_LAUNCHER_ITEMSTACK = new ItemStack(Material.IRON_SPADE);
		
		ItemMeta meta = SPLEGG_LAUNCHER_ITEMSTACK.getItemMeta();
		meta.setDisplayName(SPLEGG_LAUNCHER_DISPLAYNAME);
		meta.setLore(SPLEGG_LAUNCHER_LORE);
		
		SPLEGG_LAUNCHER_ITEMSTACK.setItemMeta(meta);
	}
	
	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {
		properties.put(GameProperty.DISABLE_FLOOR_BREAK, true);
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the Splegg gamemode in spleef games.");
	}
	
	@SuppressWarnings("deprecation")
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		Game game = event.getGame();
		
		for (SpleefPlayer player : game.getPlayers()) {
			Inventory inv = player.getBukkitPlayer().getInventory();
			inv.addItem(SPLEGG_LAUNCHER_ITEMSTACK);
			
			player.getBukkitPlayer().updateInventory();
		}
	}
	
	@Subscribe
	public void onPlayerInteractGame(PlayerInteractGameEvent event) {
		HeavySpleef heavySpleef = getHeavySpleef();
		SpleefPlayer player = event.getPlayer();
		
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		GameManager manager = heavySpleef.getGameManager();
		Game game = manager.getGame(player);
		
		if (game == null || game.getGameState() != GameState.INGAME) { 
			return;
		}
		
		Player bukkitPlayer = player.getBukkitPlayer();
		ItemStack inHand = bukkitPlayer.getItemInHand();
		if (inHand.getType() != SPLEGG_LAUNCHER_ITEMSTACK.getType()) {
			return;
		}
		
		bukkitPlayer.launchProjectile(Arrow.class);
		bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.GHAST_FIREBALL, 0.4f, 2f);
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (!getValue()) {
			return;
		}
		
		Projectile projectile = event.getEntity();
		if (!(projectile instanceof Egg)) {
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
		
		blockHit.setType(Material.AIR);
	}
	
}
