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
package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.selection.Selection;
import me.matzefratze123.heavyspleef.util.Permissions;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddLose extends HSCommand {

	public CommandAddLose() {
		setMaxArgs(1);
		setMinArgs(1);
		setPermission(Permissions.ADD_LOSEZONE);
		setOnlyIngame(true);
		setUsage("/spleef addlose <name>");
		setTabHelp(new String[]{"<arena>"});
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Selection s = HeavySpleef.instance.getSelectionManager().getSelection(player);
		
		if (!s.has()) {
			player.sendMessage(_("needSelection"));
			return;
		}
		if (s.isTroughWorlds()) {
			player.sendMessage(_("selectionCantTroughWorlds"));
			return;
		}
		
		Game g = GameManager.getGame(args[0]);
		Location loc1 = s.getFirst();
		Location loc2 = s.getSecond();
	
		int id = g.addLoseZone(loc1, loc2);
		player.sendMessage(_("loseZoneCreated", String.valueOf(id), g.getName(), String.valueOf(id)));
	}

}
