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
package de.matzefratze123.heavyspleef.core;

import static de.matzefratze123.heavyspleef.core.flag.FlagType.ONEVSONE;
import static de.matzefratze123.heavyspleef.core.flag.FlagType.ROUNDS;

import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;

import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.database.DatabaseSerializeable;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class SignWall extends RegionCuboid implements DatabaseSerializeable {
	
	private Location[] locations;
	private Game game;
	
	public SignWall(Location firstCorner, Location secondCorner, Game game, int id) {
		super(id, firstCorner, secondCorner);
		
		this.game = game;
		
		calculateSigns(firstCorner, secondCorner);
		overrideOtherWalls();
		update();
	}
	
	protected void setGame(Game game) {
		this.game = game;
	}
	
	public static SignWall fromString(String fromString, Game game) {
		String[] parts = fromString.split(";");
		
		if (parts.length < 3)
			return null;
		
		int id = Integer.parseInt(parts[0]);
		Location loc1 = Parser.convertStringtoLocation(parts[1]);
		Location loc2 = Parser.convertStringtoLocation(parts[2]);
		
		return new SignWall(loc1, loc2, game, id);
	}
	
	//Removes all other walls that overlap this wall
	//Most users don't want to type /spleef removewall
	private void overrideOtherWalls() {
		
		//Loop around every game
		for (Game game : GameManager.getGames()) {
			//Loop around there walls
			for (SignWall wall : game.getComponents().getSignWalls()) {
				//Loop around the signs of the wall
				for (Location location : wall.locations) {
					//Loop around the signs of this wall
					for (Location thisLocation : this.locations) {
						//And check if it equals
						if (location.equals(thisLocation))
							game.getComponents().removeSignWall(wall.getId());//Remove the wall, because it overlaps an other
					}
				}
			}
		}
	}
	
	private void calculateSigns(Location... locations) {
		if (locations.length < 2)
			return;
		if (locations[0].getBlockY() != locations[1].getBlockY())
			return;
		
		int minX = Math.min(locations[0].getBlockX(), locations[1].getBlockX());
		int maxX = Math.max(locations[0].getBlockX(), locations[1].getBlockX());
		
		int minZ = Math.min(locations[0].getBlockZ(), locations[1].getBlockZ());
		int maxZ = Math.max(locations[0].getBlockZ(), locations[1].getBlockZ());
		Location[] signArray = null;
		
		int count = 0;
		if (minX == maxX) {//Wall trough Z!
			signArray = new Location[maxZ - minZ + 1];
			
			Block block = locations[0].getWorld().getBlockAt(minX, locations[0].getBlockY(), minZ);
			Sign sign = (Sign)block.getState();
			Block attachedBlock = getAttachedBlock(sign);
			
			int difference = block.getLocation().getBlockX() - attachedBlock.getLocation().getBlockX();
			
			int joinsign = minZ;
			int infosign = minZ + 1;
			
			if (difference == 1) {
				joinsign = maxZ;
				infosign = maxZ - 1;
			}
			
			int update = -difference;
			
			for (int z = difference == -1 ? minZ : maxZ; difference == -1 ? z <= maxZ : z >= minZ; z += update) {
				Block currentBlock = locations[0].getWorld().getBlockAt(minX, locations[0].getBlockY(), z);
				
				if (z == joinsign)
					signArray[0] = currentBlock.getLocation();
				else if (z == infosign) {
					signArray[1] = currentBlock.getLocation();
				} else {
					signArray[count] = currentBlock.getLocation();
				}
				
				count++;
			}
		} else if (minZ == maxZ) {//Wall trough X!
			signArray = new Location[maxX - minX + 1];
			
			Block block = locations[0].getWorld().getBlockAt(minX, locations[0].getBlockY(), minZ);
			Sign sign = (Sign)block.getState();
			Block attachedBlock = getAttachedBlock(sign);
			
			int difference = block.getLocation().getBlockZ() - attachedBlock.getLocation().getBlockZ();
			
			int joinsign = minX;
			int infosign = minX + 1;
			
			if (difference == -1) {
				joinsign = maxX;
				infosign = maxX - 1;
			}
			
			int update = difference;
			
			for (int x = difference == 1 ? minX : maxX; difference == 1 ? x <= maxX : x >= minX; x += update) {
				Block currentBlock = locations[0].getWorld().getBlockAt(x, locations[0].getBlockY(), minZ);
				
				if (x == joinsign)
					signArray[0] = currentBlock.getLocation();
				else if (x == infosign) {
					signArray[1] = currentBlock.getLocation();
				} else {
					signArray[count] = currentBlock.getLocation();
				}
				
				count++;
			}
		}
		
		this.locations = signArray;
	}
	
	public Sign[] getSignLocations() {
		Sign[] array = new Sign[locations.length];
				
		for (int i = 0; i < locations.length; i++) {
			if (!isSign(locations[i].getBlock()))
				continue;
			array[i] = (Sign) locations[i].getBlock().getState();
		}
		
		return array;
	}
	
	public void update() {
		if (game == null) {
			return;
		}
		
		List<SpleefPlayer> inPlayers = game.getIngamePlayers();
		List<OfflinePlayer> outPlayers = game.getOutPlayers();
		
		String infinity = new String("\u221E");
		String maxPlayers = String.valueOf(game.getFlag(FlagType.MAXPLAYERS) < 2 ? infinity : game.getFlag(FlagType.MAXPLAYERS));
		
		Sign joinSign = (Sign)locations[0].getBlock().getState();
		
		if (joinSign.getType() != Material.WALL_SIGN) {
			joinSign.setType(Material.WALL_SIGN);
		}
		
		joinSign.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
		joinSign.setLine(1, ChatColor.DARK_RED + "[Join]");
		joinSign.setLine(2, game.getName());
		joinSign.update();
		
		Sign infoSign = (Sign)locations[1].getBlock().getState();
		if (infoSign.getType() != Material.WALL_SIGN) {
			infoSign.setType(Material.WALL_SIGN);
		}
		
		infoSign.setLine(0, ChatColor.RED + game.getName());
		infoSign.setLine(1, game.getGameState().name());
		infoSign.setLine(2, inPlayers.size() + "/" + ChatColor.GRAY + outPlayers.size() + ChatColor.RESET + "/" + maxPlayers);
		boolean is1vs1 = game.getFlag(ONEVSONE);
		
		if (game.getGameState() == GameState.COUNTING)
			infoSign.setLine(3, ChatColor.DARK_RED + "Start in " + ChatColor.BOLD + game.getCountLeft());
		else if (is1vs1 && (game.getGameState() == GameState.INGAME || game.getGameState() == GameState.COUNTING)) {
			int rounds = game.getFlag(ROUNDS);
			infoSign.setLine(3, ChatColor.DARK_GREEN + "Round " + (game.getRoundsPlayed() + 1) + "/" + rounds);
		} else
			infoSign.setLine(3, "");
		
		infoSign.update();
		
		Iterator<SpleefPlayer> inIterator = inPlayers.iterator();
		Iterator<OfflinePlayer> outIterator = outPlayers.iterator();
		
		for (int i = 2; i < locations.length; i++) {
			for (int line = 0; line < 4; line++) {
				if (!isSign(locations[i].getBlock()))
						continue;
					
				Sign sign = (Sign)locations[i].getBlock().getState();
				
				if (inIterator.hasNext()) {
					SpleefPlayer player = inIterator.next();
					String name = player.getName();
					
					Team team = game.getComponents().getTeam(player);
					String prefix = team == null ? name.equalsIgnoreCase("matzefratze123") ? ChatColor.DARK_RED.toString() : "" : team.getColor().toString();
					sign.setLine(line, prefix + (name.equalsIgnoreCase("matzefratze123") ? "matzefratze" : prefix + name));
					sign.update();
				} else if (outIterator.hasNext()) {
					String name = outIterator.next().getName();
					
					if (name.length() > 15)
						name = name.substring(0, 15);
					sign.setLine(line, ChatColor.GRAY + name);
					sign.update();
				} else {
					sign.setLine(line, "");
					sign.update();
				}
			}
		}
	}

	@Override
	public boolean contains(Location l) {
		for (Location location : locations) {
			if (Parser.roundLocation(location).equals(Parser.roundLocation(l)))
				return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return getId() + ";" + Parser.convertLocationtoString(firstPoint) + ";" + Parser.convertLocationtoString(secondPoint); 
	}
	
	public static boolean oneCoordSame(Location loc1, Location loc2) {
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		if (minX == maxX)
			return true;
		if (minZ == maxZ)
			return true;
		
		return false;
	}
	
	public static int getDifference(Location loc1, Location loc2) {
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		if (minX == maxX)
			return maxZ - minZ + 1;
		if (minZ == maxZ)
			return maxX - minX + 1;
		
		return -1;
	}
	
	public static boolean isAllSign(Location loc1, Location loc2) {
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
		int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block block = loc1.getWorld().getBlockAt(x, y, z);
					if (block.getType() != Material.WALL_SIGN)
						return false;
				}
			}
		}
		
		return true;
	}
	
	public static Block getAttachedBlock(Sign sign) {
		org.bukkit.material.Sign s = new org.bukkit.material.Sign(sign.getType(), sign.getData().getData());
		BlockFace attachedFace = s.getAttachedFace();
		
		return sign.getBlock().getRelative(attachedFace);
	}
	
	public static boolean isSign(Block block) {
		return block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST;
	}

	@Override
	public ConfigurationSection serialize() {
		MemorySection section = new MemoryConfiguration();
		
		section.set("id", id);
		section.set("first", Parser.convertLocationtoString(firstPoint));
		section.set("second", Parser.convertLocationtoString(secondPoint));
		
		return section;
	}
	
	public static SignWall deserialize(ConfigurationSection section) {
		int id = section.getInt("id");
		Location first = Parser.convertStringtoLocation(section.getString("first"));
		Location second = Parser.convertStringtoLocation(section.getString("second"));
		
		return new SignWall(first, second, null, id);
	}
	
}
