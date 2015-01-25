package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.ServicesManager;

import com.google.common.collect.ImmutableSet;

import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.GameWinEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
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
	public void onPlayerWin(GameWinEvent event) {
		SpleefPlayer[] winners = event.getWinners();
		FlagEntryFee flagEntryFee = (FlagEntryFee) getParent();
		
		Economy economy = flagEntryFee.getEconomy();
		double jackpot = flagEntryFee.getValue() * playerCount;
		
		double amountPerWinner = jackpot / winners.length;
		
		
	}

}
