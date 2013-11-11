/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CylinderRegion;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameCylinder;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.region.FloorCuboid;
import de.matzefratze123.heavyspleef.core.region.FloorCylinder;
import de.matzefratze123.heavyspleef.core.region.FloorType;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.objects.RegionCylinder;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.ADMIN)
public class CommandAddFloor extends HSCommand {

	public CommandAddFloor() {
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_FLOOR);
		setUsage("/spleef addfloor <randomwool|given|block[:subdata]>]");
		setHelp("Adds a floor to the game");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Block looking = player.getTargetBlock(Util.getTransparentMaterials(), 100);
		
		Selection selection = HeavySpleef.getInstance().getSelectionManager().getSelection(player);
		
		Location firstPoint = selection.getFirst();
		Location secondPoint = selection.getSecond();
		
		Game foundGame = null;
		
		//Search games using player position and selection position
		for (Game game : GameManager.getGames()) {
			if (game.contains(player.getLocation()) || (firstPoint != null && game.contains(firstPoint) || (secondPoint != null && game.contains(secondPoint)))) {
				foundGame = game;
			}
		}
		
		if (foundGame == null) {
			//FIXME stehengeblieben...
			player.sendMessage(_("notInsideArena"));
			return;
		}
		
		SimpleBlockData blockData = null;
		FloorType type = null;
		Location[] locations = null;
		
		if (args[0].equalsIgnoreCase("randomwool")) {
			type = FloorType.RANDOMWOOL;
		} else if (args[0].equalsIgnoreCase("given")) {
			type = FloorType.GIVENFLOOR;
		} else {
			blockData = Util.parseMaterial(args[0], false);
			
			if (blockData == null) {
				//Failed to parse blockdata
				player.sendMessage(getUsage());
				return;
			}
			
			type = FloorType.SPECIFIEDID;
			
		}
		
		if (foundGame.getType() == GameType.CUBOID) {
			if (!selection.hasSelection()) {
				player.sendMessage(_("needSelection"));
				return;
			}
			
			if (selection.isTroughWorlds()) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			
			locations = new Location[2];
			
			locations[0] = firstPoint;
			locations[1] = secondPoint;
		} else if (foundGame.getType() == GameType.CYLINDER) {
			GameCylinder cylGame = (GameCylinder) foundGame;
			RegionCylinder region = (RegionCylinder)cylGame.getRegion();
			
			Location center = Util.toBukkitLocation(region.getWorldEditRegion().getWorld(), region.getWorldEditRegion().getCenter());
			
			locations = new Location[1];
			int y;
			
			if (type == FloorType.GIVENFLOOR) {
				y = looking.getLocation().getBlockY();
			} else {
				y = player.getLocation().getBlockY();
			}
			
			locations[0] = new Location(center.getWorld(), center.getBlockX(), y, center.getBlockZ());
		}
		
		addFloor(foundGame, player, type, blockData, locations);
	}
	
	private void addFloor(Game game, Player player, FloorType type, SimpleBlockData blockData, Location... locations) {
		Material material = Material.SNOW;
		byte data = (byte) 0;
		
		if (type == FloorType.RANDOMWOOL) {
			material = Material.WOOL;
		} else if (type == FloorType.SPECIFIEDID) {
			material = blockData.getMaterial();
			data = blockData.getData();
		}
		
		IFloor floor = null;
		int id = 0;
		
		while (game.getComponents().hasFloor(id)) {
			id++;
		}
		
		if (game.getType() == GameType.CUBOID) {
			floor = new FloorCuboid(id, locations[0], locations[1], type);
			floor.setBlockData(new SimpleBlockData(material, data));
		} else if (game.getType() == GameType.CYLINDER) {
			GameCylinder cylinderGame = (GameCylinder) game;
			RegionCylinder region = (RegionCylinder) cylinderGame.getRegion();
			CylinderRegion weRegion = region.getWorldEditRegion();
			
			Location floorCenter = new Location(BukkitUtil.toWorld(weRegion.getWorld()), weRegion.getCenter().getX(), locations[0].getBlockY(), weRegion.getCenter().getZ());
			
			floor = new FloorCylinder(id, floorCenter, weRegion.getRadius().getBlockX(), locations[0].getBlockY(), locations[0].getBlockY(), type);
			floor.setBlockData(new SimpleBlockData(material, data));
		}
		
		floor.generate();
		game.getComponents().addFloor(floor);
		player.sendMessage(_("floorCreated", String.valueOf(id + 1)));
	}
	
} 
