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

import de.xaniox.heavyspleef.core.event.GameStartEvent;
import de.xaniox.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.game.Game.JoinResult;
import de.xaniox.heavyspleef.core.hook.HookManager;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.DoubleFlag;
import net.milkbowl.vault.economy.Economy;

import java.util.List;

@Flag(name = "entry-fee", depend = HookReference.VAULT)
public class FlagEntryFee extends DoubleFlag {
	
	private Economy economy;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a fee that every player has to pay in order to play a Spleef game");
	}
	
	@Override
	public void validateInput(Double input) throws ValidationException {
		if (input <= 0d) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_ENTRY_FEE));
		}
	}

	public Economy getEconomy() {
		//Lazy initialization
		if (economy == null) {
			HookManager manager = getHeavySpleef().getHookManager();
			economy = manager.getHook(HookReference.VAULT).getService(Economy.class);
		}
		
		return economy;
	}
	
	@Subscribe
	public void onPlayerPreJoinGame(PlayerPreJoinGameEvent event) {
		//Check if the player has sufficient funds
		SpleefPlayer player = event.getPlayer();
		double amount = getValue();
		
		Economy economy = getEconomy();
		if (economy.has(player.getBukkitPlayer(), amount)) {
			return;
		}
		
		//Don't let this player join
		event.setJoinResult(JoinResult.PERMANENT_DENY);
		event.setMessage(getI18N().getVarString(Messages.Command.UNSUFFICIENT_FUNDS)
				.setVariable("amount", economy.format(amount))
				.toString());
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		double fee = getValue();
		Economy economy = getEconomy();
		
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			economy.withdrawPlayer(player.getBukkitPlayer(), fee);
			player.sendMessage(getI18N().getVarString(Messages.Player.PAID_ENTRY_FEE)
					.setVariable("amount", economy.format(fee))
					.toString());
		}
	}

}