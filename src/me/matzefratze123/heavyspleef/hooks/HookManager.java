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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class HookManager {

	private Economy econ;
	private boolean hasVault = false;
	
	private WorldEditPlugin worldEdit;
	private boolean hasWorldEdit = false;
	
	public HookManager() {
		setupEconomy();
		setupWorldEdit();
	}
	
	private void setupEconomy() {
	       if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
	           return;
	       }
	       RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
	       if (rsp == null) {
	           return;
	       }
	       econ = rsp.getProvider();
	       hasVault = econ != null;
	}
	
	private void setupWorldEdit() {
		Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
		
		if (we == null)
			return;
		
		if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
			return;
		
		if (!(we instanceof WorldEditPlugin))
			return;
		
		worldEdit = (WorldEditPlugin) we;
		hasWorldEdit = true;
	}
	
	public boolean hasVault() {
		return this.hasVault;
	}
	
	public boolean hasWorldEdit() {
		return this.hasWorldEdit;
	}
	
	public WorldEditPlugin getWorldEdit() {
		return this.worldEdit;
	}
	
	public Economy getVaultEconomy() {
		return this.econ;
	}
	
}
