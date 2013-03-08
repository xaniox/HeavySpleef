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
import me.matzefratze123.heavyspleef.core.GameCylinder;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Type;
import me.matzefratze123.heavyspleef.selection.SelectionManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddFloor extends HSCommand {

	public CommandAddFloor() {
		setMaxArgs(1);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_FLOOR.getPerm());
		setUsage("/spleef addfloor [Block-ID[:DATA]|given]");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Block block = player.getTargetBlock(null, 100);
		
		
		int blockID = 0;
		byte data = 0;
		
		if (args.length == 1 && args[0].equalsIgnoreCase("given"))
			blockID = -1;
		
		if (blockID == -1) {
			for (Game game : GameManager.getGames()) {
				if (!game.contains(block))
					continue;
				if (game.getType() != Type.CYLINDER)
					continue;
				GameCylinder cylGame = (GameCylinder) game;
				Location center = cylGame.getCenter();
				
				game.addFloor(blockID, data, false, true, new Location(center.getWorld(), center.getBlockX(), block.getLocation().getBlockY(), center.getBlockZ()));
				player.sendMessage(_("floorCreated"));
				return;
			}
		}
		//First check if the arguments are longer then one and get the blockID and data...
		else if (args.length == 1) {
			
			try {
				String[] split = args[0].split(":"); 
				
				blockID = Integer.parseInt(split[0]);
				if (blockID == 0) {
					player.sendMessage(_("cantConsistOfAir"));
					return;
				}
				Material mat = Material.getMaterial(blockID);
				if (mat == null) {
					player.sendMessage(_("invalidBlockID"));
					return;
				}
				
				if (split.length > 1) {
					data = Byte.parseByte(split[1]);
					if (data > Byte.MAX_VALUE || data < Byte.MIN_VALUE) {
						player.sendMessage(_("toBigData"));
						return;
					}
				}
			} catch (NumberFormatException e) {
				player.sendMessage(_("blockIDIsntNumber"));
				return;
			}
		}
		
		for (Game game : GameManager.getGames()) {
			if (game.contains(player.getLocation()) && game.getType() == Type.CYLINDER) {
				boolean given = blockID == -1;
				boolean wool = args.length < 1;
				
				if ((given && wool)) //A floor can't be wool and given. That would interrupt the floor...
					return;
				GameCylinder gameC = (GameCylinder)game; //Cast the game to a GameCylinder because we can be sure that it is one...
				
				Location center = gameC.getCenter();
						
				game.addFloor(blockID, data, wool, given, new Location(center.getWorld(), center.getBlockX(), player.getLocation().getBlockY(), center.getBlockZ()));
				player.sendMessage(_("floorCreated"));
				return;
			}
		}
		//If the player does not stand inside a cylinder game, we are going to check for cuboid regions...
		
		SelectionManager selManager = HeavySpleef.instance.getSelectionManager();
		if (!selManager.hasSelection(player) || selManager.getFirstSelection(player) == null || selManager.getSecondSelection(player) == null) {
			player.sendMessage(_("needSelection"));
			return;
		}
		if (selManager.isTroughWorlds(player)) {
			player.sendMessage(_("selectionCantTroughWorlds"));
			return;
		}
		Game g = null;
		Location loc1 = selManager.getFirstSelection(player);
		Location loc2 = selManager.getSecondSelection(player);
		
		
		for (Game game : GameManager.getGames()) {
			if (game.contains(loc1) && game.contains(loc2))
				g = game;
		}
		if (g == null) {
			player.sendMessage(_("notInsideArena"));
			return;
		}
		
		if (blockID == -1) {
			g.addFloor(0, (byte)0, false, true, loc1, loc2);
			player.sendMessage(_("floorCreated"));
			return;
		}
		
		if (blockID > 0)
			g.addFloor(blockID, data, false, false, loc1, loc2);
		g.addFloor(35, (byte)0, true, false, loc1, loc2);
		player.sendMessage(_("floorCreated"));
	}

}
