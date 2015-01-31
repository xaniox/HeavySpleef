package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import net.milkbowl.vault.economy.Economy;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameWinEvent;
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
	public void onGameWin(GameWinEvent event) {
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