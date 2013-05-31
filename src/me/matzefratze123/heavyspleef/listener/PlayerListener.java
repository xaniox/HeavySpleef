/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package me.matzefratze123.heavyspleef.listener;

import static me.matzefratze123.heavyspleef.core.flag.FlagType.SHOVELS;

import java.util.ArrayList;
import java.util.List;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.LoseCause;
import me.matzefratze123.heavyspleef.core.QueuesManager;
import me.matzefratze123.heavyspleef.core.flag.FlagType;
import me.matzefratze123.heavyspleef.core.flag.enums.Difficulty;
import me.matzefratze123.heavyspleef.core.region.LoseZone;
import me.matzefratze123.heavyspleef.util.Permissions;
import me.matzefratze123.heavyspleef.util.SimpleBlockData;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	private ArrayList<String> isCheckOut = new ArrayList<String>();
	public static boolean loseOnTouchWaterOrLava;
	
	public PlayerListener() {
		loseOnTouchWaterOrLava = HeavySpleef.getSystemConfig().getBoolean("blocks.loseOnTouchWaterOrLava", true);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		final Player p = e.getPlayer();
		
		Location to = e.getTo();
		
		if (!GameManager.isInAnyGame(p))
			return;
		Game game = GameManager.fromPlayer(p);
		if (!game.isCounting() && !game.isIngame())
			return;
		
		if (loseOnTouchWaterOrLava && 
				  (to.getBlock().getType() == Material.WATER 
				|| to.getBlock().getType() == Material.LAVA 
				|| to.getBlock().getType() == Material.STATIONARY_WATER 
				|| to.getBlock().getType() == Material.STATIONARY_LAVA))
			out(p, game);
			
		for (LoseZone loseZone : game.getLoseZones()) {
			if (loseZone.contains(to)) {
				out(p, game);
				return;
			}
		}
		
	}
	
	private void out(final Player p, Game game) {
		if (isCheckOut.contains(p.getName()))
			return;
		game.removePlayer(p, LoseCause.LOSE);
		isCheckOut.add(p.getName());
		Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.instance, new Runnable() {
			@Override
			public void run() {
				isCheckOut.remove(p.getName());
			}
		}, 20L);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block block = e.getClickedBlock();
		
		if (p == null)
			return;
		if (block == null)
			return;
		if (!GameManager.isInAnyGame(p))
			return;
		
		Game game = GameManager.fromPlayer(p);
		if (!game.canSpleef(block, p))
			return;
		
		boolean shovels = game.getFlag(SHOVELS);
		if (shovels)
			return;
		
		if (game.getFlag(FlagType.DIFFICULTY) != Difficulty.MEDIUM)
			return;
		
		game.addBrokenBlock(p, block);
		block.setType(Material.AIR);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block block = e.getBlock();
		
		if (!GameManager.isInAnyGame(p)) {
			for (Game game : GameManager.getGames()) {
				if (game.contains(block)) {
					if (p.hasPermission(Permissions.BUILD_BYPASS.getPerm()))
						return;
					if (!HeavySpleef.getSystemConfig().getBoolean("general.protectArena", true))
						return;
					e.setCancelled(true);
					fixBlockGlitch(p, block);
					p.sendMessage(Game._("notAllowedToBuild"));
					return;
				}
			}
			return;
		}
		
		
		Game game = GameManager.fromPlayer(p);
		
		if (!game.canSpleef(block, p)) {
			e.setCancelled(true);
			fixBlockGlitch(p, block);
			p.sendMessage(Game._("notAllowedToBuild"));
			return;
		}
		
		e.getBlock().setTypeId(0);
		game.addBrokenBlock(p, block);
		
	}
	
	private void fixBlockGlitch(Player p, Block b) {
		Location bLoc = b.getLocation();
		Location pLoc = p.getLocation();
		
		if (shouldFix(pLoc, bLoc)) {
			if (!SimpleBlockData.isSolid(b.getTypeId()))
				return;
			
			bLoc.setY(bLoc.getY() + 1);
			bLoc.setX(pLoc.getX());
			bLoc.setZ(pLoc.getZ());
			bLoc.setPitch(pLoc.getPitch());
			bLoc.setYaw(pLoc.getYaw());
			
			p.teleport(bLoc);
		}
	}
	
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		if (GameManager.isInAnyGame(p))
			e.setFoodLevel(20);
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent e) {
		for (Game game : GameManager.getGames()) {
			if (game.contains(e.getLocation())) {
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player player = (Player)e.getEntity();
		if (GameManager.isInAnyGame(player))
			e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGamemodeChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		if (!GameManager.isInAnyGame(p))
			return;
		if (e.getNewGameMode() == GameMode.SURVIVAL)
			return;
		p.sendMessage(Game._("cantChangeGamemode"));
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		for (Game game : GameManager.getGames()) {
			if (!game.contains(e.getBlock()))
				return;
			if (e.getPlayer().hasPermission(Permissions.BUILD_BYPASS.getPerm()))
				return;
			if (!HeavySpleef.getSystemConfig().getBoolean("general.protectArena", true))
				return;
			e.setCancelled(true);
			e.getPlayer().sendMessage(Game._("notAllowedToBuild"));
		}
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (!GameManager.isInAnyGame(e.getPlayer()))
			return;
		if (e.getPlayer().hasPermission(Permissions.COMMAND_WHITELISTED.getPerm()))
			return;
		String[] split = e.getMessage().split(" ");
		String cmd = split[0];
		if (cmd.equalsIgnoreCase("/spleef") || cmd.equalsIgnoreCase("/hs") || cmd.equalsIgnoreCase("/hspleef"))
			return;
		List<String> whitelist = HeavySpleef.getSystemConfig().getStringList("general.commandWhitelist");
		for (String c : whitelist) {
			if (c.equalsIgnoreCase(cmd))
				return;
		}
		e.setCancelled(true);
		e.getPlayer().sendMessage(Game._("cantUseCommands"));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent e) {
		handleQuit(e);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerKick(PlayerKickEvent e) {
		handleQuit(e);
	}
	
	//This event shouldn't be fired because player is in god mode
	//But we don't know if someone types /kill player while he's playing spleef...
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (!GameManager.isInAnyGame(p))
			return;
		
		Game game = GameManager.fromPlayer(p);
		game.removePlayer(p, LoseCause.UNKNOWN);
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		if (!GameManager.isInAnyGame(e.getPlayer()))
			return;
		
		e.setCancelled(true);
	}
	
	private void handleQuit(PlayerEvent e) {
		QueuesManager.removeFromQueue(e.getPlayer());
		
		if (!GameManager.isInAnyGame(e.getPlayer()))
			return;
		
		Game game = GameManager.fromPlayer(e.getPlayer());
		game.removePlayer(e.getPlayer(), LoseCause.QUIT);
	}
	
	private boolean shouldFix(Location pLoc, Location bLoc) {
		return pLoc.getBlockX() == bLoc.getBlockX() && pLoc.getBlockZ() == bLoc.getBlockZ() && pLoc.getY() > bLoc.getY();
	}
	
}
