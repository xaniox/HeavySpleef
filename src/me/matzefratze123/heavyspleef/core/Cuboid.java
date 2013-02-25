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
