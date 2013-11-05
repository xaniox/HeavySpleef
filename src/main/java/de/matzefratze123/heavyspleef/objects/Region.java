package de.matzefratze123.heavyspleef.objects;

import org.bukkit.Location;

public interface Region {
	
	/**
	 * Gets the id of this region
	 * 
	 * @return Region id
	 */
	public int getId();
	
	/**
	 * Checks if this region contains a location
	 */
	public boolean contains(Location location);

}
