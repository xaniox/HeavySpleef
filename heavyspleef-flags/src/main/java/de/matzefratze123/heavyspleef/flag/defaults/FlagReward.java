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
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "reward", depend = HookReference.VAULT)
public class FlagReward extends IntegerFlag {

	private Economy economy;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a money reward given to all winners");
	}
	
	@GameListener
	public void onGameWin(PlayerWinGameEvent event) {
		//Call the method to potentially initialize the economy plugin
		Economy economy = getEconomy();
		int depositValue = getValue();
		
		for (SpleefPlayer winner : event.getWinners()) {
			economy.depositPlayer(winner.getBukkitPlayer(), depositValue);
		}
	}
	
	private Economy getEconomy() {
		if (economy == null) {
			HookManager manager = getHeavySpleef().getHookManager();
			economy = manager.getHook(HookReference.VAULT).getService(Economy.class);
		}
		
		return economy;
	}

}