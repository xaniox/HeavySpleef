package me.matzefratze123.heavyspleef.core;

import org.bukkit.Location;

public abstract class Cuboid {

	private Location corner1;
	private Location corner2;
	private int id;
	
	public Cuboid(Location loc1, Location loc2, int id) {
		this.setFirstCorner(loc1);
		this.setSecondCorner(loc2);
		this.setId(id);
	}
	
	public abstract void create();
	
	public abstract void remove();

	public Location getSecondCorner() {
		return corner2;
	}

	protected void setSecondCorner(Location corner2) {
		this.corner2 = corner2;
	}

	public Location getFirstCorner() {
		return corner1;
	}

	protected void setFirstCorner(Location corner1) {
		this.corner1 = corner1;
	}

	public int getId() {
		return id;
	}

	protected void setId(int id) {
		this.id = id;
	}
	
}
