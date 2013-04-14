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
package me.matzefratze123.heavyspleef.core;

import static me.matzefratze123.heavyspleef.core.flag.FlagType.ONEVSONE;
import static me.matzefratze123.heavyspleef.core.flag.FlagType.ROUNDS;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import me.matzefratze123.heavyspleef.core.flag.FlagType;
import me.matzefratze123.heavyspleef.core.region.RegionBase;
import me.matzefratze123.heavyspleef.database.Parser;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class SignWall extends RegionBase {
	
	private Location loc1;
	private Location loc2;
	
	private Sign[] signs;
	private Game game;
	
	public SignWall(Location firstCorner, Location secondCorner, Game game, int id) {
		super(id);
		this.game = game;
		this.loc1 = firstCorner;
		this.loc2 = secondCorner;
		
		calculateSigns(firstCorner, secondCorner);
		update();
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
	
	private void calculateSigns(Location... locations) {
		if (locations.length < 2)
			return;
		if (locations[0].getBlockY() != locations[1].getBlockY())
			return;
		
		int minX = Math.min(locations[0].getBlockX(), locations[1].getBlockX());
		int maxX = Math.max(locations[0].getBlockX(), locations[1].getBlockX());
		
		int minZ = Math.min(locations[0].getBlockZ(), locations[1].getBlockZ());
		int maxZ = Math.max(locations[0].getBlockZ(), locations[1].getBlockZ());
		Sign[] signArray = null;
		
		int count = 0;
		if (minX == maxX) {
			signArray = new Sign[maxZ - minZ + 1];
			
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
					signArray[0] = (Sign)currentBlock.getState();
				else if (z == infosign) {
					signArray[1] = (Sign)currentBlock.getState();
				} else {
					signArray[count] = (Sign)currentBlock.getState();
				}
				
				count++;
			}
		} else if (minZ == maxZ) {
			signArray = new Sign[maxX - minX + 1];
			
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
					signArray[0] = (Sign)currentBlock.getState();
				else if (x == infosign) {
					signArray[1] = (Sign)currentBlock.getState();
				} else {
					signArray[count] = (Sign)currentBlock.getState();
				}
				
				count++;
			}
		}
		signs = signArray;
	}
	
	public Sign getJoinSign() {
		if (signs.length < 1)
			return null;
		
		return this.signs[0];
	}
	
	public Sign getInfoSign() {
		if (signs.length < 2)
			return null;
		
		return this.signs[1];
	}
	
	public Sign[] getSigns() {
		return this.signs;
	}
	
	public void update() {
		List<String> inPlayers = game.players;
		List<String> outPlayers = game.outPlayers;
		
		String infinity = new String("\u221E".getBytes(), Charset.forName("UTF-8"));
		String maxPlayers = String.valueOf(game.getFlag(FlagType.MAXPLAYERS) < 2 ? infinity : game.getFlag(FlagType.MAXPLAYERS));
		
		if (getJoinSign().getType() != Material.WALL_SIGN)
			getJoinSign().setType(Material.WALL_SIGN);
		getJoinSign().setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
		getJoinSign().setLine(1, ChatColor.RED + "[Join]");
		getJoinSign().setLine(2, game.getName());
		getJoinSign().update();
		
		if (getInfoSign().getType() != Material.WALL_SIGN)
			getInfoSign().setType(Material.WALL_SIGN);
		getInfoSign().setLine(0, ChatColor.RED + game.getName());
		getInfoSign().setLine(1, game.getGameState().name());
		getInfoSign().setLine(2, inPlayers.size() + "/" + ChatColor.GRAY + outPlayers.size() + ChatColor.RESET + "/" + maxPlayers);
		boolean is1vs1 = game.getFlag(ONEVSONE);
		
		if (game.isCounting())
			getInfoSign().setLine(3, ChatColor.DARK_RED + "Start in " + ChatColor.BOLD + game.getCurrentCount());
		else if (is1vs1 && (game.isIngame() || game.isCounting())) {
			int rounds = game.getFlag(ROUNDS);
			getInfoSign().setLine(3, ChatColor.DARK_GREEN + "Round " + game.getCurrentRound() + "/" + rounds);
		} else
			getInfoSign().setLine(3, "");
		getInfoSign().update();
		
		Iterator<String> inIterator = inPlayers.iterator();
		Iterator<String> outIterator = outPlayers.iterator();
		
		for (int i = 2; i < signs.length; i++) {
			for (int line = 0; line < 4; line++) {
				if (inIterator.hasNext()) {
					String name = inIterator.next();
					signs[i].setLine(line, name);
					signs[i].update();
				} else if (outIterator.hasNext()) {
					String name = outIterator.next();
					if (name.length() > 15)
						name = name.substring(0, 15);
					signs[i].setLine(line, ChatColor.GRAY + name);
					signs[i].update();
				} else {
					signs[i].setLine(line, "");
					signs[i].update();
				}
			}
		}
	}

	@Override
	public boolean contains(Location l) {
		for (Sign sign : signs) {
			if (Parser.roundLocation(sign.getLocation()).equals(Parser.roundLocation(l)))
				return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return getId() + ";" + Parser.convertLocationtoString(loc1) + ";" + Parser.convertLocationtoString(loc2); 
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
	
}
