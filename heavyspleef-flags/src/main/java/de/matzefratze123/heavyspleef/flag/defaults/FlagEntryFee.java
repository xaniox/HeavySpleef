package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import net.milkbowl.vault.economy.Economy;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.Hooks;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.DoubleFlag;

public class FlagEntryFee extends DoubleFlag {
	
	private Economy economy;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a fee that every player has to pay in order to play a Spleef game");
	}
	
	@Override
	public boolean canBeSet() {
		HookManager manager = getHeavySpleef().getHookManager();
		return manager.getHook(Hooks.VAULT).isProvided();
	}

	public Economy getEconomy() {
		//Lazy initialization
		if (economy == null) {
			HookManager manager = getHeavySpleef().getHookManager();
			economy = manager.getHook(Hooks.VAULT).getService(Economy.class);
		}
		
		return economy;
	}
	
	@GameListener
	public void onGameStart(GameStartEvent event) {
		double fee = getValue();
		
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			getEconomy().withdrawPlayer(player.getBukkitPlayer(), fee);
			player.sendMessage(null); //TODO: add messaage
		}
	}

}
