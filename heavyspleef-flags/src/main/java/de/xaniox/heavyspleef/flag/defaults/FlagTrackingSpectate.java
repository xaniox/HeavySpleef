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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.matzefratze123.inventoryguilib.GuiInventory;
import de.matzefratze123.inventoryguilib.GuiInventorySlot;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.MetadatableItemStack;
import de.xaniox.heavyspleef.core.SimpleBasicTask;
import de.xaniox.heavyspleef.core.Unregister;
import de.xaniox.heavyspleef.core.event.PlayerJoinGameEvent;
import de.xaniox.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.BukkitListener;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.FlagInit;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Flag(name = "tracking", parent = FlagSpectate.class)
@BukkitListener
public class FlagTrackingSpectate extends BaseFlag {
	
	private static final String TRACKER_KEY = "spectate_tracker";
	private static final int ITEM_SLOT = 0;
	private static final int MAX_LINES = 6;
	
	private static TrackingTask task;
	
	private @Inject
    Game game;
	private GuiInventory trackerInventory;
	private Map<SpleefPlayer, SpleefPlayer> tracking;
	
	public FlagTrackingSpectate() {
		this.tracking = Maps.newHashMap();
	}
	
	@FlagInit
	public static void startTrackingTask(HeavySpleef heavySpleef) {
		task = new TrackingTask(heavySpleef);
		task.start();
	}
	
	@Unregister
	public static void cancelTrackingTask(HeavySpleef heavySpleef) {
		if (task != null && task.isRunning()) {
			task.cancel();
		}
		
		task = null;
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Gives spectating players a tracking compass to teleport to ingame players");
	}
	
	@Subscribe
	public void onSpectateEnter(FlagSpectate.SpectateEnteredEvent event) {
		MetadatableItemStack compass = new MetadatableItemStack(Material.COMPASS);
		ItemMeta meta = compass.getItemMeta();
		meta.setDisplayName(getI18N().getString(Messages.Player.TRACKER));
		meta.setLore(Lists.newArrayList(getI18N().getString(Messages.Player.TRACKER_LORE)));
		compass.setItemMeta(meta);
		compass.setMetadata(TRACKER_KEY, null);
		
		Player bukkitPlayer = event.getPlayer().getBukkitPlayer();
		Inventory inventory = bukkitPlayer.getInventory();
		inventory.setItem(ITEM_SLOT, compass);
		bukkitPlayer.updateInventory();
	}
	
	@Subscribe
	public void onSpectateLeave(FlagSpectate.SpectateLeaveEvent event) {
		Player player = event.getPlayer().getBukkitPlayer();
		Inventory inventory = player.getInventory();
		
		for (ItemStack stack : inventory.getContents()) {
			if (stack == null) {
				continue;
			}
			
			MetadatableItemStack metadatable = new MetadatableItemStack(stack);
			if (!metadatable.hasItemMeta() || !metadatable.getItemMeta().hasLore() || !metadatable.hasMetadata(TRACKER_KEY)) {
				continue;
			}
			
			inventory.remove(stack);
		}
		
		player.updateInventory();
		tracking.remove(player);
	}
	
	@SuppressWarnings("deprecation")
	@Subscribe
	public void onPlayerJoinGame(PlayerJoinGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		GuiInventory trackerInventory = getTrackerInventory();
		
		//Insert the player's head somewhere in the tracker inventory
		for (int y = 0; y < trackerInventory.getLines(); y++) {
			for (int x = 0; x < GuiInventory.SLOTS_PER_LINE; x++) {
				trackerInventory.getSlot(x, y);
				
				GuiInventorySlot slot = trackerInventory.getSlot(x, y);
				if (slot.getItem() != null) {
					continue;
				}
				
				MaterialData data = new MaterialData(Material.SKULL_ITEM, (byte)SkullType.PLAYER.ordinal());
				ItemStack skull = data.toItemStack(1);
				SkullMeta meta = (SkullMeta) skull.getItemMeta();
				meta.setDisplayName(getI18N().getVarString(Messages.Player.TRACKER_SKULL_TITLE)
						.setVariable("tracking", player.getDisplayName())
						.toString());
				meta.setOwner(player.getName());
				skull.setItemMeta(meta);
				
				slot.setItem(skull);
				slot.setValue(player);
				break;
			}
		}
		
		trackerInventory.updateViews();
	}
	
	@Subscribe
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		
		GuiInventory trackerInventory = getTrackerInventory();
		GuiInventorySlot slot = trackerInventory.getSlotByValue(player);
		
		if (slot == null) {
			return;
		}
		
		slot.setItem((ItemStack)null);
		slot.setValue(null);
		
		//Shift all other tracked players left
		for (int y = slot.getY(); y < trackerInventory.getLines(); y++) {
			for (int x = slot.getX() + 1; x < GuiInventory.SLOTS_PER_LINE; x++) {
				GuiInventorySlot otherSlot = trackerInventory.getSlot(x, y);
				
				int shiftingX = x == 0 ? GuiInventory.SLOTS_PER_LINE - 1 : x;
				int shiftingY = x == 0 ? y - 1 : y;
				
				GuiInventorySlot shiftingSlot = trackerInventory.getSlot(shiftingX, shiftingY);
				shiftingSlot.setItem(otherSlot.getItem());
				shiftingSlot.setValue(otherSlot.getValue());
				
				otherSlot.setItem((ItemStack)null);
				otherSlot.setValue(null);
			}
		}
		
		trackerInventory.updateViews();
		
		Iterator<Entry<SpleefPlayer, SpleefPlayer>> iterator = tracking.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<SpleefPlayer, SpleefPlayer> entry = iterator.next();
			
			if (entry.getValue() != player) {
				continue;
			}
			
			iterator.remove();
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		FlagSpectate flag = (FlagSpectate) getParent();
		
		if (!flag.isSpectating(player)) {
			return;
		}
		
		ItemStack clicked = player.getBukkitPlayer().getItemInHand();
		if (clicked == null) {
			return;
		}
		
		MetadatableItemStack metadatable = new MetadatableItemStack(clicked);
		if (!metadatable.hasItemMeta() || !metadatable.getItemMeta().hasLore() || !metadatable.hasMetadata(TRACKER_KEY)) {
			return;
		}
		
		//Open inventory
		getTrackerInventory().open(player.getBukkitPlayer());
		
		//Prevent the player from using the compass as a WorldEdit teleporter for example
		event.setCancelled(true);
	}
	
	private GuiInventory getTrackerInventory() {
		int lines = (int) Math.ceil(game.getPlayers().size() / (double)GuiInventory.SLOTS_PER_LINE);
		if (lines > MAX_LINES) {
			lines = MAX_LINES;
		} else if (lines <= 0) {
			lines = 1;
		}
		
		if (trackerInventory == null) {
			trackerInventory = new GuiInventory(getHeavySpleef().getPlugin(), lines, getI18N().getString(Messages.Player.TRACKER_INVENTORY_TITLE)) {
				
				@Override
				public void onClick(GuiClickEvent event) {
					event.setCancelled(true);
					
					SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
					GuiInventorySlot slot = event.getSlot();
					
					SpleefPlayer to = (SpleefPlayer) slot.getValue();
					if (to == null) {
						return;
					}
					
					player.teleport(to.getBukkitPlayer().getLocation());
					player.getBukkitPlayer().setCompassTarget(to.getBukkitPlayer().getLocation());
					tracking.put(player, to);
					
					player.sendMessage(getI18N().getVarString(Messages.Player.TRACKER_NOW_TRACKING)
							.setVariable("tracking", to.getDisplayName())
							.toString());
				}
			};
		} else {
			if (lines != trackerInventory.getLines()) {
				trackerInventory.setLines(lines);
			}
		}
		
		return trackerInventory;
	}
	
	private static class TrackingTask extends SimpleBasicTask {

		private GameManager gameManager;
		
		public TrackingTask(HeavySpleef heavySpleef) {
			super(heavySpleef.getPlugin(), TaskType.SYNC_REPEATING_TASK, 20L, 20L);
			
			this.gameManager = heavySpleef.getGameManager();
		}

		@Override
		public void run() {
			for (Game game : gameManager.getGames()) {
				if (!game.isFlagPresent(FlagTrackingSpectate.class)) {
					continue;
				}
				
				FlagTrackingSpectate flag = game.getFlag(FlagTrackingSpectate.class);
				Map<SpleefPlayer, SpleefPlayer> trackingMap = flag.tracking;
				
				for (Entry<SpleefPlayer, SpleefPlayer> entry : trackingMap.entrySet()) {
					Player tracker = entry.getKey().getBukkitPlayer();
					Player tracking = entry.getValue().getBukkitPlayer();
					
					tracker.setCompassTarget(tracking.getLocation());
				}
			}
		}
		
	}

}