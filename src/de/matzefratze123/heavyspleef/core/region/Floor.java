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
package de.matzefratze123.heavyspleef.core.region;

import java.util.ArrayList;
import java.util.Random;


import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;

public abstract class Floor extends RegionBase implements Comparable<Floor> {

	protected int m;
	protected byte data;
	protected FloorType type;
	protected int y;
	
	protected Random random = new Random();
	
	public ArrayList<SimpleBlockData> givenFloorList = new ArrayList<SimpleBlockData>();
	
	public Floor(int id, int m, byte data, FloorType type, int y) {
		super(id);
		
		this.m = m;
		this.data = data;
		this.type = type;
		this.y = y;
	}
	
	public abstract void initFloor();
	
	public abstract void create();
	
	public abstract void remove();
	
	public abstract GameType getType();
	
	public String asInfo() {
		return "ID: " + getId() + ", shape: " + getType().name() + ", type: " + getFloorType();
	}
	
	public FloorType getFloorType() {
		return type;
	}
	
	@Override
	public abstract String toString();
	
	public int getBlockID() {
		return this.m;
	}
	
	public byte getData() {
		return this.data;
	}
	
	public boolean isWoolFloor() {
		return type == FloorType.RANDOMWOOL;
	}
	
	public boolean isGivenFloor() {
		return type == FloorType.GIVENFLOOR;
	}
	
	public boolean isSpecifiedId() {
		return type == FloorType.SPECIFIEDID;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setFloorType(FloorType type) {
		this.type = type;
	}
	
	public SimpleBlockData getSimpleBlockData(Location loc) {
		loc = Parser.roundLocation(loc);
		for (SimpleBlockData data : givenFloorList) {
			if (data.getLocation().equals(loc))
				return data;
		}
		
		return null;
	}
	
	@Override
	public int compareTo(Floor o) {
		return Integer.valueOf(y).compareTo(o.y);
	}

}
