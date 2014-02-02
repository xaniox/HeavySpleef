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

import org.bukkit.ChatColor;
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
import de.matzefratze123.heavyspleef.core.GameCuboid;
import de.matzefratze123.heavyspleef.core.GameCylinder;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.RegionCylinder;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandCreate implements CommandListener {
	
	private static final int INVALID_REGION_ID = -1;
	
	@Command(value = "create", minArgs = 2, onlyIngame = true)
	@CommandPermissions(value = {Permissions.CREATE_GAME})
	@CommandHelp(usage = "/spleef create <name> cuboid\n" +
				 "/spleef create <name> cylinder <radius> <height>", description = "Creates a new spleef game")
	public void execute(Player player, String name, String type, Integer radius, Integer height) {
		if (GameManager.hasGame(name)) {
			player.sendMessage(_("arenaAlreadyExists"));
			return;
		}
		
		if (type.equalsIgnoreCase("cylinder") || type.equalsIgnoreCase("cyl")) {
			//Create a new cylinder game
			if (radius == null || height == null) {
				player.sendMessage(ChatColor.RED + "Please enter a radius and the height of your arena");
				return;
			}
		
			Location center = player.getLocation();
			
			int minY = center.getBlockY();
			int maxY = center.getBlockY() + height;
			
			RegionCylinder region = new RegionCylinder(INVALID_REGION_ID, center, radius, minY, maxY);
			Game game = new GameCylinder(name, region);
			GameManager.addGame(game);
		
			player.sendMessage(_("gameCreated"));
		} else if (type.equalsIgnoreCase("cuboid") || type.equalsIgnoreCase("cub")) {
			//Create a new cuboid game
			Selection s = HeavySpleef.getInstance().getSelectionManager().getSelection(player);
			if (!s.hasSelection()) {
				player.sendMessage(_("needSelection"));
				return;
			}
			if (s.isTroughWorlds()) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			
			RegionCuboid region = new RegionCuboid(INVALID_REGION_ID, s.getFirst(), s.getSecond());
			Game game = new GameCuboid(name, region);
			
			GameManager.addGame(game);
			player.sendMessage(_("gameCreated"));
		} else {
			player.sendMessage(_("unknownSpleefType"));
		}
	}

}
