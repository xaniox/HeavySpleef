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

import java.util.List;

import org.bukkit.inventory.ItemStack;

import de.matzefratze123.heavyspleef.commands.SpleefCommandManager;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyExecution;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.HeavySpleef.GamesLoadCallback;
import de.matzefratze123.heavyspleef.core.Unregister;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagInit;
import de.matzefratze123.heavyspleef.core.flag.Inject;

@Flag(name = "join-item")
public class FlagJoinItem extends SingleItemStackFlag {

	private static ProxyExecution execution;
	private static JoinInventory inventory;
	private static JoinCommandProxy proxy;
	
	@Inject
	private static JoinGuiAddOn addOn;
	
	@FlagInit
	public static void injectCommandProxy(final HeavySpleef heavySpleef) {
		GamesLoadCallback callback = new GamesLoadCallback() {
			
			@Override
			public void onGamesLoaded(List<Game> games) {
				SpleefCommandManager manager = (SpleefCommandManager) heavySpleef.getCommandManager();
				CommandManagerService service = manager.getService();
				
				inventory = new JoinInventory(addOn);
				heavySpleef.getGlobalEventBus().registerGlobalListener(inventory);
				
				proxy = new JoinCommandProxy(inventory);
				
				execution = ProxyExecution.inject(service, "spleef/join");
				execution.attachProxy(proxy);
			}
		};
		
		if (heavySpleef.isGamesLoaded()) {
			callback.onGamesLoaded(null);
		} else {
			heavySpleef.addGamesLoadCallback(callback);
		}
	}
	
	@Unregister
	public static void unattachCommandProxy(HeavySpleef heavySpleef) {
		execution.unattachProxy(proxy);
		heavySpleef.getGlobalEventBus().unregisterGlobalListener(inventory);
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Sets the item displayed in the join gui for a game");
	}
	
	@Override
	public void setValue(ItemStack value) {
		super.setValue(value);
		
		if (inventory != null) {
			inventory.update();
		}
	}

}
