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
package me.matzefratze123.heavyspleef.database;

import java.io.File;

import me.matzefratze123.heavyspleef.core.region.LoseZone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Parser {

	public static String convertLocationtoString(Location location) {
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		
		float pitch = location.getPitch();
		float yaw = location.getYaw();
		
		String world = location.getWorld().getName();
		
		String s = world + "," + x + "," + y + "," + z + "," + pitch + "," + yaw;
		return s;
	}
	
	public static Location convertStringtoLocation(String s) {
		if (s == null || s.isEmpty() || s.equalsIgnoreCase("null"))
			return null;
		String[] split = s.split(",");
		
		World world = Bukkit.getWorld(split[0]);
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		
		if (world == null) {
			File f = new File(split[0]);
			if (f.exists()) {
				Bukkit.getServer().createWorld(new WorldCreator(split[0]));
				world = Bukkit.getWorld(split[0]);
			} else
				return null;
		}
		
		if (split.length > 4) {
			float pitch = Float.parseFloat(split[4]);
			float yaw = Float.parseFloat(split[5]);
			return new Location(world, x, y, z, yaw, pitch);
		}
		
		return new Location(world, x, y, z);
	}
	
	public static LoseZone convertStringToLosezone(String s) {
		String[] split = s.split(";");
		
		int id = Integer.parseInt(split[0]);
		Location firstCorner = convertStringtoLocation(split[1]);
		Location secondCorner = convertStringtoLocation(split[2]);
		
		return new LoseZone(secondCorner, firstCorner, id);
	}
	
	public static String convertLoseZoneToString(LoseZone z) {
		
		int id = z.getId();
		String firstCorner = convertLocationtoString(z.getFirstCorner());
		String secondCorner = convertLocationtoString(z.getSecondCorner());
		
		return id + ";" + firstCorner + ";" + secondCorner;
	}
	
	public static String convertPotionEffectToString(PotionEffect pe) {
		int durationTicks = pe.getDuration();
		int amplifier = pe.getAmplifier();
		String potionType = pe.getType().getName();
		return potionType + ";" + amplifier + ";" + durationTicks;
	}
	
	public static PotionEffect convertStringToPotionEffect(String s) {
		String[] split = s.split(";");
		int durationTicks = Integer.parseInt(split[2]);
		int amplifier = Integer.parseInt(split[1]);
		PotionEffectType type = PotionEffectType.getByName(split[0]);
		return new PotionEffect(type, durationTicks, amplifier);
	}

}
