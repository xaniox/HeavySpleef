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
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = "jackpot", parent = FlagEntryFee.class)
public class FlagJackpot extends BooleanFlag {

	private int playerCount;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines wether the sum of all entry fees is given to the winner");
	}
	
	@GameListener
	public void onGameStart(GameStartEvent event) {
		// Save the count of players
		playerCount = event.getGame().getPlayers().size();
	}
	
	@GameListener
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
