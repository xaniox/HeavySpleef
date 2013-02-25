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
package me.matzefratze123.heavyspleef.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import me.matzefratze123.heavyspleef.core.Floor;
import me.matzefratze123.heavyspleef.core.Game;

import org.bukkit.Location;

public class FloorLoader {

	public static void saveFloor(Floor f, Game game) {
		File file = new File("plugins/HeavySpleef/games/floor_" + game.getName() + "_" + f.getId() + ".floor");
		try {
			if (!file.exists())
				file.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			Set<Location> keySet = f.givenFloor.keySet();
			
			for (Location loc : keySet) {
				BlockInfo info = f.givenFloor.get(loc);
				writer.write(info.toString() + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadFloor(Floor floor, String gameName) {
		File file = new File("plugins/HeavySpleef/games/floor_" + gameName + "_" + floor.getId() + ".floor");
		try {
			if (!file.exists())
				return;
			
			floor.setGiven(true);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String line;
			
			while ((line = reader.readLine()) != null) {
				BlockInfo info = new BlockInfo(line);
				Location loc = info.getLocation();
				floor.givenFloor.put(loc, info);
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
