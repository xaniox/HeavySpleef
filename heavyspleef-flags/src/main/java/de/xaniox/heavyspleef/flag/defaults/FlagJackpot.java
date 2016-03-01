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
import de.xaniox.heavyspleef.core.event.PlayerWinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import net.milkbowl.vault.economy.Economy;

import java.util.List;

@Flag(name = "jackpot", parent = FlagEntryFee.class, depend = HookReference.VAULT)
public class FlagJackpot extends BaseFlag {

	private int playerCount;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines wether the sum of all entry fees is given to the winner");
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		// Save the count of players
		playerCount = event.getGame().getPlayers().size();
	}
	
	@Subscribe
	public void onPlayerWin(PlayerWinGameEvent event) {
		SpleefPlayer[] winners = event.getWinners();
		FlagEntryFee flagEntryFee = (FlagEntryFee) getParent();
		
		Economy economy = flagEntryFee.getEconomy();
		double jackpot = flagEntryFee.getValue() * playerCount;
		
		double amountPerWinner = jackpot / winners.length;
		for (SpleefPlayer winner : winners) {
			economy.depositPlayer(winner.getBukkitPlayer(), amountPerWinner);
			winner.sendMessage(getI18N().getVarString(Messages.Player.PLAYER_RECEIVE_JACKPOT)
					.setVariable("amount", economy.format(amountPerWinner))
					.toString());
		}
	}

}