package de.matzefratze123.heavyspleef.objects;

import org.bukkit.Location;

public class RegionCuboid implements Region {

	protected Location firstPoint;
	protected Location secondPoint;

	protected int id;

	public RegionCuboid(int id, Location firstPoint, Location secondPoint) {
		this.id = id;

		this.firstPoint = firstPoint;
		this.secondPoint = secondPoint;
	}

	public Location getFirstPoint() {
		return firstPoint;
	}

	public Location getSecondPoint() {
		return secondPoint;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean contains(Location location) {
		return contains(location, this);
	}
	
	public static boolean contains(Location location, RegionCuboid region) {
		int minX = Math.min(region.firstPoint.getBlockX(), region.secondPoint.getBlockX());
		int maxX = Math.max(region.firstPoint.getBlockX(), region.secondPoint.getBlockX());

		int minY = Math.min(region.firstPoint.getBlockY(), region.secondPoint.getBlockY());
		int maxY = Math.max(region.firstPoint.getBlockY(), region.secondPoint.getBlockY());

		int minZ = Math.min(region.firstPoint.getBlockZ(), region.secondPoint.getBlockZ());
		int maxZ = Math.max(region.firstPoint.getBlockZ(), region.secondPoint.getBlockZ());

		return location.getWorld() == region.firstPoint.getWorld()
				&& location.getBlockX() >= minX && location.getBlockX() <= maxX
				&& location.getBlockY() >= minY && location.getBlockY() <= maxY
				&& location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
	}
	
}
