/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class WorldEditHook implements Hook<WorldEditPlugin> {

	private WorldEditPlugin	hook	= null;

	@Override
	public void hook() {
		Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");

		if (we == null) {
			return;
		}

		if (!we.isEnabled()) {
			return;
		}

		if (!(we instanceof WorldEditPlugin))
			return;

		hook = (WorldEditPlugin) we;
	}

	@Override
	public WorldEditPlugin getHook() {
		if (hook == null) {
			hook();
		}

		return this.hook;
	}

	@Override
	public boolean hasHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");

		if (plugin == null)
			return false;
		if (!plugin.isEnabled())
			return false;
		if (!(plugin instanceof WorldEditPlugin))
			return false;

		if (hook == null)
			hook();
		return true;
	}

}
