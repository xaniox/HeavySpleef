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
import de.xaniox.heavyspleef.core.MetadatableItemStack;
import de.xaniox.heavyspleef.core.config.DefaultConfig;
import de.xaniox.heavyspleef.core.event.GameStartEvent;
import de.xaniox.heavyspleef.core.event.PlayerJoinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.BukkitListener;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.QuitCause;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;

@Flag(name = "leave-item")
@BukkitListener
public class FlagLeaveItem extends BaseFlag {

	private static final String LEAVE_ITEM_KEY = "leave_item_game";
	private static final int RIGHT_HOTBAR_SLOT = 8;
	
	@Inject
	private DefaultConfig config;
	@Inject
	private Game game;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Adds a leave item to the player's hand which he can right click to leave the game");
	}
	
	@Subscribe
	public void onPlayerJoinGame(PlayerJoinGameEvent event) {
		MaterialData leaveItemData = config.getFlagSection().getLeaveItem();
		MetadatableItemStack stack = new MetadatableItemStack(leaveItemData.toItemStack(1));
		
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(getI18N().getString(Messages.Player.LEAVE_GAME_DISPLAYNAME));
		meta.setLore(Lists.newArrayList(getI18N().getString(Messages.Player.LEAVE_GAME_LORE)));
		stack.setItemMeta(meta);
		stack.setMetadata(LEAVE_ITEM_KEY, null);
		
		Player bukkitPlayer = event.getPlayer().getBukkitPlayer();
		bukkitPlayer.getInventory().setItem(RIGHT_HOTBAR_SLOT, stack);
		bukkitPlayer.updateInventory();
	}
	
	@Subscribe(priority = Subscribe.Priority.LOW)
	public void onGameStart(GameStartEvent event) {
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			Player bukkitPlayer = player.getBukkitPlayer();
			Inventory inv = bukkitPlayer.getInventory();
			
			for (int i = 0; i < inv.getSize(); i++) {
				ItemStack stack = inv.getItem(i);
				if (stack == null || stack.getType() == Material.AIR) {
					continue;
				}
				
				MetadatableItemStack metadatable = new MetadatableItemStack(stack);
				if (!metadatable.hasItemMeta() || !metadatable.getItemMeta().hasLore() || !metadatable.hasMetadata(LEAVE_ITEM_KEY)) {
					continue;
				}
				
				inv.setItem(i, null);
			}
			
			bukkitPlayer.updateInventory();
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		if (!game.isIngame(player)) {
			return;
		}
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		MetadatableItemStack clicked = new MetadatableItemStack(player.getBukkitPlayer().getItemInHand());
		if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasLore() || !clicked.hasMetadata(LEAVE_ITEM_KEY)) {
			return;
		}
		
		game.requestLose(player, QuitCause.SELF);
	}

}