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

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Team;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class TagListener implements Listener {

	private Map<String, String> previousTag = new HashMap<String, String>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNameTag(PlayerReceiveNameTagEvent e) {
		Player player = e.getNamedPlayer();
		
		if (GameManager.isInAnyGame(player)) {
		
			Game game = GameManager.fromPlayer(player);
			Team team = game.getTeam(player);
			
			if (team == null)
				return;
			
			if (!previousTag.containsKey(player.getName()))
				previousTag.put(player.getName(), e.getTag());
			e.setTag(team.getColor() + player.getName());
		} else {
			e.setTag(previousTag.get(player.getName()));
			previousTag.remove(player.getName());
		}
	}
	
}
