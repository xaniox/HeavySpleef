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
package de.matzefratze123.heavyspleef.selection;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;

public class SelectionWorldEdit extends Selection {

	public SelectionWorldEdit(String owner) {
		super(owner);
	}

	@Override
	public Location getFirst() {
		WorldEditPlugin we = HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).getHook();
		Player player = Bukkit.getPlayer(owner);
		
		if (player == null)
			return null;
		if (!player.isOnline())
			return null;
		
		com.sk89q.worldedit.bukkit.selections.Selection s = we.getSelection(player);
		
		if (s == null)
			return null;
		
		return s.getMinimumPoint();
	}

	@Override
	public Location getSecond() {
		WorldEditPlugin we = HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).getHook();
		Player player = Bukkit.getPlayer(owner);
		
		if (player == null)
			return null;
		if (!player.isOnline())
			return null;
		
		com.sk89q.worldedit.bukkit.selections.Selection s = we.getSelection(player);
		
		if (s == null)
			return null;
		
		return s.getMaximumPoint();
	}

	@Override
	public boolean has() {
		WorldEditPlugin we = HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).getHook();
		Player player = Bukkit.getPlayer(owner);
		
		if (player == null)
			return false;
		if (!player.isOnline())
			return false;
		
		com.sk89q.worldedit.bukkit.selections.Selection s = we.getSelection(player);
		
		if (s == null)
			return false;
		
		return s.getMaximumPoint() != null && s.getMinimumPoint() != null;
	}

	@Override
	public boolean isTroughWorlds() {
		WorldEditPlugin we = HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).getHook();
		Player player = Bukkit.getPlayer(owner);
		
		if (player == null)
			return false;
		if (!player.isOnline())
			return false;
		
		com.sk89q.worldedit.bukkit.selections.Selection s = we.getSelection(player);
		
		if (s == null)
			return false;
		
		return s.getMinimumPoint().getWorld() != s.getMaximumPoint().getWorld();
	}
	
}
