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
package me.matzefratze123.heavyspleef.listener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

public class TagListener implements Listener {
	
	private static Map<String, ChatColor> tags = new HashMap<String, ChatColor>();
	
	public static void setTag(Player player, ChatColor tag) {
		if (tag == null) {
			tags.remove(player.getName());
		} else {
			tags.put(player.getName(), tag);
		}
		
		TagAPI.refreshPlayer(player);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNameTagReceive(PlayerReceiveNameTagEvent e) {
		if (!tags.containsKey(e.getNamedPlayer().getName())) {
			return;
		}
		
		String tag = tags.get(e.getNamedPlayer().getName()) + e.getNamedPlayer().getName();
		if (tag.length() > 16)
			tag = tag.substring(0, 16);
		
		e.setTag(tag);
	}
	
}
