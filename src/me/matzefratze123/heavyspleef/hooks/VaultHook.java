package me.matzefratze123.heavyspleef.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class VaultHook implements Hook<Economy> {

	private Economy hook = null;
	
	@Override
	public void hook() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null)
			return;
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		
		if (rsp == null)
			return;
		
		hook = rsp.getProvider();
	}

	@Override
	public Economy getHook() {
		return this.hook;
	}

	@Override
	public boolean hasHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
		
		if (plugin == null)
			return false;
		if (!plugin.isEnabled())
			return false;
		
		if (hook == null)
			hook();
		if (hook == null)
			return false;
		return true;
	}

}
