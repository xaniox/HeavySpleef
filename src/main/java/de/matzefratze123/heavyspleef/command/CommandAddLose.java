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

import static de.matzefratze123.heavyspleef.util.I18N._;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandAddLose implements CommandListener {

	@Command(value = "addlose", minArgs = 1, onlyIngame = true)
	@CommandPermissions(value = { Permissions.ADD_LOSEZONE })
	@CommandHelp(usage = "/spleef addlose <game>", description = "Creates a new losezone based on your selection")
	public void execute(Player player, Game game) {
		if (game == null) {
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

		Location loc1 = s.getFirst();
		Location loc2 = s.getSecond();

		int id = 0;
		while (game.getComponents().hasLoseZone(id)) {
			id++;
		}

		LoseZone loseZone = new LoseZone(loc1, loc2, id);
		game.getComponents().addLoseZone(loseZone);

		player.sendMessage(_("loseZoneCreated", String.valueOf(id), game.getName(), String.valueOf(id)));
	}

}
