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
package de.matzefratze123.heavyspleef.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.player.PlayerManager;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class BukkitListener implements Listener {
	
	private PlayerManager playerManager;
	private GameManager gameManager;
	
	public BukkitListener(PlayerManager playerManager, GameManager gameManager, JavaPlugin plugin) {
		this.playerManager = playerManager;
		this.gameManager = gameManager;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		HumanEntity entity = event.getEntity();
		
		if (!(entity instanceof Player)) {
			return;
		}
		
		handlePlayerEvent((Player)entity, event);
	}
	
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		handleEntityDamageEvent(event);
	}
	
	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		handleEntityDamageEvent(event);
	}
	
	@EventHandler
	public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
		LivingEntity livingTarget = event.getTarget();
		if (!(livingTarget instanceof Player)) {
			return;
		}
		
		Player player = (Player) livingTarget;
		handlePlayerEvent(player, event);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		handlePlayerEvent(event.getEntity(), event);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		handlePlayerEvent(event.getPlayer(), event);
	}
	
	private void handleEntityDamageEvent(EntityDamageEvent event) {
		Entity damagedEntity = event.getEntity();
		if (!(damagedEntity instanceof Player)) {
			return;
		}
		
		handlePlayerEvent((Player) damagedEntity, event);
	}
	
	private void handlePlayerEvent(Player bukkitPlayer, Event event) {
		SpleefPlayer player = playerManager.getSpleefPlayer(bukkitPlayer);
		
		if (event instanceof PlayerRespawnEvent
				|| event instanceof PlayerQuitEvent
				|| event instanceof PlayerKickEvent
				|| event instanceof PlayerCommandPreprocessEvent) {
			for (Game game : gameManager.getGames()) {
				invokeEvent(game, event, player);
			}
		} else {
			Game game = gameManager.getGame(player);
			if (game != null) {
				invokeEvent(game, event, player);
			}
		}
	}
	
	private void invokeEvent(Game game, Event event, SpleefPlayer player) {
		if (event instanceof PlayerInteractEvent) {
			game.onPlayerInteract((PlayerInteractEvent)event, player);
		} else if (event instanceof BlockBreakEvent) {
			game.onPlayerBreakBlock((BlockBreakEvent)event, player);
		} else if (event instanceof BlockPlaceEvent) {
			game.onPlayerPlaceBlock((BlockPlaceEvent)event, player);
		} else if (event instanceof PlayerPickupItemEvent) {
			game.onPlayerPickupItem((PlayerPickupItemEvent)event, player);
		} else if (event instanceof PlayerDropItemEvent) {
			game.onPlayerDropItem((PlayerDropItemEvent)event, player);
		} else if (event instanceof FoodLevelChangeEvent) {
			game.onPlayerFoodLevelChange((FoodLevelChangeEvent)event, player);
		} else if (event instanceof EntityDamageByEntityEvent) {
			game.onEntityByEntityDamageEvent((EntityDamageByEntityEvent)event, player);
		} else if (event instanceof EntityDamageEvent) {
			game.onEntityDamageEvent((EntityDamageEvent) event, player);
		} else if (event instanceof EntityTargetLivingEntityEvent) {
			game.onEntityTargetLivingEntity((EntityTargetLivingEntityEvent) event, player);
		} else if (event instanceof PlayerQuitEvent) {
			game.onPlayerQuit((PlayerQuitEvent) event, player);
		} else if (event instanceof PlayerKickEvent) {
			game.onPlayerKick((PlayerKickEvent)event, player);
		} else if (event instanceof PlayerCommandPreprocessEvent) {
			game.onPlayerCommandPreprocess((PlayerCommandPreprocessEvent)event, player);
		} else if (event instanceof PlayerDeathEvent) {
			game.onPlayerDeath((PlayerDeathEvent)event, player);
		} else if (event instanceof PlayerRespawnEvent) {
			game.onPlayerRespawn((PlayerRespawnEvent)event, player);
		} else if (event instanceof PlayerGameModeChangeEvent) {
			game.onPlayerGameModeChange((PlayerGameModeChangeEvent)event, player);
		} else if (event instanceof PlayerTeleportEvent) {
			game.onPlayerTeleport((PlayerTeleportEvent)event, player);
		}
	}
	
}