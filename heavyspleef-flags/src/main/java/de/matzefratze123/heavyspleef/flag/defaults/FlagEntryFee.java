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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import net.milkbowl.vault.economy.Economy;
import de.matzefratze123.heavyspleef.core.event.GameEventHandler;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.DoubleFlag;

@Flag(name = "entry-fee", depend = HookReference.VAULT)
public class FlagEntryFee extends DoubleFlag {
	
	private Economy economy;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a fee that every player has to pay in order to play a Spleef game");
	}

	public Economy getEconomy() {
		//Lazy initialization
		if (economy == null) {
			HookManager manager = getHeavySpleef().getHookManager();
			economy = manager.getHook(HookReference.VAULT).getService(Economy.class);
		}
		
		return economy;
	}
	
	@GameEventHandler
	public void onGameStart(GameStartEvent event) {
		double fee = getValue();
		
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			getEconomy().withdrawPlayer(player.getBukkitPlayer(), fee);
			player.sendMessage(getI18N().getVarString(Messages.Player.PAID_ENTRY_FEE)
					.setVariable("amount", getEconomy().format(fee))
					.toString());
		}
	}

}
