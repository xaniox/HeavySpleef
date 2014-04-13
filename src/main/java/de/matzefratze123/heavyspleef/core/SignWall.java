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
package de.matzefratze123.heavyspleef.core;

import static de.matzefratze123.heavyspleef.core.flag.FlagType.ROUNDS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.database.DatabaseSerializeable;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.BlockIterator;

public class SignWall extends RegionCuboid implements DatabaseSerializeable {
	
	private static final int JOIN_SIGN_INDEX = 0;
	private static final int INFORMATION_SIGN_INDEX = 1;
	private static final int MAX_SIGN_LINES = 4;
	private static final char INFINITY_CHAR = '\u221E';
	
	private boolean usesX;
	private boolean positiveAttached;
	
	private boolean initialized;
	
	public SignWall(int id, Location corner1, Location corner2) {
		super(id, corner1, corner2);
		
		usesX = getFirstPoint().getBlockX() == getSecondPoint().getBlockX();
		//Calculate attachement
		positiveAttached = usesX ? getAttachedBlock(getFirstPoint().getBlock()).getLocation().getBlockX() - getFirstPoint().getBlockX() < 0 ? false : true :
								   getAttachedBlock(getFirstPoint().getBlock()).getLocation().getBlockZ() - getFirstPoint().getBlockZ() < 0 ? false : true ;
		
		initialized = true;
	}
	
	public void drawWall(Game game) {
		if (!initialized) {
			throw new IllegalStateException("SignWall was not correct initalized");
		}
		
		int min, max;
		
		int firstX = getFirstPoint().getBlockX();
		int firstZ = getFirstPoint().getBlockZ();
		int secondX = getSecondPoint().getBlockX();
		int secondZ = getSecondPoint().getBlockZ();
		
		min = usesX ? Math.min(firstZ, secondZ) : Math.min(firstX, secondX);
		max = usesX ? Math.max(firstZ, secondZ) : Math.max(firstX, secondX);
		
		List<Block> blockList = new ArrayList<Block>();
		
		for (int i = min; i <= max; i++) {
			Block next = getFirstPoint().getWorld().getBlockAt(usesX ? getFirstPoint().getBlockX() : i, getFirstPoint().getBlockY(), usesX ? i : getFirstPoint().getBlockZ());
			
			blockList.add(next);
		}
		
		boolean forward = !xor(usesX, positiveAttached);
		BlockIterator iterator = new BlockIterator(blockList);
		iterator.setDirection(forward ? BlockIterator.FORWARD : BlockIterator.BACKWARD);
		
		int index = 0;
		
		Iterator<SpleefPlayer> iteratorIn = game.getIngamePlayers().iterator();
		Iterator<OfflinePlayer> iteratorOut = game.getOutPlayers().iterator();
		
		for (Block block : iterator) {
			if (!(block.getState() instanceof Sign)) {
				continue;
			}
			
			Sign sign = (Sign) block.getState();
			
			if (index == JOIN_SIGN_INDEX) {
				sign.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Spleef" + ChatColor.DARK_GRAY + "]");
				sign.setLine(1, ChatColor.GREEN + "" + ChatColor.BOLD + "Join");
				sign.setLine(2, game.getName());
			} else if (index == INFORMATION_SIGN_INDEX) {
				String maxPlayers = String.valueOf(game.getFlag(FlagType.MAXPLAYERS) < 2 ? INFINITY_CHAR : game.getFlag(FlagType.MAXPLAYERS));
				
				sign.setLine(0, ChatColor.RED + game.getName());
				sign.setLine(1, game.getGameState().name());
				sign.setLine(2, game.getIngamePlayers().size() + "/" + ChatColor.GRAY + game.getOutPlayers().size() + ChatColor.RED + "/" + maxPlayers);
				
				if (game.getGameState() == GameState.COUNTING) {
					sign.setLine(3, ChatColor.DARK_RED + "Start in " + ChatColor.BOLD + game.getCountLeft());
				} else if (game.getFlag(FlagType.ONEVSONE) && (game.getGameState() == GameState.INGAME || game.getGameState() == GameState.COUNTING)) {
					int rounds = game.getFlag(ROUNDS);
					sign.setLine(3, ChatColor.DARK_GREEN + "Round " + (game.getRoundsPlayed() + 1) + "/" + rounds);
				} else {
					sign.setLine(3, "");
				}
			} else {
				for (int j = 0; j < MAX_SIGN_LINES; j++) {
					String name = "";
					
					if (iteratorIn.hasNext()) {
						SpleefPlayer player = iteratorIn.next();
						
						name = player.getRawName();
						
						if (game.getFlag(FlagType.TEAM)) {
							name = game.getComponents().getTeam(player).getColor().toChatColor() + name;
						} else if (name.equalsIgnoreCase("matzefratze123")) {
							name = ChatColor.DARK_RED + name;
						}
					} else if (iteratorOut.hasNext()) {
						OfflinePlayer player = iteratorOut.next();
						
						name = ChatColor.GRAY + player.getName();
					}
					
					sign.setLine(j, name);
				}
			}
			
			sign.update();
			
			index++;
		}
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
		
		return new SignWall(id, first, second);
	}
	
	private static Block getAttachedBlock(Block block) {
		MaterialData data = block.getState().getData();
		
		if (!(data instanceof Attachable)) {
			throw new IllegalArgumentException("Block cannot be attached!");
		}
		
		Attachable a = (Attachable) data;
		return block.getRelative(a.getAttachedFace());
	}
	
	private boolean xor(boolean a, boolean b) {
		return ((a || b) && !(a && b));
	}

}
