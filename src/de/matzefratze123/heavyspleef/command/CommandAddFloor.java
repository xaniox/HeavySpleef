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
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameCylinder;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.region.FloorType;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Util;

public class CommandAddFloor extends HSCommand {

	public CommandAddFloor() {
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_FLOOR);
		setUsage("/spleef addfloor <randomwool|given|block[:subdata]>]");
		setTabHelp(new String[]{"randomwool", "given", "block[:subdata]"});
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Block block = player.getTargetBlock(null, 100);
		
		Selection s = HeavySpleef.getInstance().getSelectionManager().getSelection(player);
		Location loc1 = s.getFirst();
		Location loc2 = s.getSecond();
		
		if (args[0].equalsIgnoreCase("randomwool")) { //Wool floor
			if (HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).hasHook()) {
				for (Game game : GameManager.getGames()) {
					if (game.contains(player.getLocation()) && game.getType() == GameType.CYLINDER) {
						GameCylinder gameC = (GameCylinder)game; //Cast the game to a GameCylinder because we can be sure that it is one...
						
						Location center = gameC.getCenter();
							
						addWoolFloor(game, player, new Location(center.getWorld(), center.getBlockX(), player.getLocation().getBlockY(), center.getBlockZ()));
						return;
					}
				}
			}
			if (!s.has()) {
				player.sendMessage(_("needSelection"));
				return;
			}
			if (s.isTroughWorlds()) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			Game game = getFromLocation(loc1, loc2);
			if (game == null) {
				player.sendMessage(_("notInsideArena"));
				return;
			}
			
			addWoolFloor(game, player, loc1, loc2);
			return;
			
		} else if (args[0].equalsIgnoreCase("given")) { //Given floor
			if (HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).hasHook()) {
				for (Game game : GameManager.getGames()) {
					if (!game.contains(block))
						continue;
					if (game.getType() != GameType.CYLINDER) 
						continue;
					GameCylinder cylGame = (GameCylinder) game;
					Location center = cylGame.getCenter();
					
					addGivenFloor(game, player, new Location(center.getWorld(), center.getBlockX(), block.getLocation().getBlockY(), center.getBlockZ()));
					return;
				}
			} 
			
			if (!s.has()) {
				player.sendMessage(_("needSelection"));
				return;
			}
			if (s.isTroughWorlds()) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			Game game = getFromLocation(loc1, loc2);
			if (game == null) {
				player.sendMessage(_("notInsideArena"));
				return;
			}
			
			addGivenFloor(game, player, loc1, loc2);
			return;
		} else {//Specified floor!
			SimpleBlockData data = Util.getMaterialFromString(args[0], true);
			if (data == null) {
				player.sendMessage(_("invalidBlock"));
				return;
			}
			
			if (HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).hasHook()) {
				for (Game game : GameManager.getGames()) {
					if (!game.contains(player.getLocation()))
						continue;
					if (game.getType() != GameType.CYLINDER)
						continue;
					GameCylinder cylGame = (GameCylinder) game;
					Location center = cylGame.getCenter();
					
					addSpecifiedFloor(game, player, data, new Location(center.getWorld(), center.getBlockX(), player.getLocation().getBlockY(), center.getBlockZ()));
					return;
				}
			}
			if (!s.has()) {
				player.sendMessage(_("needSelection"));
				return;
			}
			if (s.isTroughWorlds()) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			Game game = getFromLocation(loc1, loc2);
			if (game == null) {
				player.sendMessage(_("notInsideArena"));
				return;
			}
			
			addSpecifiedFloor(game, player, data, loc1, loc2);
			return;
			
		}
	}
	
	private void addWoolFloor(Game game, Player p, Location... locations) {
		int id = game.addFloor(35, (byte)0, FloorType.RANDOMWOOL, locations);
		p.sendMessage(_("floorCreated", String.valueOf(id + 1)));
	}
	
	private void addSpecifiedFloor(Game game, Player p, SimpleBlockData data, Location... locations) {
		int id = game.addFloor(data.getMaterial().getId(), data.getData(), FloorType.SPECIFIEDID, locations);
		p.sendMessage(_("floorCreated", String.valueOf(id + 1)));
	}
	
	private void addGivenFloor(Game game, Player p, Location... locations) {
		int id = game.addFloor(0, (byte)0, FloorType.GIVENFLOOR, locations);
		p.sendMessage(_("floorCreated", String.valueOf(id + 1)));
	}
	
	private Game getFromLocation(Location... locations) {
		Game g = null;
		for (Game game : GameManager.getGames()) {
			boolean is = true;
			for (Location l : locations) {
				if (!game.contains(l))
					is = false;
			}
			
			if (is) {
				g = game;
				break;
			}
		}
		
		return g;
	}
	
} 
