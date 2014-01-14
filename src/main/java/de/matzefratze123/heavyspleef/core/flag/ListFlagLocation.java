/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.core.flag;

import java.io.Serializable;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class ListFlagLocation extends ListFlag<ListFlagLocation.SerializeableLocation> {

	public ListFlagLocation(String name, List<SerializeableLocation> defaulte) {
		super(name, defaulte);
	}

	@Override
	public void putElement(Player player, String input, List<SerializeableLocation> list) {
		Location location = player.getLocation();
		SerializeableLocation sLocation = new SerializeableLocation(location);
		
		list.add(sLocation);
	}

	@Override
	public String toInfo(Object value) {
		return getName() + ": LIST";
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + " /spleef flag <name> " + getName() + "\n" + 
			   HeavySpleef.PREFIX + " Adds the next location.";
	}
	
	public static class SerializeableLocation implements Serializable {

		/* Serial Version UID */
		private static final long serialVersionUID = 6983776452848943576L;
		
		private double x, y, z;
		private String world;
		
		private float pitch;
		private float yaw;
		
		private transient Location holding;
		
		public SerializeableLocation(Location holding) {
			setBukkitLocation(holding);
		}

		public double getX() {
			return x;
		}

		public void setX(double x) {
			this.x = x;
		}

		public double getY() {
			return y;
		}

		public void setY(double y) {
			this.y = y;
		}

		public double getZ() {
			return z;
		}

		public void setZ(double z) {
			this.z = z;
		}

		public String getWorld() {
			return world;
		}

		public void setWorld(String world) {
			this.world = world;
		}

		public float getPitch() {
			return pitch;
		}

		public void setPitch(float pitch) {
			this.pitch = pitch;
		}

		public float getYaw() {
			return yaw;
		}

		public void setYaw(float yaw) {
			this.yaw = yaw;
		}

		public Location getBukkitLocation() {
			if (holding == null) {
				holding = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
			}
			
			return holding;
		}

		public void setBukkitLocation(Location holding) {
			this.holding = holding;
			
			setX(holding.getX());
			setY(holding.getY());
			setZ(holding.getZ());
			
			setWorld(holding.getWorld().getName());
			setPitch(holding.getPitch());
			setYaw(holding.getYaw());
		}
		
	}
	
}
