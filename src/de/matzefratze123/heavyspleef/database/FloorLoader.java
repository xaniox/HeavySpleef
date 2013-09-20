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
package de.matzefratze123.heavyspleef.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.region.Floor;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;


public class FloorLoader {

	public static void saveFloor(Floor f, Game game) {
		File file = new File("plugins/HeavySpleef/games/floor_" + game.getName() + "_" + f.getId() + ".floor");
		try {
			OutputStream oS = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(oS);
			
			ArrayList<SimpleBlockData> list = f.givenFloorList;
			
			o.writeObject(list);
			o.flush();
			o.close();
		} catch (IOException e) {
			HeavySpleef.instance.getLogger().severe("Could not save floor #" + f.getId() + " from the game " + game.getName() + " to the database! IOException?!");
			e.printStackTrace();
		}
	}
	
	public static void loadFloor(Floor floor, String gameName) {
		File file = new File("plugins/HeavySpleef/games/floor_" + gameName + "_" + floor.getId() + ".floor");
		try {
			InputStream is = new FileInputStream(file);
			ObjectInputStream o = new ObjectInputStream(is);
			
			Object object = o.readObject();
			if (!(object instanceof ArrayList<?>)) {
				o.close();
				return;
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<SimpleBlockData> list = (ArrayList<SimpleBlockData>) object;
			
			floor.givenFloorList = list;
			o.close();
		} catch (IOException e) {
			HeavySpleef.instance.getLogger().severe("Could not load floor #" + floor.getId() + " from the game " + gameName + " from the database! IOException?!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
