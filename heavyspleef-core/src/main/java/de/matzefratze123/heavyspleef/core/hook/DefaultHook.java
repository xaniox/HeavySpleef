/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class DefaultHook implements Hook {

	private final PluginManager pluginManager;
	protected final ServicesManager servicesManager;
	
	private String pluginName;
	
	public DefaultHook(String pluginName) {
		this.pluginManager = Bukkit.getPluginManager();
		this.servicesManager = Bukkit.getServicesManager();
		this.pluginName = pluginName;
	}
	
	@Override
	public String getName() {
		return pluginName;
	}
	
	@Override
	public Plugin getPlugin() {
		return pluginManager.getPlugin(pluginName);
	}

	@Override
	public boolean isProvided() {
		return pluginManager.isPluginEnabled(pluginName);
	}
	
	public <T> T getService(Class<T> service) {
		if (!isProvided()) {
			throw new IllegalStateException("Hook is not provided");
		}
		
		RegisteredServiceProvider<T> provider = servicesManager.getRegistration(service);
		if (provider == null) {
			throw new IllegalStateException("RegisteredServiceProvider is null for service " + service.getName());
		}
		
		return provider.getProvider();
	}

}
