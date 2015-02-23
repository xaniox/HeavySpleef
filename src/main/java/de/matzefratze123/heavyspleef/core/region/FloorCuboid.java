/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core.region;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameComponents;
import de.matzefratze123.heavyspleef.core.task.SaveSchematic;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.util.Logger;

public class FloorCuboid extends RegionCuboid implements IFloor {

	private Game	game;
	private boolean	randomWool;

	public FloorCuboid(int id, Game game, Location firstPoint, Location secondPoint) {
		super(id, firstPoint, secondPoint);

		this.game = game;
	}

	public void generateWool() {
		byte data = (byte) HeavySpleef.getRandom().nextInt(16);

		int minX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());
		int maxX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());

		int minY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());
		int maxY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());

		int minZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		int maxZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block current = getWorld().getBlockAt(x, y, z);

					current.setType(Material.WOOL);
					current.setData(data);
				}
			}
		}
	}

	@Override
	public int compareTo(IFloor o) {
		return Integer.valueOf(getY()).compareTo(o.getY());
	}

	@Override
	public int getY() {
		int y = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());

		return y;
	}

	@Override
	public String asPlayerInfo() {
		return "ID: " + getId() + ", shape: CUBOID";
	}

	@Override
	public ConfigurationSection serialize() {
		MemorySection section = new MemoryConfiguration();

		section.set("id", id);
		section.set("shape", "CUBOID");
		section.set("first", Parser.convertLocationtoString(firstPoint));
		section.set("second", Parser.convertLocationtoString(secondPoint));

		return section;
	}

	public static FloorCuboid deserialize(ConfigurationSection section, Game game) {
		int id = section.getInt("id");
		String shape = section.getString("shape");

		Location first, second;

		if (shape.equalsIgnoreCase("CUBOID")) {
			first = Parser.convertStringtoLocation(section.getString("first"));
			second = Parser.convertStringtoLocation(section.getString("second"));
		} else if (shape.equalsIgnoreCase("CYLINDER")) {
			// Convert old cylinder floors into cuboid floors
			Location center = Parser.convertStringtoLocation(section.getString("center"));
			int radius = section.getInt("radius");
			int min = section.getInt("min");
			int max = section.getInt("max");

			first = new Location(center.getWorld(), center.getBlockX() - radius, min, center.getBlockZ() - radius);
			second = new Location(center.getWorld(), center.getBlockX() + radius, max, center.getBlockZ() + radius);
		} else {
			Logger.warning("Invalid floor shape " + shape + " for floor " + id + "!");
			Logger.warning("Failed to load floor " + id + "!");
			return null;
		}

		FloorCuboid floor = new FloorCuboid(id, game, first, second);
		File file = new File(((GameComponents) game.getComponents()).getFloorFolder(), id + "." + FILE_EXTENSION);

		if (!file.exists()) {
			SaveSchematic saver = new SaveSchematic(floor);
			Bukkit.getScheduler().runTask(HeavySpleef.getInstance(), saver);
		}

		return floor;
	}

	@Override
	public Game getGame() {
		return game;
	}

	@Override
	public void delete() {
		game.getComponents().removeFloor(id);

		File file = new File(((GameComponents) game.getComponents()).getFloorFolder(), id + "." + FILE_EXTENSION);
		if (file.exists()) {
			file.delete();
		}
	}

	public boolean getRandomWool() {
		return randomWool;
	}

	public void setRandomWool(boolean randomWool) {
		this.randomWool = randomWool;

		if (randomWool) {
			generateWool();
		}
	}

}
