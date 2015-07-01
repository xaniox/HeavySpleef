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
package de.matzefratze123.joingui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.JoinRequester;
import de.matzefratze123.heavyspleef.core.JoinRequester.JoinValidationException;
import de.matzefratze123.heavyspleef.core.event.GameCountdownChangeEvent;
import de.matzefratze123.heavyspleef.core.event.GameRenameEvent;
import de.matzefratze123.heavyspleef.core.event.GameStateChangeEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.SpleefListener;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.inventoryguilib.GuiInventory;
import de.matzefratze123.inventoryguilib.GuiInventorySlot;

public class JoinInventory extends GuiInventory implements SpleefListener {
	
	private static final GameNameComparator COMPARATOR = new GameNameComparator();
	private final HeavySpleef heavySpleef;
	private final I18N i18n;
	private final GameManager gameManager;
	private List<Game> recentRegisteredGames;
	private InventoryEntryConfig config;
	
	public JoinInventory(JoinGuiAddOn addOn) {
		super(addOn.getHeavySpleef().getPlugin());
		
		this.heavySpleef = addOn.getHeavySpleef();
		this.i18n = addOn.getI18n();
		this.gameManager = addOn.getHeavySpleef().getGameManager();
		this.config = addOn.getInventoryEntryConfig();
		
		init(null);
	}
	
	public void init(List<Game> games) {
		if (games == null) {
			games = Lists.newArrayList(gameManager.getGames());
			Collections.sort(games, COMPARATOR);
		}
		
		setTitle(i18n.getString(de.matzefratze123.joingui.Messages.INVENTORY_TITLE));
		setLines((int) Math.ceil(games.size() / 9D));
		
		int x = 0;
		int y = 0;
		Iterator<Game> iterator = games.iterator();
		
		while (iterator.hasNext()) {
			Game game = iterator.next();
			
			GuiInventorySlot slot = getSlot(x++, y);
			placeGame(slot, game);
			
			if (x >= SLOTS_PER_LINE) {
				x = 0;
				++y;
			}
		}
	}
	
	private void placeGame(GuiInventorySlot slot, Game game) {
		slot.setValue(game);
		
		ItemStack stack;
		
		if (game.isFlagPresent(FlagJoinItem.class)) {
			FlagJoinItem flag = game.getFlag(FlagJoinItem.class);
			
			stack = flag.getValue().clone();
		} else {
			stack = new ItemStack(Material.DIAMOND_SPADE);
		}
		
		slot.setItem(stack);
		updateSlot(slot, game);
	}
	
	public void update() {
		List<Game> games = Lists.newArrayList(gameManager.getGames());
		Collections.sort(games, COMPARATOR);
		
		if (!games.equals(recentRegisteredGames)) {
			//Games have been added or deleted
			//Clear and wipe the entire inventory
			clearInventory();
			//Re-initialize it
			init(games);
			
			recentRegisteredGames = games;
		} else {
			//Nothing has been changed, just update already existent slots
			for (int x = 0; x < SLOTS_PER_LINE; x++) {
				for (int y = 0; y < getLines(); y++) {
					GuiInventorySlot slot = getSlot(x, y);
					
					Object value = slot.getValue();
					if (value == null || !(value instanceof Game)) {
						continue;
					}
					
					Game game = (Game) value;
					
					//Actually update the itemstack with the game data
					updateSlot(slot, game);
				}
			}
		}
		
		updateViews();
	}
	
	@Override
	public void open(Player player) {
		update();
		
		super.open(player);
	}
	
	private void updateSlot(GuiInventorySlot slot, Game game) {
		InventoryEntryLayout layout = config.getLayout();
		layout.inflate(slot.getItem(), game);
	}
	
	private void clearInventory() {
		for (int x = 0; x < SLOTS_PER_LINE; x++) {
			for (int y = 0; y < getLines(); y++) {
				GuiInventorySlot slot = getSlot(x, y);
				clearSlot(slot);
			}
		}
	}
	
	private void clearSlot(GuiInventorySlot slot) {
		slot.setItem((ItemStack) null);
		slot.setValue(null);
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onGameRename(GameRenameEvent event) {
		update();
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event) {
		update();
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onPlayerJoinGame(PlayerJoinGameEvent event) {
		update();
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		update();
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onCountdownChange(GameCountdownChangeEvent event) {
		update();
	}

	@Override
	public void onClick(GuiClickEvent event) {
		event.setCancelled(true);
		
		GuiInventorySlot slot = event.getSlot();
		Object val = slot.getValue();
		
		if (!(val instanceof Game)) {
			return;
		}
		
		Game game = (Game) val;
		SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
		
		try {
			long timer = game.getJoinRequester().request(player, JoinRequester.QUEUE_PLAYER_CALLBACK);
			if (timer > 0) {
				player.sendMessage(i18n.getVarString(Messages.Command.JOIN_TIMER_STARTED)
						.setVariable("timer", String.valueOf(timer))
						.toString());
			}
		} catch (JoinValidationException e) {
			player.sendMessage(e.getMessage());
		}
	}
	
	private static class GameNameComparator implements Comparator<Game> {

		@Override
		public int compare(Game o1, Game o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	}

}
