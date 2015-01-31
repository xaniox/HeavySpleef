package de.matzefratze123.heavyspleef.core.hook;

import net.milkbowl.vault.economy.Economy;

public class VaultHook extends DefaultHook {

	public VaultHook(String pluginName) {
		super(pluginName);
	}
	
	public Economy getEconomy() {
		return getService(Economy.class);
	}

}
