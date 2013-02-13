package me.matzefratze123.heavyspleef.listener;

import java.util.ArrayList;
import java.util.List;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Cuboid;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.LoseCause;
import me.matzefratze123.heavyspleef.utility.LocationHelper;
import me.matzefratze123.heavyspleef.utility.MaterialNameHelper;
import me.matzefratze123.heavyspleef.utility.Permissions;
import me.matzefratze123.heavyspleef.utility.PlayerStateManager;

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
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

	private ArrayList<String> isCheckOut = new ArrayList<String>();
	private static List<Integer> cantBreak;
	private static boolean loseOnTouchWaterOrLava;
	
	public PlayerListener() {
		cantBreak = HeavySpleef.instance.getConfig().getIntegerList("blocks.cantBreak");
		loseOnTouchWaterOrLava = HeavySpleef.instance.getConfig().getBoolean("blocks.loseOnTouchWaterOrLava");
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		final Player p = e.getPlayer();
		
		Location to = e.getTo();
		Location from = e.getFrom();
		
		if (!GameManager.isInAnyGame(p))
			return;
		Game game = GameManager.getGameFromPlayer(p);
		if (!game.isCounting() && !game.isIngame())
			return;
		
		if (loseOnTouchWaterOrLava && (to.getBlock().getType() == Material.STATIONARY_WATER || to.getBlock().getType() == Material.STATIONARY_LAVA))
			out(p, game);
			
		for (Cuboid loseZone : game.getLoseZones()) {
			if (LocationHelper.isInsideRegion(to, loseZone.getFirstCorner(), loseZone.getSecondCorner()))
				out(p, game);
		}
		
		if (!LocationHelper.isInsideRegion(to, game.getFirstCorner(), game.getSecondCorner())) {
			if (game.isCounting() || game.isIngame()) {
				p.teleport(from);
				p.sendMessage(Game._("notAllowedLeaveArena"));
			}
		}
		
	}
	
	private void out(final Player p, Game game) {
		if (isCheckOut.contains(p.getName()))
			return;
		p.sendMessage(Game._("outOfGame"));
		game.removePlayer(p, LoseCause.LOSE);
		isCheckOut.add(p.getName());
		Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.instance, new Runnable() {
			@Override
			public void run() {
				isCheckOut.remove(p.getName());
			}
		}, 20L);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block block = e.getClickedBlock();
		
		if (p == null)
			return;
		if (block == null)
			return;
		if (!GameManager.isInAnyGame(p))
			return;
		
		Game game = GameManager.getGameFromPlayer(p);
		if (!LocationHelper.isInsideRegion(block.getLocation(), game.getFirstInnerCorner(), game.getSecondInnerCorner()))
			return;
		if (!game.isIngame())
			return;
		block.setType(Material.AIR);
		game.addBrokenBlock(p, block);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block block = e.getBlock();
		
		if (!GameManager.isInAnyGame(p)) {
			for (Game game : GameManager.getGames()) {
				if (LocationHelper.isInsideRegion(block, game.getFirstCorner(), game.getSecondCorner())) {
					if (p.hasPermission(Permissions.BUILD_BYPASS.getPerm()) && !game.isIngame())
						return;
					if (!HeavySpleef.instance.getConfig().getBoolean("general.protectArena"))
						return;
					e.setCancelled(true);
					fixBlockGlitch(p, block);
					p.sendMessage(Game._("notAllowedToBuild"));
					return;
				}
			}
			return;
		}
		
		
		Game game = GameManager.getGameFromPlayer(p);
		if (game.isCounting()) {
			e.setCancelled(true);
			fixBlockGlitch(p, block);
			p.sendMessage(Game._("notAllowedToBuild"));
			return;
		}
		
		if (cantBreak.contains(block.getTypeId())) {
			e.setCancelled(true);
			fixBlockGlitch(p, block);
			p.sendMessage(Game._("notAllowedToBreakSpecified", MaterialNameHelper.getName(block.getType().name())));
			return;
		}
		
		if (LocationHelper.isInsideRegion(block, game.getFirstInnerCorner(), game.getSecondInnerCorner())) {
			if (p.hasPermission(Permissions.BUILD_BYPASS.getPerm()) && !game.isIngame())
				return;
			e.setCancelled(true);
			fixBlockGlitch(p, block);
			p.sendMessage(Game._("notAllowedToBuild"));
		} else {
			e.setCancelled(true);
			fixBlockGlitch(p, block);
			p.sendMessage(Game._("notAllowedToBuild"));
		}
	}
	
	private void fixBlockGlitch(Player p, Block b) {
		Location bLoc = b.getLocation();
		Location pLoc = p.getLocation();
		
		if (pLoc.getBlockX() == bLoc.getBlockX() && pLoc.getBlockZ() == bLoc.getBlockZ() && pLoc.getY() > bLoc.getY()) {
			if (!GameManager.isSolid(b))
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
			if (LocationHelper.isInsideRegion(e.getLocation(), game.getFirstCorner(), game.getSecondCorner())) {
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
			if (!LocationHelper.isInsideRegion(e.getBlock().getLocation(), game.getFirstCorner(), game.getSecondCorner()))
				return;
			if (e.getPlayer().hasPermission(Permissions.BUILD_BYPASS.getPerm()) && !game.isIngame())
				return;
			if (!HeavySpleef.instance.getConfig().getBoolean("general.protectArena"))
				return;
			e.setCancelled(true);
			e.getPlayer().sendMessage(Game._("notAllowedToBuild"));
		}
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (!GameManager.isInAnyGame(e.getPlayer()))
			return;
		String[] split = e.getMessage().split(" ");
		String cmd = split[0];
		if (cmd.equalsIgnoreCase("/spleef") || cmd.equalsIgnoreCase("/hs") || cmd.equalsIgnoreCase("/hspleef"))
			return;
		List<String> whitelist = HeavySpleef.instance.getConfig().getStringList("general.commandWhitelist");
		for (String c : whitelist) {
			if (c.equalsIgnoreCase(cmd))
				return;
		}
		e.setCancelled(true);
		e.getPlayer().sendMessage(Game._("cantUseCommands"));
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (!GameManager.isInAnyGame(e.getPlayer()))
			return;
		Game game = GameManager.getGameFromPlayer(e.getPlayer());
		if (LocationHelper.isInsideRegion(e.getTo(), game.getFirstCorner(), game.getSecondCorner()))
			return;
		if (isSameBlockLocation(e.getTo(), game.getWinPoint()))
			return;
		if (isSameBlockLocation(e.getTo(), game.getLosePoint()))
			return;
		if (shouldFix(e.getTo(), e.getFrom()))
			return;
		
		
		
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent e) {
		handleQuit(e);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerKick(PlayerKickEvent e) {
		handleQuit(e);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		for (Game game : GameManager.getGames()) {
			if (game.wereOffline.contains(p.getName())) {
				p.teleport(game.getLosePoint());
				p.sendMessage(Game._("loginAfterServerShutdown", game.getName()));
				PlayerStateManager.restorePlayerState(p);
				return;
			}
		}
	}
	
	//This event shouldn't be fired because player is in god mode, but save is save
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (!GameManager.isInAnyGame(p))
			return;
		
		Game game = GameManager.getGameFromPlayer(p);
		game.removePlayer(p, LoseCause.UNKNOWN);
	}
	
	private void handleQuit(PlayerEvent e) {
		if (!GameManager.isInAnyGame(e.getPlayer()))
			return;
		Game game = GameManager.getGameFromPlayer(e.getPlayer());
		game.removePlayer(e.getPlayer(), LoseCause.QUIT);
		e.getPlayer().teleport(game.getLosePoint());
	}
	
	private boolean isSameBlockLocation(Location loc1, Location loc2) {
		return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
	}
	
	private boolean shouldFix(Location pLoc, Location bLoc) {
		return pLoc.getBlockX() == bLoc.getBlockX() && pLoc.getBlockZ() == bLoc.getBlockZ() && pLoc.getY() > bLoc.getY();
	}
	
}
