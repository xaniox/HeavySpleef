package de.matzefratze123.heavyspleef.core.region;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.database.DatabaseSerializeable;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;

public interface IFloor extends Comparable<IFloor>, DatabaseSerializeable {
	
	public int getId();
	
	public SimpleBlockData getBlockData();
	
	public void setBlockData(SimpleBlockData data);
	
	public FloorType getType();
	
	public boolean contains(Location location);
	
	public void generate();
	
	public void remove();
	
	public int getY();

	public String asPlayerInfo();

}
