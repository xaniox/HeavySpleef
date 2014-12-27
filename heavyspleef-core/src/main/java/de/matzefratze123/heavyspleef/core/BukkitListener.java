package de.matzefratze123.heavyspleef.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
	
	private void handleEntityDamageEvent(EntityDamageEvent event) {
		Entity damagedEntity = event.getEntity();
		if (!(damagedEntity instanceof Player)) {
			return;
		}
		
		handlePlayerEvent((Player) damagedEntity, event);
	}
	
	private void handlePlayerEvent(Player bukkitPlayer, Event event) {
		SpleefPlayer player = playerManager.getSpleefPlayer(bukkitPlayer);
		
		Game game = gameManager.getGame(player);
		if (game != null) {
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
			}
		}
	}
	
}