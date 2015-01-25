package de.matzefratze123.heavyspleef.core.hook;

import org.bukkit.plugin.Plugin;

public interface Hook {
	
	public String getName();
	
	public Plugin getPlugin();
	
	public boolean isProvided();
	
	public <T> T getService(Class<T> registrationClass);
	
}
