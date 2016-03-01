/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.flag.defaults;

import de.xaniox.heavyspleef.core.event.PlayerBlockBreakEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.BukkitListener;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.game.GameProperty;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;
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

import java.util.List;

@Flag(name = "snowballs")
@BukkitListener
public class FlagSnowballs extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Gives players a certain amount of snowballs to use when they break blocks");
	}
	
	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 1) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_SNOWBALL_AMOUNT));
		}
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