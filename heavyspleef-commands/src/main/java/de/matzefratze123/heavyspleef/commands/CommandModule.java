package de.matzefratze123.heavyspleef.commands;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;

public class CommandModule extends SimpleModule {

	public CommandModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		// Bukkit keeps a reference of the CommandManagerService
		CommandManager manager = new CommandManager(getHeavySpleef());
		manager.init();
	}

	@Override
	public void disable() {}

}
