/*
 * This file is part of addons.
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
package de.xaniox.joingui;

import de.xaniox.heavyspleef.addon.java.BasicAddOn;
import de.xaniox.heavyspleef.commands.SpleefCommandManager;
import de.xaniox.heavyspleef.commands.base.CommandManagerService;
import de.xaniox.heavyspleef.commands.base.proxy.Filter;
import de.xaniox.heavyspleef.commands.base.proxy.ProxyExecution;
import de.xaniox.heavyspleef.commands.base.proxy.ProxyPriority;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.HeavySpleef.GamesLoadCallback;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.Unregister;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.FlagInit;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.flag.presets.ItemStackFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Flag(name = "join-item")
public class FlagJoinItem extends ItemStackFlag {

	private static ProxyExecution joinExecution;
	private static JoinInventory joinInventory;
	private static CommandProxy joinProxy;

    private static ProxyExecution spectateExecution;
    private static SpectateInventory spectateInventory;
    private static CommandProxy spectateProxy;
	
	@Inject
	private static JoinGuiAddOn addOn;
	
	@FlagInit
	public static void injectCommandProxy(final HeavySpleef heavySpleef) {
		GamesLoadCallback callback = new GamesLoadCallback() {
			
			@Override
			public void onGamesLoaded(List<Game> games) {
				SpleefCommandManager manager = (SpleefCommandManager) heavySpleef.getCommandManager();
				CommandManagerService service = manager.getService();
				
				joinInventory = new JoinInventory(addOn, addOn.getJoinInventoryEntryConfig());
				heavySpleef.getGlobalEventBus().registerListener(joinInventory);
				
				joinProxy = new JoinCommandProxy(joinInventory, addOn);
				
				joinExecution = ProxyExecution.inject(service, "spleef/join");
				joinExecution.attachProxy(joinProxy);


                spectateInventory = new SpectateInventory(addOn, addOn.getSpectateInventoryEntryConfig());
                heavySpleef.getGlobalEventBus().registerListener(spectateInventory);

                spectateProxy = new SpectateCommandProxy(spectateInventory, addOn);

                spectateExecution = ProxyExecution.inject(service, "spleef/spectate");
                spectateExecution.attachProxy(spectateProxy);
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
		if (joinExecution != null) {
			joinExecution.unattachProxy(joinProxy);
		}

        if (spectateExecution != null) {
            spectateExecution.unattachProxy(spectateProxy);
        }
		
		if (joinInventory != null) {
			heavySpleef.getGlobalEventBus().unregister(joinInventory);
		}

        if (spectateInventory != null) {
            heavySpleef.getGlobalEventBus().unregister(spectateInventory);
        }
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Sets the item displayed in the join gui for a game");
	}
	
	@Override
	public void setValue(ItemStack value) {
		super.setValue(value);
		
		if (joinInventory != null) {
			joinInventory.update();
		}

        if (spectateInventory != null) {
            spectateInventory.update();
        }
	}

    @Filter("spleef/join")
    @ProxyPriority(ProxyPriority.Priority.HIGH)
    private static class JoinCommandProxy extends CommandProxy {

        public JoinCommandProxy(GameInventory inventory, BasicAddOn addOn) {
            super(inventory, addOn, Permissions.PERMISSION_JOIN);
        }

    }

    @Filter("spleef/spectate")
    @ProxyPriority(ProxyPriority.Priority.HIGH)
    private static class SpectateCommandProxy extends CommandProxy {

        public SpectateCommandProxy(GameInventory inventory, BasicAddOn addOn) {
            super(inventory, addOn, Permissions.PERMISSION_SPECTATE);
        }

    }

}