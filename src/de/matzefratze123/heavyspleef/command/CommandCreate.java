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
package de.matzefratze123.heavyspleef.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;

public class CommandCreate extends HSCommand {

	public CommandCreate() {
		setMinArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.CREATE_GAME);
		setUsage("/spleef create <name> cuboid\n" +
		         "Creates a new cuboid game within your selection\n\n" + 
				 "/spleef create <name> cylinder <radius> <height>\n" +
				 "Creates a new cylinder arena with the radius and height\n\n" +
				 "/spleef create <name> ellipse <radiusEastWest> <radiusNorthSouth> <height>\n" +
				 "Creates a new ellipse game with the given two radians and the height");
		setTabHelp(new String[]{"<name> cuboid", "<name> cylinder <radius> <height>", "<name> ellipse <radiusEastWest> <radiusNorthSouth> <height>"});
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaAlreadyExists"));
			return;
		}
		
		if (args[1].equalsIgnoreCase("cylinder") || args[1].equalsIgnoreCase("cyl")) {
			//Create a new cylinder game
			if (!HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).hasHook()) {
				player.sendMessage(_("noWorldEdit"));
				return;
			}
			if (args.length < 4) {
				player.sendMessage(getUsage());
				return;
			}
			for (Game game : GameManager.getGames()) {
				if (game.contains(player.getLocation())) {
					player.sendMessage(_("arenaCantBeInsideAnother"));
					return;
				}
			}
			try {
				int radius = Integer.parseInt(args[2]);
				int height = Integer.parseInt(args[3]);
				
				Location center = player.getLocation();
				
				int minY = center.getBlockY();
				int maxY = center.getBlockY() + height;
				
				GameManager.createCylinderGame(args[0].toLowerCase(), center, radius, minY, maxY);
			} catch (NumberFormatException e) {
				player.sendMessage(_("notANumber", args[2]));
				return;
			}
			
			player.sendMessage(_("gameCreated"));
		} else if (args[1].equalsIgnoreCase("oval") || args[1].equalsIgnoreCase("ellipse"))  {
			//Create a new ellipse game
			if (!HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).hasHook()) {
				player.sendMessage(_("noWorldEdit"));
				return;
			}
			if (args.length < 5) {
				player.sendMessage(getUsage());
				return;
			}
			for (Game game : GameManager.getGames()) {
				if (game.contains(player.getLocation())) {
					player.sendMessage(_("arenaCantBeInsideAnother"));
					return;
				}
			}
			try {
				int radiusEastWest = Integer.parseInt(args[3]);
				int radiusNorthSouth = Integer.parseInt(args[2]);
				int height = Integer.parseInt(args[4]);
				
				Location center = player.getLocation();
				
				int minY = center.getBlockY();
				int maxY = center.getBlockY() + height;
				
				GameManager.createCylinderGame(args[0].toLowerCase(), center, radiusEastWest, radiusNorthSouth, minY, maxY);
			} catch (NumberFormatException e) {
				player.sendMessage(_("notANumber", args[2]));
				return;
			}
			
			player.sendMessage(_("gameCreated"));
		} else if (args[1].equalsIgnoreCase("cuboid") || args[1].equalsIgnoreCase("cub")) {
			//Create a new cuboid game
			Selection s = HeavySpleef.getInstance().getSelectionManager().getSelection(player);
			if (!s.has()) {
				player.sendMessage(_("needSelection"));
				return;
			}
			if (s.isTroughWorlds()) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			
			for (Game game : GameManager.getGames()) {
				if (game.contains(s.getFirst()) || game.contains(s.getSecond())) {
					player.sendMessage(_("arenaCantBeInsideAnother"));
					return;
				}
			}
			
			GameManager.createCuboidGame(args[0].toLowerCase(), s.getFirst(), s.getSecond());
			player.sendMessage(_("gameCreated"));
		} else {
			player.sendMessage(_("unknownSpleefType"));
		}
	}

}
