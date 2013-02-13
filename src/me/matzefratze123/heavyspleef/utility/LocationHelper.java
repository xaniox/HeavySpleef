package me.matzefratze123.heavyspleef.utility;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class LocationHelper {
	
	public static boolean isInsideRegion(Block toCheck, Location corner1, Location corner2) {
		return isInsideRegion(toCheck.getLocation(), corner1, corner2);
	}
	
	public static boolean isInsideRegion(Location toCheck, Location corner1, Location corner2) {
		int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
		int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
		
		int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
		int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
		
		int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
		int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
		
		if (!toCheck.getWorld().getName().equalsIgnoreCase(corner1.getWorld().getName()))
			return false;
		if (toCheck.getBlockX() > maxX || toCheck.getBlockX() < minX)
			return false;
		if (toCheck.getBlockY() > maxY || toCheck.getBlockY() < minY)
			return false;
		if (toCheck.getBlockZ() > maxZ || toCheck.getBlockZ() < minZ)
			return false;
		return true;
	}
	
	public static double getDistance2D(Location loc1, Location loc2) {
		
		if (!loc1.getWorld().getName().equalsIgnoreCase(loc2.getWorld().getName()))
			return -1.0D;
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		int distanceX = maxX - minX;
		int distanceZ = maxZ - minZ;
		
		int realDistanceSqared = (distanceX * distanceX) + (distanceZ * distanceZ);
		return realDistanceSqared;
	}
	
	public static double getDistance3D(Location loc1, Location loc2) {
		if (!loc1.getWorld().getName().equalsIgnoreCase(loc2.getWorld().getName()))
			return -1.0D;
		
		double minX = Math.min(loc1.getX(), loc2.getX());
		double maxX = Math.max(loc1.getX(), loc2.getX());
		
		double minZ = Math.min(loc1.getZ(), loc2.getZ());
		double maxZ = Math.max(loc1.getZ(), loc2.getZ());
		
		double minY = Math.min(loc1.getY(), loc1.getY());
		double maxY = Math.max(loc1.getY(), loc2.getY());
		
		double distanceX = maxX - minX;
		double distanceZ = maxZ - minZ;
		double distanceY = maxY - minY;
		
		double XZdistance = (distanceX * distanceX) + (distanceZ * distanceZ);
		double XZYdistance = XZdistance + (distanceY * distanceY);
		
		return XZYdistance;
	}
	
}
