/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.kitteh.tag.TagAPI;

public class TagAPIHook implements Hook<TagAPI> {

	private TagAPI hook;
	
	@Override
	public void hook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("TagAPI");
		
		if (plugin == null)
			return;
		if (!plugin.isEnabled())
			return;
		if (!(plugin instanceof TagAPI))
			return;
		
		TagAPI api = (TagAPI)plugin;
		hook = api;
	}

	@Override
	public TagAPI getHook() {
		if (hook == null) {
			hook();
		}
		
		return this.hook;
	}

	@Override
	public boolean hasHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("TagAPI");
		
		if (plugin == null)
			return false;
		if (!plugin.isEnabled())
			return false;
		if (!(plugin instanceof TagAPI))
			return false;
		
		if (hook == null)
			hook();
		
		return true;
	}
	
	
	
}
