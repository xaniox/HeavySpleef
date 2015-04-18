/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.persistence;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;

public enum RegionType {
	
	CUBOID(CuboidRegion.class, "cuboid"),
	CYLINDER(CylinderRegion.class, "cylinder"),
	POLYGONAL2D(Polygonal2DRegion.class, "polygonal2D");
	
	private Class<? extends Region> regionClass;
	private String persistenceName;
	
	private RegionType(Class<? extends Region> regionClass, String persistenceName) {
		this.regionClass = regionClass;
		this.persistenceName = persistenceName;
	}
	
	public Class<? extends Region> getRegionClass() {
		return regionClass;
	}
	
	public String getPersistenceName() {
		return persistenceName;
	}
	
	public synchronized static RegionType byRegionType(Class<? extends Region> clazz) {
		for (RegionType type : values()) {
			if (type.getRegionClass() == clazz) {
				return type;
			}
		}
		
		return null;
	}
	
	public synchronized static RegionType byPersistenceName(String name) {
		for (RegionType type : values()) {
			if (type.getPersistenceName().equals(name)) {
				return type;
			}
		}
		
		return null;
	}
	
}
