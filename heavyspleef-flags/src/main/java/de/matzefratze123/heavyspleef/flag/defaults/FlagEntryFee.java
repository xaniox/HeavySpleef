package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.plugin.ServicesManager;

import net.milkbowl.vault.economy.Economy;
import de.matzefratze123.heavyspleef.flag.presets.DoubleFlag;

public class FlagEntryFee extends DoubleFlag {
	
	private Economy economy;
	
	@Override
	public void getDescription(List<String> description) {
		
	}
	
	@Override
	public boolean canBeSet() {
		ServicesManager manager = getHeavySpleef().getPlugin().getServer().getServicesManager();
		if (!manager.isProvidedFor(Economy.class)) {
			return false;
		}
		
		return super.canBeSet();
	}

	public Economy getEconomy() {
		//Lazy initialization
		if (economy == null) {
			//TODO
		}
		
		return null;
	}

}
