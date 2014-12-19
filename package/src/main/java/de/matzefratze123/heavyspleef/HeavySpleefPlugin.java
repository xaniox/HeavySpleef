package de.matzefratze123.heavyspleef;

import org.bukkit.plugin.java.JavaPlugin;

import de.matzefratze123.heavyspleef.commands.CommandModule;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.module.Module;
import de.matzefratze123.heavyspleef.flag.FlagModule;
import de.matzefratze123.heavyspleef.persistence.PersistenceModule;

public class HeavySpleefPlugin extends JavaPlugin {

	private HeavySpleef heavySpleef;
	
	@Override
	public void onEnable() {
		heavySpleef = new HeavySpleef(this);
		heavySpleef.load();
		
		Module flagModule = new FlagModule(heavySpleef);
		Module commandModule = new CommandModule(heavySpleef);
		Module persistenceModule = new PersistenceModule(heavySpleef);
		
		heavySpleef.registerModule(flagModule);
		heavySpleef.registerModule(commandModule);
		heavySpleef.registerModule(persistenceModule);
		
		heavySpleef.enable();
	}
	
	@Override
	public void onDisable() {
		if (heavySpleef != null) {
			heavySpleef.disable(); 
		}
	}
	
}
