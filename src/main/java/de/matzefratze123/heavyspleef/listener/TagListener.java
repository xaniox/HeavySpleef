/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
package de.matzefratze123.heavyspleef.listener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.TagAPIHook;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class TagListener implements Listener {

	private static ConcurrentMap<SpleefPlayer, ChatColor>	tags	= new ConcurrentHashMap<SpleefPlayer, ChatColor>();

	public static void setTag(SpleefPlayer player, ChatColor tag) {
		if (!HookManager.getInstance().getService(TagAPIHook.class).hasHook()) {
			return;
		}

		if (tag == null) {
			tags.remove(player);
		} else {
			tags.put(player, tag);
		}

		TagAPI.refreshPlayer(player.getBukkitPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNameTagReceive(AsyncPlayerReceiveNameTagEvent e) {
		SpleefPlayer namedPlayer = HeavySpleef.getInstance().getSpleefPlayer(e.getNamedPlayer());

		if (!tags.containsKey(namedPlayer)) {
			return;
		}

		String tag = tags.get(namedPlayer) + namedPlayer.getRawName();
		if (tag.length() > 16)
			tag = tag.substring(0, 16);

		e.setTag(tag);
	}

}
