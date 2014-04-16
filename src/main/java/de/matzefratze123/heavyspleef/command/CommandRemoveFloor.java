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

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandRemoveFloor implements CommandListener {

	@Command(value = "removefloor", onlyIngame = true)
	@CommandPermissions(value = { Permissions.REMOVE_FLOOR })
	@CommandHelp(usage = "/spleef removefloor", description = "Removes a floor from a game where you are currently looking")
	public void execute(Player player) {
		Block block = player.getTargetBlock(null, 50);
		if (block == null) {
			player.sendMessage(_("notLookingAtABlock"));
			return;
		}

		for (Game game : GameManager.getGames()) {
			if (game.getType() == GameType.CYLINDER && !HookManager.getInstance().getService(WorldEditHook.class).hasHook())
				continue;
			for (IFloor floor : game.getComponents().getFloors()) {
				if (floor.contains(block.getLocation())) {
					int id = floor.getId();
					game.getComponents().removeFloor(id);
					player.sendMessage(_("floorRemoved"));
					return;
				}
			}
		}

		player.sendMessage(_("notLookingAtFloor"));
	}

}
