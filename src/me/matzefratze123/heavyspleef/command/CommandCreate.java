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
import me.matzefratze123.heavyspleef.selection.SelectionManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCreate extends HSCommand {

	public CommandCreate() {
		setMaxArgs(4);
		setMinArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.CREATE_GAME.getPerm());
		setUsage("/spleef create <name> <cuboid|cylinder <radius> <height>>");
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
			//TODO WorldEdit check
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
			
		} else if (args[1].equalsIgnoreCase("cuboid") || args[1].equalsIgnoreCase("cub")) {
			//Create a new cuboid game
			SelectionManager selManager = HeavySpleef.instance.getSelectionManager();
			if (!selManager.hasSelection(player) || selManager.getFirstSelection(player) == null || selManager.getSecondSelection(player) == null) {
				player.sendMessage(_("needSelection"));
				return;
			}
			if (selManager.isTroughWorlds(player)) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			
			for (Game game : GameManager.getGames()) {
				if (game.contains(selManager.getFirstSelection(player)) || game.contains(selManager.getSecondSelection(player))) {
					player.sendMessage(_("arenaCantBeInsideAnother"));
					return;
				}
			}
			
			GameManager.createCuboidGame(args[0].toLowerCase(), selManager.getFirstSelection(player), selManager.getSecondSelection(player));
		}
		player.sendMessage(_("gameCreated"));
	}

}
