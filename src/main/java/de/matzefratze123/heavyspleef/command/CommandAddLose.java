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
package de.matzefratze123.heavyspleef.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandAddLose extends HSCommand {

	public CommandAddLose() {
		setMinArgs(1);
		setPermission(Permissions.ADD_LOSEZONE);
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0])) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Selection s = HeavySpleef.getInstance().getSelectionManager().getSelection(player);
		
		if (!s.hasSelection()) {
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
	
		int id = 0;
		while (g.getComponents().hasLoseZone(id)) {
			id++;
		}
		
		LoseZone loseZone = new LoseZone(loc1, loc2, id);
		g.getComponents().addLoseZone(loseZone);
		
		player.sendMessage(_("loseZoneCreated", String.valueOf(id), g.getName(), String.valueOf(id)));
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef addlose <game>");
		
		help.addHelp("Creates a new losezone based on your selection");
		
		return help;
	}

}
