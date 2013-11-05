package de.matzefratze123.heavyspleef.objects;

import org.bukkit.Location;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CylinderRegion;

import de.matzefratze123.heavyspleef.util.Util;

public class RegionCylinder implements Region {

	protected CylinderRegion region;
	protected int id;
	
	public RegionCylinder(int id, Location center, int radius, int minY, int maxY) {
		LocalWorld localWorld = BukkitUtil.getLocalWorld(center.getWorld());
		Vector vCenter = Util.toWorldEditVector(center);
		Vector2D v2DRadius = new Vector2D(radius, radius);
		
		region = new CylinderRegion(localWorld, vCenter, v2DRadius, minY, maxY);
	}
	
	public CylinderRegion getWorldEditRegion() {
		return region;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean contains(Location location) {
		Vector vector = Util.toWorldEditVector(location);
		
		return region.contains(vector);
	}

}
