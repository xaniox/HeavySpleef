package me.matzefratze123.heavyspleef.database;

import java.io.File;

import me.matzefratze123.heavyspleef.core.Cuboid;
import me.matzefratze123.heavyspleef.core.Floor;
import me.matzefratze123.heavyspleef.core.LoseZone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Methods to parse Locations to Strings and back
 * 
 * @author matzefratze123
 */
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
	
	public static String convertFloorToString(Floor f) {
		int id = f.getId();
		String base = id + ";" + convertLocationtoString(f.getFirstCorner()) + ";" + convertLocationtoString(f.getSecondCorner());
		
		if (f.wool)
			return base + ";0;0"; 
		return base + ";" + f.getBlockID() + ";" + f.getBlockData();
	}
	
	public static String convertCuboidToString(Cuboid c) {
		int id = c.getId();
		return id + ";" + convertLocationtoString(c.getFirstCorner()) + ";" + convertLocationtoString(c.getSecondCorner());
	}
	
	public static Floor convertStringToFloor(String s) {
		String[] split = s.split(";");
		
		int id = Integer.parseInt(split[0]);
		Location firstCorner = convertStringtoLocation(split[1]);
		Location secondCorner = convertStringtoLocation(split[2]);
		
		int blockID = Integer.parseInt(split[3]);
		byte data = Byte.parseByte(split[4]);
		
		if (blockID == 0)
			return new Floor(id, firstCorner, secondCorner, 35, data, true);
		return new Floor(id, firstCorner, secondCorner, blockID, data, false);
	}
	
	public static LoseZone convertStringToLosezone(String s) {
		String[] split = s.split(";");
		
		int id = Integer.parseInt(split[0]);
		Location firstCorner = convertStringtoLocation(split[1]);
		Location secondCorner = convertStringtoLocation(split[2]);
		
		return new LoseZone(secondCorner, firstCorner, id);
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
