package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import net.milkbowl.vault.economy.Economy;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameWinEvent;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.Hooks;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

public class FlagReward extends IntegerFlag {

	private Economy economy;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a money reward given to all winners");
	}
	
	@Override
	public boolean canBeSet() {
		HookManager manager = getHeavySpleef().getHookManager();
		return manager.getHook(Hooks.VAULT).isProvided();
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
			economy = manager.getHook(Hooks.VAULT).getService(Economy.class);
		}
		
		return economy;
	}

}