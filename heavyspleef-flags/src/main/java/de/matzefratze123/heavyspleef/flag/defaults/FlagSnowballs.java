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

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.PlayerBlockBreakEvent;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "snowballs")
@BukkitListener
public class FlagSnowballs extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Gives players a certain amount of snowballs to use when they break blocks");
	}
	
	@Subscribe
	public void onBlockBreak(PlayerBlockBreakEvent event) {
		int val = getValue();
		
		ItemStack stack = new ItemStack(Material.SNOW_BALL, val);
		
		SpleefPlayer player = event.getPlayer();
		player.getBukkitPlayer().getInventory().addItem(stack);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		if (!(projectile instanceof Snowball)) {
			return;
		}
		
		ProjectileSource shooter = projectile.getShooter();
		if (!(shooter instanceof Player)) {
			return;
		}
		
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(shooter);
		GameManager manager = getHeavySpleef().getGameManager();
		Game game;
		
		if ((game = manager.getGame(player)) == null) {
			return;
		}

		Location location = projectile.getLocation();
		Vector start = location.toVector();
		Vector dir = projectile.getVelocity().normalize();
		
		BlockIterator iterator = new BlockIterator(projectile.getWorld(), start, dir, 0, 4);
		
		Block blockHit = null;
		
		while (iterator.hasNext()) {
			blockHit = iterator.next();
			
			if (blockHit.getType() != Material.AIR) {
				break;
			}
		}
		
		if (!game.canSpleef(blockHit)) {
			//Cannot remove this block
			return;
		}
		
		projectile.remove();
		game.addBlockBroken(player, blockHit);
		
		blockHit.setType(Material.AIR);
		if (game.getPropertyValue(GameProperty.PLAY_BLOCK_BREAK)) {
			blockHit.getWorld().playEffect(blockHit.getLocation(), Effect.STEP_SOUND, blockHit.getTypeId());
		}
	}

}
