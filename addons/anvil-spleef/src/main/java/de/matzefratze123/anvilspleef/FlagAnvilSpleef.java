/*
 * This file is part of addons.
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
package de.matzefratze123.anvilspleef;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.core.SimpleBasicTask;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.Inject;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.flag.ValidationException;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameProperty;
import de.matzefratze123.heavyspleef.core.game.QuitCause;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.defaults.FlagScoreboard.GetScoreboardDisplayNameEvent;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "anvil-spleef", hasGameProperties = true)
@BukkitListener
public class FlagAnvilSpleef extends IntegerFlag {

	private static final int MAX_SPAWN_RATE = 150;
	private static final int MIN_SPAWN_RATE = 10;
	private static final int DEFAULT_SPAWN_RATE = 50;
	private static Method GET_HANDLE_METHOD;
	private static Field HURT_ENTITIES_FIELD;
	
	@Inject
	private Game game;
	private List<FallingBlock> fallingAnvils;
	private AnvilSpawnTask task;
	
	public FlagAnvilSpleef() {
		this.fallingAnvils = Lists.newArrayList();
	}
	
	@Override
	public void onFlagAdd(Game game) {
		this.task = new AnvilSpawnTask(game);
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Adds the gamemode Anvil Spleef to your game");
		description.add("Specify the number of anvils being spawned every second in a number");
	}
	
	@Override
	public Integer parseInput(SpleefPlayer player, String input) throws InputParseException {
		int rate = DEFAULT_SPAWN_RATE;
		
		if (!input.trim().isEmpty()) {
			rate = super.parseInput(player, input);
		}
		
		return rate;
	}
	
	@Override
	public void validateInput(Integer input, Game game) throws ValidationException {
		if (game.getFloors().size() != 1) {
			throw new ValidationException(getI18N().getString(ASMessages.ANVIL_SPLEEF_GAME_FLOOR_LIMITED));
		}
		
		if (input < MIN_SPAWN_RATE || input > MAX_SPAWN_RATE) {
			throw new ValidationException(getI18N().getString(ASMessages.SPAWN_RATE_LIMITS));
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent e) {
		EntityType type = e.getEntityType();
		if (type != EntityType.FALLING_BLOCK) {
			return;
		}
		
		Entity entity = e.getEntity();
		if (!fallingAnvils.contains(entity)) {
			return;
		}
		
		Block block = e.getBlock();
		Block under = block.getRelative(BlockFace.DOWN);
		
		fallingAnvils.remove(entity);
		e.setCancelled(true);		
		
		if (!game.canSpleef(under)) {
			entity.remove();
			return;
		}
		
		Material material = under.getType();
		under.setType(Material.AIR);
		World world = under.getWorld();
		world.playSound(block.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
		
		if (game.getPropertyValue(GameProperty.PLAY_BLOCK_BREAK)) {
			world.playEffect(under.getLocation(), Effect.STEP_SOUND, material.getId());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getCause() != DamageCause.FALLING_BLOCK) {
			return;
		}
		
		Entity damaged = e.getEntity();
		if (!(damaged instanceof Player)) {
			return;
		}
		
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(damaged);
		if (!game.isIngame(player)) {
			return;
		}
		
		e.setCancelled(false);
	}
	
	@Subscribe
	public void onPlayerLeaveGameEvent(PlayerLeaveGameEvent e) {
		if (e.getCause() != QuitCause.LOSE) {
			return;
		}
		
		SpleefPlayer player = e.getPlayer();
		if (!player.getBukkitPlayer().isDead()) {
			return;
		}
		
		e.setPlayerMessage(getI18N().getString(Messages.Player.PLAYER_LOSE));
		e.setBroadcastMessage(getI18N().getVarString(ASMessages.ANVIL_DEATH_BROADCAST_MESSAGE)
				.setVariable("player", player.getDisplayName())
				.toString());
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		if (task.isRunning()) {
			task.cancel();
		}
		
		fallingAnvils.clear();
		task.calculateSpawningBlocks();
		task.start();
	}
	
	@Subscribe
	public void onGameEnd(GameEndEvent event) {
		if (task.isRunning()) {
			task.cancel();
		}
		
		for (FallingBlock block : fallingAnvils) {
			block.remove();
		}
	}
	
	@Subscribe(priority = Priority.HIGH)
	public void onGetScoreboardDisplayName(GetScoreboardDisplayNameEvent event) {
		event.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Anvil Spleef");
	}
	
	private void initHurtEntitiesField(FallingBlock block) {
		try {
			GET_HANDLE_METHOD = block.getClass().getMethod("getHandle");

			Object handle = GET_HANDLE_METHOD.invoke(block);
			HURT_ENTITIES_FIELD = handle.getClass().getDeclaredField("hurtEntities");
			HURT_ENTITIES_FIELD.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			getHeavySpleef().getLogger().log(Level.SEVERE, "Cannot reflect getHandle() method or hurtEntities field", e);
		}
	}
	
	private class AnvilSpawnTask extends SimpleBasicTask {

		//Spawn anvil 20 blocks above the top floor
		private static final int ANVIL_SPAWN_HEIGHT = 20;
		
		private Game game;
		private List<Location> spawning;
		private final Random random;
		
		public AnvilSpawnTask(Game game) {
			super(getHeavySpleef().getPlugin(), TaskType.SYNC_REPEATING_TASK, 4L, 4L);
			
			this.game = game;
			this.spawning = Lists.newArrayList();
			this.random = new Random();
		}
		
		private void calculateSpawningBlocks() {
			spawning.clear();
			
			Collection<Floor> floors = game.getFloors();
			Floor floor = floors.iterator().next();
			
			Region region = floor.getRegion();
			Iterator<BlockVector> iterator = region.iterator();
			
			while (iterator.hasNext()) {
				BlockVector vector = iterator.next();
				Location location = BukkitUtil.toLocation(game.getWorld(), vector);
				
				boolean alreadyRegistered = false;
				for (Location spawnLoc : spawning) {
					if (location.getBlockX() != spawnLoc.getBlockX() || location.getBlockZ() != spawnLoc.getBlockZ()) {
						continue;
					}
					
					alreadyRegistered = true;
					break;
				}
				
				if (alreadyRegistered) {
					continue;
				}
				
				spawning.add(location.add(0, ANVIL_SPAWN_HEIGHT, 0));
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			int rateNow = (int) ((getTaskArgument(1) * getValue()) / 20);
			boolean reflectExceptionPrint = false;
			
			List<Location> spawningCopy = Lists.newArrayList(spawning);
			for (int i = 0; i < rateNow; i++) {
				int rndIndex = random.nextInt(spawningCopy.size());
				Location location = spawningCopy.remove(rndIndex);
				
				FallingBlock block = game.getWorld().spawnFallingBlock(location, Material.ANVIL, (byte)0);
				fallingAnvils.add(block);
				
				try {
					if (GET_HANDLE_METHOD == null || HURT_ENTITIES_FIELD == null) {
						//Lazy initialization over here
						initHurtEntitiesField(block);
					}
					
					Object handle = GET_HANDLE_METHOD.invoke(block);
					HURT_ENTITIES_FIELD.set(handle, true);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					if (reflectExceptionPrint) {
						continue;
					}
					
					getHeavySpleef().getLogger().log(Level.SEVERE, "Cannot set the hurtEntities attribute for a falling anvil", e);
					reflectExceptionPrint = true;
				}
			}
			
			//Remove anvils which are under the floor
			Floor floor = game.getFloors().iterator().next();
			
			Iterator<FallingBlock> iterator = fallingAnvils.iterator();
			while (iterator.hasNext()) {
				FallingBlock block = iterator.next();
				Location location = block.getLocation();
				
				if (location.getY() >= floor.getRegion().getMinimumPoint().getY()) {
					continue;
				}
				
				block.remove();
				iterator.remove();
			}
		}
		
		@Override
		public void cancel() {
			super.cancel();
			
			spawning.clear();
		}
		
	}

}
