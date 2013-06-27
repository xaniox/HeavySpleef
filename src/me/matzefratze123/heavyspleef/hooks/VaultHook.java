/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
