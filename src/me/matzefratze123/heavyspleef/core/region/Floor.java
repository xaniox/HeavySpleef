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
package me.matzefratze123.heavyspleef.core.region;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.matzefratze123.heavyspleef.core.Type;
import me.matzefratze123.heavyspleef.utility.SimpleBlockData;

import org.bukkit.Location;

public abstract class Floor extends RegionBase {

	protected int m;
	protected byte data;
	protected boolean wool;
	protected boolean givenFloor;
	protected int y;
	
	protected Random random = new Random();
	
	public Map<Location, SimpleBlockData> givenFloorMap = new HashMap<Location, SimpleBlockData>();
	
	public Floor(int id, int m, byte data, boolean wool, boolean givenFloor, int y) {
		super(id);
		
		this.m = m;
		this.data = data;
		this.wool = wool;
		this.givenFloor = givenFloor;
		this.y = y;
	}
	
	public abstract void initFloor();
	
	public abstract void create();
	
	public abstract void remove();
	
	public abstract Type getType();
	
	@Override
	public abstract String toString();
	
	public int getBlockID() {
		return this.m;
	}
	
	public byte getData() {
		return this.data;
	}
	
	public boolean isWoolFloor() {
		return this.wool;
	}
	
	public boolean isGivenFloor() {
		return this.givenFloor;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setGiven(boolean given) {
		this.givenFloor = given;
	}

}
