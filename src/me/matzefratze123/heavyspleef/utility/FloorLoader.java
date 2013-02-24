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
